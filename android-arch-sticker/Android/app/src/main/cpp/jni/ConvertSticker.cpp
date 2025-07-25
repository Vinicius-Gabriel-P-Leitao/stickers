/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

#include <jni.h>
#include <cmath>
#include <vector>
#include <memory>
#include <string>
#include <base.h>
#include <format.h>
#include <android/log.h>

#include "exception/HandlerJavaException.hpp"

#include "service/WebpAnimationConverter.hpp"
#include "service/ProcessFramesToFormat.hpp"
#include "service/ProcessWebpToAvFrames.hpp"
#include "service/ProcessInputMedia.hpp"

#include "raii/AVFrameDestroyer.hpp"
#include "raii/AVBufferDestroyer.hpp"
#include "raii/SwsContextDestroyer.hpp"
#include "raii/AVCodecContextDestroyer.hpp"
#include "raii/AVFormatContextDestroyer.hpp"

extern "C" {
#include "mux.h"
#include "decode.h"
#include "encode.h"
#include "libavutil/frame.h"
#include "libavutil/avutil.h"
#include "libavutil/imgutils.h"
#include "libavcodec/avcodec.h"
#include "libswscale/swscale.h"
#include "libavformat/avformat.h"
#include "libswresample/swresample.h"
}

#define LOG_TAG_RESIZE_CROP "NativeStickerConverter"
#define LOG_TAG_FFMPEG "NativeFFmpeg"

#define LOGIF(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG_FFMPEG, __VA_ARGS__)
#define LOGDF(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG_FFMPEG, __VA_ARGS__)

#define DURATION_MS  100
#define OUTPUT_SIZE  512

struct JniString {
    JNIEnv *env;
    jstring jstr;
    const char *cstr;

    JniString(JNIEnv *env, jstring jstr) : env(env), jstr(jstr), cstr(nullptr) {
        if (jstr) {
            cstr = env->GetStringUTFChars(jstr, nullptr);
        }
    }

    ~JniString() {
        if (cstr) {
            env->ReleaseStringUTFChars(jstr, cstr);
        }
    }

    [[nodiscard]] const char *get() const { return cstr; }
};

extern "C"
JNIEXPORT jboolean JNICALL Java_br_arch_sticker_core_lib_NativeProcessWebp_convertToWebp(
        JNIEnv *env, jobject /* this */, jstring inputPath, jstring outputPath, jfloat quality, jboolean lossless) {
    jclass nativeMediaException = env->FindClass("br/arch/sticker/core/error/throwable/media/NativeConversionException");

    JniString inPath(env, inputPath);
    JniString outPath(env, outputPath);
    int lossless_flag = (lossless == JNI_TRUE) ? 1 : 0;

    bool isWebP = false;

    {
        FILE *file = fopen(inPath.get(), "rb");
        if (file) {
            uint8_t header[12];
            fread(header, 1, 12, file);
            fclose(file);
            if (memcmp(header, "RIFF", 4) == 0 && memcmp(header + 8, "WEBP", 4) == 0) {
                isWebP = true;
            }
        }
    }

    if (isWebP) {
        std::vector<FrameWithBuffer> vFramesWithBuffer;

        if (!ProcessWebpToAvFrames::decodeWebPAsAVFrames(env, inPath.get(), vFramesWithBuffer, OUTPUT_SIZE, OUTPUT_SIZE)) {
            HandlerJavaException::throwNativeConversionException(env, nativeMediaException, "Erro ao processar arquivo WebP");
            return JNI_FALSE;
        }

        if (vFramesWithBuffer.empty()) {
            HandlerJavaException::throwNativeConversionException(env, nativeMediaException, "Nenhum frame extraído do WebP");
            return JNI_FALSE;
        }

        int result = WebpAnimationConverter::convertToWebp(
                env, outPath.get(), vFramesWithBuffer, OUTPUT_SIZE, OUTPUT_SIZE, DURATION_MS, quality, lossless_flag);
        return result ? JNI_TRUE : JNI_FALSE;
    }

    try {
        ProcessInputMedia pProcessInputMedia(env, nativeMediaException);
        ProcessFramesToFormat processFramesToFormat(env, nativeMediaException);

        auto frameProcessor = [&processFramesToFormat](
                JNIEnv *env, jclass clazz, AVFramePtr &frame, int width, int height, std::vector<FrameWithBuffer> &buffers, const ParamsMap &params) {
            processFramesToFormat.processFrame(frame, -1, -1, width, height, buffers);
        };

        std::vector<FrameWithBuffer> vFramesWithBuffer = pProcessInputMedia.processVideoFrames(inPath.get(), outPath.get(), frameProcessor);

        if (!vFramesWithBuffer.empty()) {
            LOGDF("Gerando animação com %zu vFrameBuffer...", vFramesWithBuffer.size());

            if (vFramesWithBuffer.size() < 2) {
                LOGIF("Apenas %zu frame(s) capturado(s) — a animação pode parecer estática",
                      vFramesWithBuffer.size());
            }

            int result = WebpAnimationConverter::convertToWebp(
                    env, outPath.get(), vFramesWithBuffer, OUTPUT_SIZE, OUTPUT_SIZE, DURATION_MS, quality, lossless_flag);

            if (!result) {
                std::string msgError = fmt::format("Falha ao criar a animação WebP");
                HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

                return JNI_FALSE;
            }
            LOGDF("Animação WebP criada com sucesso: %s", outPath.get());
        } else {
            std::string msgError = fmt::format("Nenhum frame capturado para criar a animação");
            HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

            return JNI_FALSE;
        }

        return JNI_TRUE;
    } catch (const std::exception &exception) {
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, exception.what());
        return JNI_FALSE;
    }
}