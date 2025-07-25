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

#include "service/WebpAnimationConverter.hpp"
#include "service/ProcessInputMedia.hpp"

#include "exception/HandlerJavaException.hpp"

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

#define LOG_TAG_RESIZE_CROP "NativeStickerCrop"
#define LOG_TAG_FFMPEG "NativeFFmpeg"

#define LOGIF(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG_FFMPEG, __VA_ARGS__)
#define LOGDF(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG_FFMPEG, __VA_ARGS__)

#define DURATION_MS  100
#define OUTPUT_SIZE  512

struct JniString {
    JNIEnv *env;
    jstring jstr;
    const char *cstr;

    JniString(JNIEnv *env, jstring pJstring) : env(env), jstr(pJstring), cstr(nullptr) {
        if (pJstring) {
            cstr = env->GetStringUTFChars(pJstring, nullptr);
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
JNIEXPORT jboolean JNICALL Java_br_arch_sticker_core_lib_NativeCropMedia_cropMedia(
        JNIEnv *env, jobject /* this */, jstring inputPath, jstring outputPath, jint x, jint y, jint width, jint height) {
    jclass nativeMediaException = env->FindClass("br/arch/sticker/core/error/throwable/media/NativeConversionException");

    JniString inPath(env, inputPath);
    JniString outPath(env, outputPath);

    try {
        ProcessInputMedia pProcessInputMedia(env, nativeMediaException);
        ProcessFramesToFormat processFramesToFormat(env, nativeMediaException);

        ParamsMap params = {
                {"cropX",      static_cast<int>(x)},
                {"cropY",      static_cast<int>(y)},
                {"cropWidth",  static_cast<int>(width)},
                {"cropHeight", static_cast<int>(height)}
        };

        auto frameProcessor = [&processFramesToFormat](
                JNIEnv *env, jclass clazz, AVFramePtr &frame, int width, int height, std::vector<FrameWithBuffer> &buffers, const ParamsMap &params) {

            int cropX = params.count("cropX") ? std::any_cast<int>(params.at("cropX")) : 0;
            int cropY = params.count("cropY") ? std::any_cast<int>(params.at("cropY")) : 0;
            int cropWidth = params.count("cropWidth") ? std::any_cast<int>(params.at("cropWidth")) : width;
            int cropHeight = params.count("cropHeight") ? std::any_cast<int>(params.at("cropHeight")) : height;

            processFramesToFormat.processFrame(frame, cropX, cropY, cropWidth, cropHeight, buffers);
        };

        std::vector<FrameWithBuffer> vFramesWithBuffer = pProcessInputMedia.processVideoFrames(inPath.get(), outPath.get(), frameProcessor);

        if (!vFramesWithBuffer.empty()) {
            LOGDF("Gerando animação com %zu vFrameBuffer...", vFramesWithBuffer.size());

            if (vFramesWithBuffer.size() < 2) {
                LOGIF("Apenas %zu frame(s) capturado(s) — a animação pode parecer estática",
                      vFramesWithBuffer.size());
            }

            int result = WebpAnimationConverter::convertToWebp(
                    env, outPath.get(), vFramesWithBuffer, OUTPUT_SIZE, OUTPUT_SIZE, DURATION_MS, 10, 0);

            if (!result) {
                std::string msgError = fmt::format("Falha ao criar a animação WebP.");
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