/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */
#include "WebpAnimationConverter.h"
#include "format.h"

#include <jni.h>
#include <vector>
#include <memory>
#include <string>
#include <iostream>
#include <android/log.h>

#include "../exception/HandlerJavaException.h"

extern "C" {
#include "mux.h"
#include "decode.h"
#include "encode.h"
#include "libswresample/swresample.h"
}

#define LOG_TAG_SERVICE "WebPAnimationConverter"
#define LOGEST(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG_SERVICE, __VA_ARGS__)
#define LOGDF(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG_SERVICE, __VA_ARGS__)
#define LOGINF(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG_SERVICE, __VA_ARGS__)

// RAII para WebPAnimEncoder
struct WebPAnimEncoderDeleter {
    void operator()(WebPAnimEncoder *enc) const {
        WebPAnimEncoderDelete(enc);
    }
};

using WebPAnimEncoderPtr = std::unique_ptr<WebPAnimEncoder, WebPAnimEncoderDeleter>;

// RAII para WebPData
struct WebPDataDeleter {
    void operator()(WebPData *data) const {
        WebPDataClear(data);
    }
};

using WebPDataPtr = std::unique_ptr<WebPData, WebPDataDeleter>;

// RAII para frame
struct AVFrameDeleter {
    void operator()(AVFrame *frame) const {
        av_frame_free(&frame);
    }
};

// RAII para buffer
struct AVBufferDeleter {
    void operator()(void *ptr) const {
        av_free(ptr);
    }
};

using AVBufferPtr = std::unique_ptr<void, AVBufferDeleter>;
using AVFramePtr = std::unique_ptr<AVFrame, AVFrameDeleter>;

// RAII para AVBufferRef para referencia dos vFrameBuffer e buffers
struct FrameWithBuffer {
    AVFramePtr frame;
    AVBufferPtr buffer;
};

int
WebpAnimationConverter::convertToWebp(JNIEnv *env,
                                      const char *outputPath,
                                      std::vector<FrameWithBuffer> &frames,
                                      int width,
                                      int height,
                                      int durationMs) {
    jclass nativeMediaException = env->FindClass("com/vinicius/sticker/core/exception/NativeConversionException");

    WebPAnimEncoderOptions encOptions;
    if (!WebPAnimEncoderOptionsInit(&encOptions)) {
        std::string msgError = fmt::format("Falha ao inicializar WebPAnimEncoderOptions");
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return 0;
    }

    // Definindo opções de animação
    WebPAnimEncoderOptionsInit(&encOptions);
    encOptions.minimize_size = 1;

    WebPConfig webPConfig;
    if (!WebPConfigInit(&webPConfig)) {
        std::string msgError = fmt::format("Falha ao inicializar WebPConfig");
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return 0;
    }

    webPConfig.lossless = 0;
    webPConfig.quality = 20.0f;
    webPConfig.method = 2;
    webPConfig.filter_strength = 70;
    webPConfig.preprocessing = 2;

    LOGINF("Criando encoder");

    WebPAnimEncoderPtr encoder(WebPAnimEncoderNew(width, height, &encOptions));
    if (!encoder) {
        std::string msgError = fmt::format("Erro ao criar WebPAnimEncoder");
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return 0;
    }

    int timestampMs = 0;
    for (const auto &frameWithBuffer: frames) {
        const AVFramePtr &frame = frameWithBuffer.frame;
        LOGDF("Adicionando frame ao encoder, timestamp: %d ms", timestampMs);

        WebPPicture webPPicture;
        if (!WebPPictureInit(&webPPicture)) {
            std::string msgError = fmt::format("Erro ao inicializar WebPPicture");
            HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

            return 0;
        }
        webPPicture.width = width;
        webPPicture.height = height;
        webPPicture.use_argb = 1;

        if (!WebPPictureImportRGB(&webPPicture, frame->data[0], frame->linesize[0])) {
            std::string msgError = fmt::format("Erro ao importar dados RGB");
            HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

            WebPPictureFree(&webPPicture);
            return 0;
        }

        if (!WebPAnimEncoderAdd(encoder.get(), &webPPicture, timestampMs, &webPConfig)) {
            std::string msgError = fmt::format("Erro ao adicionar frame ao encoder: %s", WebPAnimEncoderGetError(encoder.get()));
            HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

            WebPPictureFree(&webPPicture);
            return 0;
        }

        WebPPictureFree(&webPPicture);
        timestampMs += durationMs;
    }

    if (!WebPAnimEncoderAdd(encoder.get(), nullptr, timestampMs, nullptr)) {
        std::string msgError = fmt::format("Erro ao finalizar vFrameBuffer: %s", WebPAnimEncoderGetError(encoder.get()));
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return 0;
    }

    WebPData webpData;
    WebPDataInit(&webpData);

    WebPDataPtr webp_data_ptr(&webpData);
    if (!WebPAnimEncoderAssemble(encoder.get(), &webpData)) {
        std::string msgError = fmt::format("Erro ao montar animação: %s", WebPAnimEncoderGetError(encoder.get()));
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return 0;
    }

    FILE *pFile = fopen(outputPath, "wb");
    if (!pFile) {
        std::string msgError = fmt::format("Erro ao abrir arquivo de saída: %s", outputPath);
        HandlerJavaException::throwNativeConversionException(env, nativeMediaException, msgError);

        return 0;
    }

    fwrite(webpData.bytes, 1, webpData.size, pFile);
    fclose(pFile);

    LOGINF("Arquivo WebP salvo com sucesso em: %s (%zu bytes)", outputPath, webpData.size);
    return 1;
}

