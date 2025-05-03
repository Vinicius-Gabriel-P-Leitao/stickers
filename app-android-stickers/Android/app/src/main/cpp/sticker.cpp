#include <jni.h>
#include <android/log.h>

extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libavutil/avutil.h>
#include <libswscale/swscale.h>
#include <libswresample/swresample.h>
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_vinicius_sticker_domain_libs_ConvertToWebp_convertToWebp(JNIEnv *env, jobject /* this */,
                                                                  jstring inputPath,
                                                                  jstring outputPath) {
    const char *inPath = env->GetStringUTFChars(inputPath, nullptr);
    const char *outPath = env->GetStringUTFChars(outputPath, nullptr);

    unsigned int version = avformat_version();
    char version_str[32];
    snprintf(version_str, sizeof(version_str), "libavformat version: %u", version);
    return env->NewStringUTF(version_str);
}