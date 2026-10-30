// Deep Primal's optional JNI adapter. NVIDIA SDK headers and runtime are external dependencies.
#include <jni.h>
#include <vulkan/vulkan.h>
#include <nvsdk_ngx_helpers.h>
#include <nvsdk_ngx_helpers_vk.h>

#include "DlssImageAliasGuard.h"

#include <array>
#include <cmath>
#include <cstdint>
#include <limits>
#include <memory>
#include <mutex>
#include <sstream>
#include <stdexcept>
#include <string>
#include <thread>
#include <unordered_map>

#define JNI_NAME(method) Java_net_vulkanmod_deepprimal_upscaling_dlss_DlssNative_##method

namespace {
constexpr const char* projectId = "7600a808-e574-4ff8-a74e-35a1d35425c2";
constexpr const char* engineVersion = "DeepPrimal-0.0.1";
std::mutex ngxMutex; // NGX's API is not thread-safe, including discovery calls.

void require(bool condition, const char* message) {
  if (!condition) throw std::invalid_argument(message);
}

void checked(NVSDK_NGX_Result result, const char* operation) {
  if (NVSDK_NGX_FAILED(result)) {
    std::ostringstream message;
    message << operation << " failed (NGX 0x" << std::hex << static_cast<unsigned int>(result) << ")";
    throw std::runtime_error(message.str());
  }
}

void javaError(JNIEnv* env, const std::exception& error) {
  if (env->ExceptionCheck()) return;
  jclass type = env->FindClass("java/lang/IllegalStateException");
  if (type) env->ThrowNew(type, error.what());
}

std::wstring wide(JNIEnv* env, jstring value) {
  require(value != nullptr, "Missing NGX path");
  const jchar* characters = env->GetStringChars(value, nullptr);
  if (!characters) throw std::runtime_error("Cannot read NGX path");
  const jsize length = env->GetStringLength(value);
  std::wstring result;
  try {
    result.reserve(length);
    for (jsize i = 0; i < length; ++i) {
      std::uint32_t code = characters[i];
      if (sizeof(wchar_t) > 2 && code >= 0xd800 && code <= 0xdbff && i + 1 < length) {
        const std::uint32_t low = characters[i + 1];
        if (low >= 0xdc00 && low <= 0xdfff) {
          code = 0x10000 + ((code - 0xd800) << 10) + low - 0xdc00;
          ++i;
        }
      }
      require(code != 0, "NGX paths must not contain NUL");
      result.push_back(static_cast<wchar_t>(code));
    }
  } catch (...) {
    env->ReleaseStringChars(value, characters);
    throw;
  }
  env->ReleaseStringChars(value, characters);
  return result;
}

template <class T> T handle(jlong value) {
  return reinterpret_cast<T>(static_cast<std::uintptr_t>(value));
}

struct Discovery {
  std::wstring dataPath;
  std::wstring runtimePath;
  const wchar_t* runtimePaths[1]{};
  NVSDK_NGX_FeatureCommonInfo common{};
  NVSDK_NGX_FeatureDiscoveryInfo info{};

  Discovery(JNIEnv* env, jstring data, jstring runtime)
      : dataPath(wide(env, data)), runtimePath(wide(env, runtime)) {
    require(!dataPath.empty() && !runtimePath.empty(), "NGX paths must not be empty");
    runtimePaths[0] = runtimePath.c_str();
    common.PathListInfo.Path = runtimePaths;
    common.PathListInfo.Length = 1;
    info.SDKVersion = NVSDK_NGX_Version_API;
    info.FeatureID = NVSDK_NGX_Feature_SuperSampling;
    info.Identifier.IdentifierType = NVSDK_NGX_Application_Identifier_Type_Project_Id;
    info.Identifier.v.ProjectDesc = {projectId, NVSDK_NGX_ENGINE_TYPE_CUSTOM, engineVersion};
    info.ApplicationDataPath = dataPath.c_str();
    info.FeatureInfo = &common;
  }
};

struct Session {
  Discovery discovery;
  VkDevice device;
  PFN_vkDeviceWaitIdle waitIdle;
  std::thread::id owner = std::this_thread::get_id();
  NVSDK_NGX_Parameter* capabilities = nullptr;
  NVSDK_NGX_Parameter* parameters = nullptr;
  NVSDK_NGX_Handle* feature = nullptr;
  bool initialized = false;
  bool failed = false;
  unsigned int renderWidth = 0, renderHeight = 0, outputWidth = 0, outputHeight = 0;

  Session(JNIEnv* env, jstring data, jstring runtime, VkDevice logicalDevice, PFN_vkDeviceWaitIdle wait)
      : discovery(env, data, runtime), device(logicalDevice), waitIdle(wait) {}

  ~Session() {
    // Every successful session reaches this only after the renderer's completion boundary.
    // An open() failure cannot have recorded any feature work yet.
    if (feature) NVSDK_NGX_VULKAN_ReleaseFeature(feature);
    if (parameters) NVSDK_NGX_VULKAN_DestroyParameters(parameters);
    if (capabilities) NVSDK_NGX_VULKAN_DestroyParameters(capabilities);
    if (initialized) NVSDK_NGX_VULKAN_Shutdown1(device);
  }
};

std::unordered_map<jlong, std::unique_ptr<Session>> sessions;
jlong nextSession = 1;

Session& session(jlong id, bool allowFailed = false) {
  auto item = sessions.find(id);
  require(item != sessions.end(), "Unknown or closed DLSS context");
  Session& value = *item->second;
  require(value.owner == std::this_thread::get_id(), "DLSS render-thread ownership violated");
  require(allowFailed || !value.failed, "DLSS context failed; retire it before retrying");
  return value;
}

NVSDK_NGX_PerfQuality_Value quality(jint value) {
  require(value >= 0 && value <= 4, "Unsupported DLSS quality enum");
  return static_cast<NVSDK_NGX_PerfQuality_Value>(value);
}

jintArray integers(JNIEnv* env, const jint* values, jsize count) {
  jintArray result = env->NewIntArray(count);
  if (result) env->SetIntArrayRegion(result, 0, count, values);
  return result;
}

NVSDK_NGX_Resource_VK image(jlong imageHandle, jlong viewHandle, jint format,
    unsigned int width, unsigned int height, bool depth, bool output) {
  require(imageHandle != 0 && viewHandle != 0 && format > 0, "Invalid DLSS Vulkan image");
  VkImageSubresourceRange subresource{};
  subresource.aspectMask = depth ? VK_IMAGE_ASPECT_DEPTH_BIT : VK_IMAGE_ASPECT_COLOR_BIT;
  subresource.levelCount = 1;
  subresource.layerCount = 1;
  return NVSDK_NGX_Create_ImageView_Resource_VK(handle<VkImageView>(viewHandle),
      handle<VkImage>(imageHandle), subresource, static_cast<VkFormat>(format), width, height, output);
}
} // namespace

extern "C" {
JNIEXPORT jint JNICALL JNI_NAME(abiVersion)(JNIEnv*, jclass) { return 1; }

JNIEXPORT jobjectArray JNICALL JNI_NAME(extensions)(JNIEnv* env, jclass,
    jlong instance, jlong physicalDevice, jstring data, jstring runtime, jboolean forInstance) {
  try {
    std::lock_guard<std::mutex> lock(ngxMutex);
    Discovery discovery(env, data, runtime);
    std::uint32_t count = 0;
    VkExtensionProperties* properties = nullptr;
    if (forInstance) {
      checked(NVSDK_NGX_VULKAN_GetFeatureInstanceExtensionRequirements(
          &discovery.info, &count, &properties), "DLSS instance-extension query");
    } else {
      require(instance != 0 && physicalDevice != 0, "Missing Vulkan adapter");
      checked(NVSDK_NGX_VULKAN_GetFeatureDeviceExtensionRequirements(handle<VkInstance>(instance),
          handle<VkPhysicalDevice>(physicalDevice), &discovery.info, &count, &properties),
          "DLSS device-extension query");
    }
    require(count <= 4096 && (count == 0 || properties), "Invalid NGX extension list");
    jclass stringClass = env->FindClass("java/lang/String");
    if (!stringClass) return nullptr;
    jobjectArray result = env->NewObjectArray(static_cast<jsize>(count), stringClass, nullptr);
    if (!result) return nullptr;
    for (std::uint32_t i = 0; i < count; ++i) {
      jstring name = env->NewStringUTF(properties[i].extensionName);
      if (!name) return nullptr;
      env->SetObjectArrayElement(result, static_cast<jsize>(i), name);
      env->DeleteLocalRef(name);
      if (env->ExceptionCheck()) return nullptr;
    }
    return result;
  } catch (const std::exception& error) { javaError(env, error); return nullptr; }
}

JNIEXPORT jintArray JNICALL JNI_NAME(requirements)(JNIEnv* env, jclass,
    jlong instance, jlong physicalDevice, jstring data, jstring runtime) {
  try {
    std::lock_guard<std::mutex> lock(ngxMutex);
    require(instance != 0 && physicalDevice != 0, "Missing Vulkan adapter");
    Discovery discovery(env, data, runtime);
    NVSDK_NGX_FeatureRequirement support{};
    checked(NVSDK_NGX_VULKAN_GetFeatureRequirements(handle<VkInstance>(instance),
        handle<VkPhysicalDevice>(physicalDevice), &discovery.info, &support), "DLSS adapter query");
    const jint values[] = {static_cast<jint>(support.FeatureSupported),
        static_cast<jint>(support.MinHWArchitecture)};
    return integers(env, values, 2);
  } catch (const std::exception& error) { javaError(env, error); return nullptr; }
}

JNIEXPORT jlong JNICALL JNI_NAME(open)(JNIEnv* env, jclass, jlong instance,
    jlong physicalDevice, jlong device, jlong getInstanceProcAddr, jlong getDeviceProcAddr,
    jstring data, jstring runtime) {
  try {
    std::lock_guard<std::mutex> lock(ngxMutex);
    require(instance && physicalDevice && device && getInstanceProcAddr && getDeviceProcAddr,
        "Missing Vulkan device or dispatch pointers");
    auto logicalDevice = handle<VkDevice>(device);
    for (const auto& existing : sessions)
      require(existing.second->device != logicalDevice, "One NGX session per Vulkan device is supported");
    auto getDevice = handle<PFN_vkGetDeviceProcAddr>(getDeviceProcAddr);
    auto waitIdle = reinterpret_cast<PFN_vkDeviceWaitIdle>(getDevice(logicalDevice, "vkDeviceWaitIdle"));
    require(waitIdle != nullptr, "Vulkan vkDeviceWaitIdle dispatch is missing");
    auto value = std::make_unique<Session>(env, data, runtime, logicalDevice, waitIdle);
    checked(NVSDK_NGX_VULKAN_Init_with_ProjectID(projectId, NVSDK_NGX_ENGINE_TYPE_CUSTOM,
        engineVersion, value->discovery.dataPath.c_str(), handle<VkInstance>(instance),
        handle<VkPhysicalDevice>(physicalDevice), logicalDevice,
        handle<PFN_vkGetInstanceProcAddr>(getInstanceProcAddr), getDevice,
        &value->discovery.common, NVSDK_NGX_Version_API), "DLSS initialization");
    value->initialized = true;
    checked(NVSDK_NGX_VULKAN_GetCapabilityParameters(&value->capabilities), "DLSS capability query");
    int needsDriver = 0;
    value->capabilities->Get(NVSDK_NGX_Parameter_SuperSampling_NeedsUpdatedDriver, &needsDriver);
    require(needsDriver == 0, "DLSS requires a newer NVIDIA driver");
    int available = 0;
    checked(value->capabilities->Get(NVSDK_NGX_Parameter_SuperSampling_Available, &available),
        "DLSS availability query");
    require(available != 0, "NVIDIA NGX did not enable DLSS on this adapter/runtime");
    checked(NVSDK_NGX_VULKAN_AllocateParameters(&value->parameters), "DLSS parameter allocation");
    require(nextSession > 0 && nextSession < std::numeric_limits<jlong>::max(), "DLSS context counter exhausted");
    const jlong id = nextSession++;
    sessions.emplace(id, std::move(value));
    return id;
  } catch (const std::exception& error) { javaError(env, error); return 0; }
}

JNIEXPORT jintArray JNICALL JNI_NAME(optimalSettings)(JNIEnv* env, jclass,
    jlong id, jint width, jint height, jint preset) {
  try {
    std::lock_guard<std::mutex> lock(ngxMutex);
    Session& value = session(id);
    require(width > 0 && height > 0, "Invalid DLSS output dimensions");
    unsigned int optimalWidth = 0, optimalHeight = 0, maximumWidth = 0, maximumHeight = 0;
    unsigned int minimumWidth = 0, minimumHeight = 0;
    float unusedSharpness = 0;
    checked(NGX_DLSS_GET_OPTIMAL_SETTINGS(value.capabilities, width, height, quality(preset),
        &optimalWidth, &optimalHeight, &maximumWidth, &maximumHeight,
        &minimumWidth, &minimumHeight, &unusedSharpness), "DLSS optimal-size query");
    require(optimalWidth > 0 && optimalHeight > 0 && optimalWidth <= static_cast<unsigned int>(width)
        && optimalHeight <= static_cast<unsigned int>(height), "DLSS quality mode is unavailable at this size");
    const jint sizes[] = {static_cast<jint>(optimalWidth), static_cast<jint>(optimalHeight),
        static_cast<jint>(minimumWidth), static_cast<jint>(minimumHeight),
        static_cast<jint>(maximumWidth), static_cast<jint>(maximumHeight)};
    return integers(env, sizes, 6);
  } catch (const std::exception& error) { javaError(env, error); return nullptr; }
}

JNIEXPORT void JNICALL JNI_NAME(create)(JNIEnv* env, jclass, jlong id, jlong commandBuffer,
    jint renderWidth, jint renderHeight, jint outputWidth, jint outputHeight, jint preset,
    jboolean hdr, jboolean invertedDepth) {
  try {
    std::lock_guard<std::mutex> lock(ngxMutex);
    Session& value = session(id);
    require(commandBuffer != 0 && value.feature == nullptr, "Invalid DLSS feature creation state");
    require(renderWidth > 0 && renderHeight > 0 && outputWidth >= renderWidth
        && outputHeight >= renderHeight, "Invalid DLSS feature dimensions");
    NVSDK_NGX_DLSS_Create_Params parameters{};
    parameters.Feature = {static_cast<unsigned int>(renderWidth), static_cast<unsigned int>(renderHeight),
        static_cast<unsigned int>(outputWidth), static_cast<unsigned int>(outputHeight), quality(preset)};
    parameters.InFeatureCreateFlags = NVSDK_NGX_DLSS_Feature_Flags_MVLowRes
        | NVSDK_NGX_DLSS_Feature_Flags_AutoExposure
        | (hdr ? NVSDK_NGX_DLSS_Feature_Flags_IsHDR : 0)
        | (invertedDepth ? NVSDK_NGX_DLSS_Feature_Flags_DepthInverted : 0);
    const NVSDK_NGX_Result result = NGX_VULKAN_CREATE_DLSS_EXT1(value.device,
        handle<VkCommandBuffer>(commandBuffer), 1, 1, &value.feature, value.parameters, &parameters);
    if (NVSDK_NGX_FAILED(result)) value.failed = true;
    checked(result, "DLSS feature creation");
    value.renderWidth = renderWidth; value.renderHeight = renderHeight;
    value.outputWidth = outputWidth; value.outputHeight = outputHeight;
  } catch (const std::exception& error) { javaError(env, error); }
}

JNIEXPORT void JNICALL JNI_NAME(evaluate)(JNIEnv* env, jclass, jlong id, jlong commandBuffer,
    jlongArray imageArray, jintArray formatArray, jlongArray bindingArray,
    jfloat jitterX, jfloat jitterY,
    jfloat motionScaleX, jfloat motionScaleY, jfloat frameTime, jfloat preExposure, jboolean reset) {
  try {
    std::lock_guard<std::mutex> lock(ngxMutex);
    Session& value = session(id);
    require(commandBuffer != 0 && value.feature, "DLSS feature is not created");
    require(imageArray && formatArray && bindingArray && env->GetArrayLength(imageArray) == 8
        && env->GetArrayLength(formatArray) == 4 && env->GetArrayLength(bindingArray) == 12,
        "Invalid DLSS image or memory-binding arrays");
    require(std::isfinite(jitterX) && std::isfinite(jitterY) && std::isfinite(motionScaleX)
        && std::isfinite(motionScaleY) && motionScaleX != 0 && motionScaleY != 0
        && std::isfinite(frameTime) && frameTime > 0 && std::isfinite(preExposure)
        && preExposure > 0, "Invalid DLSS temporal constants");
    std::array<jlong, 8> handles{};
    std::array<jint, 4> formats{};
    std::array<jlong, 12> bindingValues{};
    env->GetLongArrayRegion(imageArray, 0, 8, handles.data());
    env->GetIntArrayRegion(formatArray, 0, 4, formats.data());
    env->GetLongArrayRegion(bindingArray, 0, 12, bindingValues.data());
    if (env->ExceptionCheck()) return;
    // Duplicate VkImage / VkImageView handles must not reach NGX, including via
    // a direct JNI caller that bypasses the Java DlssFrame contract.
    // Distinct handles do not prove disjoint VkDeviceMemory allocations.
    require(deepprimal::dlss::hasDistinctImagesAndViews(handles),
        "DLSS sampled inputs and output require distinct image/view handles");
    std::array<deepprimal::dlss::MemoryBinding, 4> bindings{};
    for (std::size_t index = 0; index < bindings.size(); ++index) {
      const std::size_t base = index * 3;
      bindings[index] = {static_cast<std::uint64_t>(bindingValues[base]),
          static_cast<std::uint64_t>(bindingValues[base + 1]),
          static_cast<std::uint64_t>(bindingValues[base + 2])};
    }
    require(deepprimal::dlss::hasDisjointBindings(bindings),
        "DLSS resources require valid, disjoint VkDeviceMemory bindings");
    require(formats[2] == VK_FORMAT_R16G16_SFLOAT || formats[2] == VK_FORMAT_R32G32_SFLOAT,
        "DLSS requires floating point RG motion vectors");
    require(formats[1] == VK_FORMAT_D16_UNORM || formats[1] == VK_FORMAT_X8_D24_UNORM_PACK32
        || formats[1] == VK_FORMAT_D32_SFLOAT, "DLSS requires a depth-only sampled view");
    auto color = image(handles[0], handles[1], formats[0], value.renderWidth, value.renderHeight, false, false);
    auto depth = image(handles[2], handles[3], formats[1], value.renderWidth, value.renderHeight, true, false);
    auto motion = image(handles[4], handles[5], formats[2], value.renderWidth, value.renderHeight, false, false);
    auto output = image(handles[6], handles[7], formats[3], value.outputWidth, value.outputHeight, false, true);
    NVSDK_NGX_VK_DLSS_Eval_Params parameters{};
    parameters.Feature.pInColor = &color;
    parameters.Feature.pInOutput = &output;
    parameters.pInDepth = &depth;
    parameters.pInMotionVectors = &motion;
    parameters.InJitterOffsetX = jitterX; parameters.InJitterOffsetY = jitterY;
    parameters.InMVScaleX = motionScaleX; parameters.InMVScaleY = motionScaleY;
    parameters.InRenderSubrectDimensions = {value.renderWidth, value.renderHeight};
    parameters.InFrameTimeDeltaInMsec = frameTime;
    parameters.InPreExposure = preExposure;
    parameters.InExposureScale = 1.0f;
    parameters.InReset = reset ? 1 : 0;
    const NVSDK_NGX_Result result = NGX_VULKAN_EVALUATE_DLSS_EXT(
        handle<VkCommandBuffer>(commandBuffer), value.feature, value.parameters, &parameters);
    if (NVSDK_NGX_FAILED(result)) value.failed = true;
    checked(result, "DLSS evaluation");
  } catch (const std::exception& error) { javaError(env, error); }
}

JNIEXPORT void JNICALL JNI_NAME(close)(JNIEnv* env, jclass, jlong id) {
  try {
    std::lock_guard<std::mutex> lock(ngxMutex);
    Session& value = session(id, true);
    // The Java drain callback must first submit/discard recording buffers; waiting only
    // protects already-submitted commands. Never retire a feature from an active recording.
    VkResult result = value.waitIdle(value.device);
    require(result == VK_SUCCESS || result == VK_ERROR_DEVICE_LOST, "DLSS GPU completion wait failed");
    sessions.erase(id);
  } catch (const std::exception& error) { javaError(env, error); }
}
} // extern "C"
