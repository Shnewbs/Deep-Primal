// TEST ONLY: synthetic JNI ABI fault injection. This is not NVIDIA code or a DLSS runtime.
// Never package this library in an installer or use it to claim graphics/RTX validation.
#include <jni.h>

#define JNI_NAME(method) Java_net_vulkanmod_deepprimal_upscaling_dlss_DlssNative_##method
namespace {
bool openContext = false;
int frames = 0;
void fail(JNIEnv* env, const char* message) {
  env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), message);
}
}

extern "C" {
JNIEXPORT jint JNICALL JNI_NAME(abiVersion)(JNIEnv*, jclass) { return 1; }
JNIEXPORT jintArray JNICALL JNI_NAME(requirements)(JNIEnv* env, jclass,
    jlong, jlong, jstring, jstring) {
  jint values[] = {0, 0};
  jintArray result = env->NewIntArray(2);
  env->SetIntArrayRegion(result, 0, 2, values);
  return result;
}
JNIEXPORT jlong JNICALL JNI_NAME(open)(JNIEnv* env, jclass, jlong, jlong,
    jlong, jlong, jlong, jstring, jstring) {
  if (openContext) { fail(env, "Synthetic context already open"); return 0; }
  openContext = true;
  return 1;
}
JNIEXPORT jintArray JNICALL JNI_NAME(optimalSettings)(JNIEnv* env, jclass,
    jlong, jint width, jint height, jint) {
  jint values[] = {width / 2, height / 2, width / 2, height / 2, width / 2, height / 2};
  jintArray result = env->NewIntArray(6);
  env->SetIntArrayRegion(result, 0, 6, values);
  return result;
}
JNIEXPORT void JNICALL JNI_NAME(create)(JNIEnv* env, jclass, jlong, jlong,
    jint, jint, jint, jint, jint quality, jboolean, jboolean) {
  frames = 0;
  if (quality == 0) fail(env, "Injected feature creation failure");
}
JNIEXPORT void JNICALL JNI_NAME(evaluate)(JNIEnv* env, jclass, jlong, jlong,
    jlongArray, jintArray, jfloat, jfloat, jfloat, jfloat,
    jfloat frameTime, jfloat, jboolean reset) {
  if (frames < 3 && static_cast<bool>(reset) != (frames == 0 || frames == 2)) {
    fail(env, "Incorrect Java temporal history reset state");
    return;
  }
  ++frames;
  if (frameTime == 17.0f && frames == 4) fail(env, "Injected feature evaluation failure");
  // Deliberately succeed on a retry: the Java failed-session latch must prevent that call.
}
JNIEXPORT void JNICALL JNI_NAME(close)(JNIEnv* env, jclass, jlong) {
  if (!openContext) { fail(env, "Synthetic context closed twice"); return; }
  openContext = false;
}
}
