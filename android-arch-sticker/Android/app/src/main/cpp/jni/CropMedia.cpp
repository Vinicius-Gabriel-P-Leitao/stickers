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
#include "service/ProcessInputMedia.hpp"

#define LOG_TAG_RESIZE_CROP "NativeStickerCrop"
#define LOG_TAG_FFMPEG "NativeFFmpeg"

#define LOGEN(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG_RESIZE_CROP, __VA_ARGS__)
#define LOGIF(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG_FFMPEG, __VA_ARGS__)
#define LOGDF(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG_FFMPEG, __VA_ARGS__)

#define DURATION_MS  100
#define OUTPUT_SIZE  512

struct JniString {
    JNIEnv *env;
    jstring jString;
    const char *charString;

    JniString(JNIEnv *env, jstring pString) : env(env), jString(pString), charString(nullptr) {
        if (pString) {
            charString = env->GetStringUTFChars(pString, nullptr);
        }
    }

    ~JniString() {
        if (charString) {
            env->ReleaseStringUTFChars(jString, charString);
        }
    }

    [[nodiscard]] const char *get() const { return charString; }
};

extern "C"
JNIEXPORT jboolean JNICALL Java_br_arch_sticker_core_lib_NativeCropMedia_cropMedia(
        JNIEnv *env, jobject /* this */,
        jstring inputPath, jstring outputPath, jint x, jint y, jint width, jint height, jfloat startSeconds, jfloat endSeconds) {
    jclass nativeMediaException = env->FindClass("br/arch/sticker/core/error/throwable/media/NativeConversionException");

    JniString inPath(env, inputPath);
    JniString outPath(env, outputPath);

    try {
        std::vector<FrameWithBuffer> vFramesWithBuffer = ProcessInputMedia::processVideoFrames(
                inPath.get(), outPath.get(), startSeconds, endSeconds,
                [](AVFramePtr &frame, std::vector<FrameWithBuffer> &buffers, const ParamsMap &params) {

                    int cropX = params.count("cropX") ? std::any_cast<int>(params.at("cropX")) : 0;
                    int cropY = params.count("cropY") ? std::any_cast<int>(params.at("cropY")) : 0;
                    int cropWidth = params.count("cropWidth") ? std::any_cast<int>(params.at("cropWidth")) : frame->width;
                    int cropHeight = params.count("cropHeight") ? std::any_cast<int>(params.at("cropHeight")) : frame->height;

                    ProcessFramesToFormat::processFrame(frame, cropX, cropY, cropWidth, cropHeight, buffers);
                }, {
                        {"cropX",      static_cast<int>(x)},
                        {"cropY",      static_cast<int>(y)},
                        {"cropWidth",  static_cast<int>(width)},
                        {"cropHeight", static_cast<int>(height)},
                }
        );

        if (!vFramesWithBuffer.empty()) {
            LOGDF("%s", fmt::format("Gerando animação com {} vFrameBuffer...", vFramesWithBuffer.size()).c_str());

            if (vFramesWithBuffer.size() < 2) {
                LOGIF("%s", fmt::format("Apenas {} frame(s) capturado(s) — a animação pode parecer estática", vFramesWithBuffer.size()).c_str());
            }

            int result = WebpAnimationConverter::convertToWebp(outPath.get(), vFramesWithBuffer, OUTPUT_SIZE, OUTPUT_SIZE, DURATION_MS, 10, 0);

            if (!result) {
                HandlerJavaException::throwNativeConversionException(
                        env, nativeMediaException, "Falha ao criar a animação WebP.");

                return JNI_FALSE;
            }

            LOGDF("%s", fmt::format("Animação WebP criada com sucesso: {}", outPath.get()).c_str());
        } else {
            HandlerJavaException::throwNativeConversionException(env, nativeMediaException, "Nenhum frame capturado para criar a animação");
            return JNI_FALSE;
        }

        return JNI_TRUE;
    } catch (const std::exception &exception) {
        LOGEN("%s", fmt::format("Erro ao realizar crop nativo: \n {}", exception.what()).c_str());
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, exception.what());
        return JNI_FALSE;
    }
}