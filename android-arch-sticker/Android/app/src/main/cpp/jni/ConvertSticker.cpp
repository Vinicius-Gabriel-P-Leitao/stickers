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

#define LOG_TAG_RESIZE_CROP "NativeStickerConverter"
#define LOG_TAG_FFMPEG "NativeFFmpeg"

#define LOGEN(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG_FFMPEG, __VA_ARGS__)
#define LOGIF(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG_FFMPEG, __VA_ARGS__)
#define LOGDF(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG_FFMPEG, __VA_ARGS__)

#define DURATION_MS  100
#define OUTPUT_SIZE  512

struct JniString {
    JNIEnv *env;
    jstring jString;
    const char *charString;

    JniString(JNIEnv *env, jstring jstr) : env(env), jString(jstr), charString(nullptr) {
        if (jstr) {
            charString = env->GetStringUTFChars(jstr, nullptr);
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
JNIEXPORT jboolean JNICALL Java_br_arch_sticker_core_lib_NativeProcessWebp_convertToWebp(
        JNIEnv *env, jobject /* this */, jstring inputPath, jstring outputPath, jfloat quality, jboolean lossless) {
    jclass nativeMediaException = env->FindClass(
            "br/arch/sticker/core/error/throwable/media/NativeConversionException");

    JniString inPath(env, inputPath);
    JniString outPath(env, outputPath);
    int losslessFlag = (lossless == JNI_TRUE) ? 1 : 0;

    bool isWebP = false;
    FILE *file = fopen(inPath.get(), "rb");
    if (file) {
        uint8_t header[12];
        fread(header, 1, 12, file);
        fclose(file);
        if (memcmp(header, "RIFF", 4) == 0 && memcmp(header + 8, "WEBP", 4) == 0) {
            isWebP = true;
        }
    }

    try {
        if (isWebP) {
            std::vector<FrameWithBuffer> vFramesWithBuffer;

            if (!ProcessWebpToAvFrames::decodeWebPAsAVFrames(inPath.get(), vFramesWithBuffer, OUTPUT_SIZE,
                                                             OUTPUT_SIZE)) {
                HandlerJavaException::throwNativeConversionException(env, nativeMediaException,
                                                                     "Erro ao processar arquivo WebP");
                return JNI_FALSE;
            }

            if (vFramesWithBuffer.empty()) {
                HandlerJavaException::throwNativeConversionException(env, nativeMediaException,
                                                                     "Nenhum frame extraído do WebP");
                return JNI_FALSE;
            }

            int result = WebpAnimationConverter::convertToWebp(
                    outPath.get(), vFramesWithBuffer, OUTPUT_SIZE, OUTPUT_SIZE, DURATION_MS, quality, losslessFlag);
            return result ? JNI_TRUE : JNI_FALSE;
        }

        auto frameProcessor = [](
                AVFramePtr &frame, std::vector<FrameWithBuffer> &buffers, const ParamsMap &params) {
            ProcessFramesToFormat::processFrame(frame, 0, 0, frame->width, frame->height, buffers);
        };

        std::vector<FrameWithBuffer> vFramesWithBuffer = ProcessInputMedia::processVideoFrames(
                inPath.get(), outPath.get(), 0, 5, frameProcessor);

        if (!vFramesWithBuffer.empty()) {
            LOGDF("%s", fmt::format("Gerando animação com {} vFrameBuffer...", vFramesWithBuffer.size()).c_str());

            if (vFramesWithBuffer.size() < 2) {
                LOGIF("%s", fmt::format("Apenas {} frame(s) capturado(s) — a animação pode parecer estática",
                                        vFramesWithBuffer.size()).c_str());
            }

            int result = WebpAnimationConverter::convertToWebp(
                    outPath.get(), vFramesWithBuffer, OUTPUT_SIZE, OUTPUT_SIZE, DURATION_MS, quality, losslessFlag);

            if (!result) {
                HandlerJavaException::throwNativeConversionException(env, nativeMediaException,
                                                                     "Falha ao criar a animação WebP.");
                return JNI_FALSE;
            }

            LOGDF("%s", fmt::format("Animação WebP criada com sucesso: {}", outPath.get()).c_str());
        } else {
            HandlerJavaException::throwNativeConversionException(env, nativeMediaException,
                                                                 "Nenhum frame capturado para criar a animação");
            return JNI_FALSE;
        }

        return JNI_TRUE;
    } catch (const std::exception &exception) {
        LOGEN("%s", fmt::format("Erro ao realizar conversão nativo: \n {}", exception.what()).c_str());
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, exception.what());
        return JNI_FALSE;
    }
}