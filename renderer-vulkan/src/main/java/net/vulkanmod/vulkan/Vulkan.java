// VulkanMod 0.6.8+26.1.2, Collateral and contributors. LGPL-3.0-only.
// Reconstructed from the matching published artifact; modified for Deep Primal on 2026-10-19.
package net.vulkanmod.vulkan;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.Util;
import net.minecraft.util.Util.OS;
import net.vulkanmod.deepprimal.GlfwVulkanBridge;
import net.vulkanmod.vulkan.device.Device;
import net.vulkanmod.vulkan.device.DeviceManager;
import net.vulkanmod.vulkan.framebuffer.SwapChain;
import net.vulkanmod.vulkan.memory.MemoryManager;
import net.vulkanmod.vulkan.memory.MemoryTypes;
import net.vulkanmod.vulkan.memory.buffer.StagingBuffer;
import net.vulkanmod.vulkan.memory.buffer.StagingBuffers;
import net.vulkanmod.vulkan.queue.Queue;
import net.vulkanmod.vulkan.shader.Pipeline;
import net.vulkanmod.vulkan.texture.SamplerManager;
import net.vulkanmod.vulkan.util.VkResult;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.EXTDebugUtils;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;
import org.lwjgl.vulkan.VkLayerProperties.Buffer;

public class Vulkan {
  public static final boolean ENABLE_VALIDATION_LAYERS =
      Boolean.getBoolean("deepprimal.vulkan.validation");
  public static final boolean DYNAMIC_RENDERING = true;
  public static final Set<String> VALIDATION_LAYERS = Set.of("VK_LAYER_KHRONOS_validation");
  public static final Set<String> REQUIRED_EXTENSION = getRequiredExtensionSet();
  public static long window;
  public static Set<String> instanceExtensions;
  private static VkInstance instance;
  private static long debugMessenger;
  private static boolean validationActive, debugUtilsEnabled;
  private static org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT debugCallbackHandle;

  public static boolean debugUtilsEnabled() {
    return debugUtilsEnabled;
  }

  private static void freeDebugCallback() {
    if (debugCallbackHandle != null) {
      debugCallbackHandle.free();
      debugCallbackHandle = null;
    }
  }

  private static long surface;
  private static long commandPool;
  private static VkCommandBuffer immediateCmdBuffer;
  private static long immediateFence;
  private static long allocator;
  private static final StagingBuffers stagingBuffers = new StagingBuffers();
  public static boolean use24BitsDepthFormat = true;
  private static int DEFAULT_DEPTH_FORMAT = 0;

  private static Set<String> getRequiredExtensionSet() {
    ArrayList<String> extensions = new ArrayList<>(List.of("VK_KHR_swapchain"));
    extensions.add("VK_KHR_dynamic_rendering");
    return new HashSet<>(extensions);
  }

  private static int debugCallback(int severity, int type, long data, long user) {
    var message = VkDebugUtilsMessengerCallbackDataEXT.create(data);
    if ((severity & 4096) != 0) {
      VulkanStatistics.validationErrors.incrementAndGet();
      net.vulkanmod.Initializer.LOGGER.error(
          "VULKAN_VALIDATION {}: {}", message.pMessageIdNameString(), message.pMessageString());
    } else
      net.vulkanmod.Initializer.LOGGER.warn(
          "VULKAN_VALIDATION {}: {}", message.pMessageIdNameString(), message.pMessageString());
    return 0;
  }

  private static int createDebugUtilsMessengerEXT(
      VkInstance instance,
      VkDebugUtilsMessengerCreateInfoEXT createInfo,
      VkAllocationCallbacks allocationCallbacks,
      LongBuffer pDebugMessenger) {
    return VK10.vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT") != 0L
        ? EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(
            instance, createInfo, allocationCallbacks, pDebugMessenger)
        : -7;
  }

  private static void destroyDebugUtilsMessengerEXT(
      VkInstance instance, long debugMessenger, VkAllocationCallbacks allocationCallbacks) {
    if (VK10.vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT") != 0L) {
      EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, allocationCallbacks);
    }
  }

  public static void initVulkan(long window) {
    createInstance();
    setupDebugMessenger();
    createSurface(window);
    DeviceManager.init(instance);
    createVma();
    MemoryTypes.createMemoryTypes();
    createCommandPool();
    setupDepthFormat();
  }

  static void createStagingBuffers() {
    stagingBuffers.updateFrameCount(Renderer.getFramesNum());
  }

  static void setupDepthFormat() {
    DEFAULT_DEPTH_FORMAT = DeviceManager.findDepthFormat(use24BitsDepthFormat);
  }

  public static void waitIdle() {
    VK10.vkDeviceWaitIdle(DeviceManager.vkDevice);
  }

  /** Initialization can fail before an allocator, queue or renderer exists. */
  public static void abortInitialization() {
    if (DeviceManager.vkDevice != null) {
      VK10.vkDeviceWaitIdle(DeviceManager.vkDevice);
      if (commandPool != 0) {
        VK10.vkDestroyCommandPool(DeviceManager.vkDevice, commandPool, null);
        commandPool = 0;
      }
      if (immediateFence != 0) {
        VK10.vkDestroyFence(DeviceManager.vkDevice, immediateFence, null);
        immediateFence = 0;
      }
      if (allocator != 0) {
        Vma.vmaDestroyAllocator(allocator);
        allocator = 0;
      }
    }
    DeviceManager.destroy();
    if (instance != null) {
      if (surface != 0) {
        KHRSurface.vkDestroySurfaceKHR(instance, surface, null);
        surface = 0;
      }
      VK10.vkDestroyInstance(instance, null);
      instance = null;
    }
    freeDebugCallback();
  }

  public static void cleanUp() {
    VK10.vkDeviceWaitIdle(DeviceManager.vkDevice);
    VK10.vkDestroyCommandPool(DeviceManager.vkDevice, commandPool, null);
    VK10.vkDestroyFence(DeviceManager.vkDevice, immediateFence, null);
    Pipeline.destroyPipelineCache();
    net.vulkanmod.vulkan.shader.SPIRVUtils.shutdown();
    Renderer.getInstance().cleanUpResources();
    freeStagingBuffers();

    try {
      MemoryManager.getInstance().freeAllBuffers();
    } catch (Exception var1) {
      var1.printStackTrace();
    }

    Vma.vmaDestroyAllocator(allocator);
    SamplerManager.cleanUp();
    DeviceManager.destroy();
    destroyDebugUtilsMessengerEXT(instance, debugMessenger, null);
    KHRSurface.vkDestroySurfaceKHR(instance, surface, null);
    VK10.vkDestroyInstance(instance, null);
    instance = null;
    debugMessenger = 0;
    freeDebugCallback();
  }

  private static void freeStagingBuffers() {
    stagingBuffers.free();
  }

  private static void createInstance() {
    instanceExtensions = querySupportedInstanceExtension();
    validationActive = ENABLE_VALIDATION_LAYERS && checkValidationLayerSupport();
    debugUtilsEnabled =
        (ENABLE_VALIDATION_LAYERS || Boolean.getBoolean("deepprimal.vulkan.debug"))
            && instanceExtensions.contains(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
    if (ENABLE_VALIDATION_LAYERS && !validationActive)
      net.vulkanmod.Initializer.LOGGER.warn("Khronos validation layer is unavailable");

    try (MemoryStack stack = MemoryStack.stackPush()) {
      VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack);
      appInfo.sType(0);
      appInfo.pApplicationName(stack.UTF8Safe("VulkanMod"));
      appInfo.applicationVersion(VK10.VK_MAKE_VERSION(1, 0, 0));
      appInfo.pEngineName(stack.UTF8Safe("VulkanMod Engine"));
      appInfo.engineVersion(VK10.VK_MAKE_VERSION(1, 0, 0));
      appInfo.apiVersion(VK12.VK_API_VERSION_1_2);
      VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc(stack);
      createInfo.sType(1);
      createInfo.pApplicationInfo(appInfo);
      createInfo.ppEnabledExtensionNames(getRequiredInstanceExtensions());
      if (Util.getPlatform() == OS.OSX
          && instanceExtensions.contains("VK_KHR_portability_enumeration"))
        createInfo.flags(
            org.lwjgl.vulkan.KHRPortabilityEnumeration
                .VK_INSTANCE_CREATE_ENUMERATE_PORTABILITY_BIT_KHR);
      if (validationActive)
        createInfo.ppEnabledLayerNames(stack.pointers(stack.UTF8("VK_LAYER_KHRONOS_validation")));
      if (debugUtilsEnabled) {
        var debug = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
        populateDebugMessengerCreateInfo(debug);
        createInfo.pNext(debug);
      }
      PointerBuffer instancePtr = stack.mallocPointer(1);
      int result = VK10.vkCreateInstance(createInfo, null, instancePtr);
      checkResult(result, "Failed to create instance");
      instance = new VkInstance(instancePtr.get(0), createInfo);
      net.vulkanmod.Initializer.LOGGER.info(
          "VULKAN_VALIDATION_ACTIVE {} debugUtils={}", validationActive, debugUtilsEnabled);
    }
  }

  static boolean checkValidationLayerSupport() {
    boolean var4;
    try (MemoryStack stack = MemoryStack.stackPush()) {
      IntBuffer layerCount = stack.ints(0);
      VK10.vkEnumerateInstanceLayerProperties(layerCount, null);
      Buffer availableLayers = VkLayerProperties.malloc(layerCount.get(0), stack);
      VK10.vkEnumerateInstanceLayerProperties(layerCount, availableLayers);
      Set<String> availableLayerNames =
          availableLayers.stream()
              .map(VkLayerProperties::layerNameString)
              .collect(Collectors.toSet());
      var4 = availableLayerNames.containsAll(VALIDATION_LAYERS);
    }

    return var4;
  }

  private static void populateDebugMessengerCreateInfo(
      VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo) {
    debugCreateInfo.sType(1000128004);
    debugCreateInfo.messageSeverity(4352);
    debugCreateInfo.messageType(7);
    if (debugCallbackHandle == null)
      debugCallbackHandle =
          org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackEXT.create(Vulkan::debugCallback);
    debugCreateInfo.pfnUserCallback(debugCallbackHandle);
  }

  private static void setupDebugMessenger() {
    if (!debugUtilsEnabled) return;
    try (var stack = MemoryStack.stackPush()) {
      var info = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
      populateDebugMessengerCreateInfo(info);
      var out = stack.mallocLong(1);
      checkResult(
          createDebugUtilsMessengerEXT(instance, info, null, out), "Cannot create debug messenger");
      debugMessenger = out.get(0);
    }
  }

  public static void setDebugLabel(MemoryStack stack, int objectType, long handle, String label) {
    if (!debugUtilsEnabled || handle == 0 || label == null) return;
    var info =
        org.lwjgl.vulkan.VkDebugUtilsObjectNameInfoEXT.calloc(stack)
            .sType$Default()
            .objectType(objectType)
            .objectHandle(handle)
            .pObjectName(stack.UTF8(label));
    EXTDebugUtils.vkSetDebugUtilsObjectNameEXT(DeviceManager.vkDevice, info);
  }

  private static void createSurface(long handle) {
    window = handle;

    try (MemoryStack stack = MemoryStack.stackPush()) {
      LongBuffer pSurface = stack.longs(0L);
      checkResult(
          GlfwVulkanBridge.createSurface(
              instance.address(), window, org.lwjgl.system.MemoryUtil.memAddress(pSurface)),
          "Failed to create window surface");
      surface = pSurface.get(0);
    }
  }

  private static void createVma() {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      VmaVulkanFunctions vulkanFunctions = VmaVulkanFunctions.calloc(stack);
      vulkanFunctions.set(instance, DeviceManager.vkDevice);
      VmaAllocatorCreateInfo allocatorCreateInfo = VmaAllocatorCreateInfo.calloc(stack);
      if (DeviceManager.enabledExtensions.contains("VK_EXT_memory_budget"))
        allocatorCreateInfo.flags(
            org.lwjgl.util.vma.Vma.VMA_ALLOCATOR_CREATE_EXT_MEMORY_BUDGET_BIT);
      allocatorCreateInfo.physicalDevice(DeviceManager.physicalDevice);
      allocatorCreateInfo.device(DeviceManager.vkDevice);
      allocatorCreateInfo.pVulkanFunctions(vulkanFunctions);
      allocatorCreateInfo.instance(instance);
      allocatorCreateInfo.vulkanApiVersion(VK12.VK_API_VERSION_1_2);
      PointerBuffer pAllocator = stack.pointers(0L);
      checkResult(
          Vma.vmaCreateAllocator(allocatorCreateInfo, pAllocator), "Failed to create Allocator");
      allocator = pAllocator.get(0);
    }
  }

  private static void createCommandPool() {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      Queue.QueueFamilyIndices queueFamilyIndices = Queue.getQueueFamilies();
      VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc(stack);
      poolInfo.sType(39);
      poolInfo.queueFamilyIndex(queueFamilyIndices.graphicsFamily);
      poolInfo.flags(2);
      LongBuffer pCommandPool = stack.mallocLong(1);
      checkResult(
          VK10.vkCreateCommandPool(DeviceManager.vkDevice, poolInfo, null, pCommandPool),
          "Failed to create command pool");
      commandPool = pCommandPool.get(0);
    }
  }

  private static PointerBuffer getRequiredInstanceExtensions() {
    PointerBuffer glfwExtensions = GlfwVulkanBridge.extensions();
    List<String> otherExtensions = new ArrayList<>();
    if (Util.getPlatform() == OS.OSX
        && instanceExtensions.contains("VK_KHR_portability_enumeration")) {
      otherExtensions.add("VK_KHR_portability_enumeration");
    }

    if (debugUtilsEnabled) otherExtensions.add(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
    if (otherExtensions.isEmpty()) {
      return glfwExtensions;
    } else {
      MemoryStack stack = MemoryStack.stackGet();
      PointerBuffer extensions =
          stack.mallocPointer(glfwExtensions.capacity() + otherExtensions.size());
      extensions.put(glfwExtensions);

      for (String s : otherExtensions) {
        extensions.put(stack.UTF8(s));
      }

      return extensions.rewind();
    }
  }

  private static Set<String> querySupportedInstanceExtension() {
    MemoryStack stack = MemoryStack.stackGet();
    IntBuffer pExtensionCount = stack.mallocInt(1);
    VK10.vkEnumerateInstanceExtensionProperties((String) null, pExtensionCount, null);
    org.lwjgl.vulkan.VkExtensionProperties.Buffer instanceExtensionProperties =
        VkExtensionProperties.malloc(pExtensionCount.get(0));
    VK10.vkEnumerateInstanceExtensionProperties(
        (String) null, pExtensionCount, instanceExtensionProperties);
    HashSet<String> instanceExtensions = new HashSet<>();

    for (int i = 0; i < pExtensionCount.get(0); i++) {
      instanceExtensions.add(instanceExtensionProperties.get(i).extensionNameString());
    }

    instanceExtensionProperties.free();
    return instanceExtensions;
  }

  public static void checkResult(int result, String errorMessage) {
    if (result != 0) {
      throw new RuntimeException(String.format("%s: %s", errorMessage, VkResult.decode(result)));
    }
  }

  public static void setVsync(boolean b) {
    SwapChain swapChain = Renderer.getInstance().getSwapChain();
    if (swapChain.isVsync() != b) {
      Renderer.scheduleSwapChainUpdate();
      swapChain.setVsync(b);
    }
  }

  public static VkDevice getVkDevice() {
    return DeviceManager.vkDevice;
  }

  public static long getAllocator() {
    return allocator;
  }

  public static int getDefaultDepthFormat() {
    return DEFAULT_DEPTH_FORMAT;
  }

  public static long getSurface() {
    return surface;
  }

  public static long getCommandPool() {
    return commandPool;
  }

  public static StagingBuffer getStagingBuffer() {
    return stagingBuffers.getStagingBuffer();
  }

  public static StagingBuffers getStagingBuffers() {
    return stagingBuffers;
  }

  public static Device getDevice() {
    return DeviceManager.device;
  }
}
