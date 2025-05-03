#include <jni.h>
#include <android/log.h>

#include <libavformat/avformat.h>
#include <libavcodec/avcodec.h>
#include <libavutil/avutil.h>
#include <libswscale/swscale.h>
#include <libswresample/swresample.h>

#define LOG_TAG "FFmpegJNI"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_vinicius_sticker_domain_libs_ConvertToWebp_convertToWebp(JNIEnv *env, jobject /* this */,
                                                                  jstring inputPath,
                                                                  jstring outputPath) {
    const char *inPath = env->GetStringUTFChars(inputPath, nullptr);
    const char *outPath = env->GetStringUTFChars(outputPath, nullptr);

    unsigned version = avformat_version();
    LOGD("Versão do FFmpeg: %u", version);

    LOGD("Conversão concluída: %s -> %s", inPath, outPath);
    return 0;
}