#include <jni.h>

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("sticker");
//    }
#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "ConvertMediaWebpCPP"

extern "C"
JNIEXPORT jstring JNICALL
Java_com_vinicius_sticker_presentation_feature_media_util_ConvertMediaToStickerFormat_00024ConvertToWebp_convertToWebp(
        JNIEnv *env, jobject thiz, jstring inputPath) {

    const char *nativeInputPath = env->GetStringUTFChars(inputPath, nullptr);
    std::string inputFilePath(nativeInputPath);
    env->ReleaseStringUTFChars(inputPath, nativeInputPath);

    std::string output = "Caminho do arquivo de entrada: " + inputFilePath;

    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "Input path recebido: %s", nativeInputPath);

    return env->NewStringUTF(output.c_str());
}