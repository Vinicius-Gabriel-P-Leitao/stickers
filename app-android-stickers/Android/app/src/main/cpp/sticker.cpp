#include <jni.h>
#include <android/log.h>
#include <iostream>

extern "C" {
#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libavutil/avutil.h>
#include <libswscale/swscale.h>
#include <libswresample/swresample.h>
#include <encode.h>
#include <decode.h>
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_vinicius_sticker_domain_libs_ConvertToWebp_convertToWebp(JNIEnv *env, jobject /* this */,
                                                                  jstring inputPath,
                                                                  jstring outputPath) {
    const char *inPath = env->GetStringUTFChars(inputPath, nullptr);
    const char *outPath = env->GetStringUTFChars(outputPath, nullptr);

    if (inPath == nullptr || outPath == nullptr) {
        if (inPath) env->ReleaseStringUTFChars(inputPath, inPath);
        if (outPath) env->ReleaseStringUTFChars(outputPath, outPath);
        return env->NewStringUTF("Invalid input or output path");
    }

    unsigned int versionFfmpeg = avformat_version();
    char version_str[64];
    snprintf(version_str, sizeof(version_str), ""
                   "\n libavformat version: %u", versionFfmpeg);

    unsigned int webp_version = WebPGetDecoderVersion();
    snprintf(version_str + strlen(version_str), sizeof(version_str) - strlen(version_str),
             "\n libwebp version: %u\n", webp_version);

    env->ReleaseStringUTFChars(inputPath, inPath);
    env->ReleaseStringUTFChars(outputPath, outPath);

    return env->NewStringUTF(version_str);
}
