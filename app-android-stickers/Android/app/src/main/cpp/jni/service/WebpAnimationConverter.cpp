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

#include <jni.h>
#include <vector>
#include <memory>
#include <string>
#include <iostream>
#include <android/log.h>

extern "C" {
#include "libswresample/swresample.h"
#include "decode.h"
#include "encode.h"
#include "mux.h"
}


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

// RAII para AVBufferRef para referencia dos frames e buffers
struct FrameWithBuffer {
    AVFramePtr frame;
    AVBufferPtr buffer;
};

int
WebpAnimationConverter::convertToWebp(const char *outputPath,
                                      std::vector<FrameWithBuffer> &frames,
                                      int width,
                                      int height,
                                      int durationMs) {
    WebPAnimEncoderOptions encOptions;
    if (!WebPAnimEncoderOptionsInit(&encOptions)) {
        __android_log_print(ANDROID_LOG_ERROR, "WebP",
                            "Falha ao inicializar WebPAnimEncoderOptions");
        return 0;
    }

    // Definindo opções de animação
    WebPAnimEncoderOptionsInit(&encOptions);
    encOptions.minimize_size = 1;

    WebPConfig webPConfig;
    if (!WebPConfigInit(&webPConfig)) {
        __android_log_print(ANDROID_LOG_ERROR, "WebP", "Falha ao inicializar WebPConfig");
        return 0;
    }

    webPConfig.lossless = 0;
    webPConfig.quality = 10.0f;
    webPConfig.method = 2;
    webPConfig.filter_strength = 70;
    webPConfig.segments = 4;
    webPConfig.preprocessing = 2;

    WebPAnimEncoderPtr encoder(WebPAnimEncoderNew(width, height, &encOptions));
    if (!encoder) {
        __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao criar WebPAnimEncoder");
        return 0;
    }

    int timestampMs = 0;
    for (const auto &frameWithBuffer: frames) {
        const AVFramePtr &frame = frameWithBuffer.frame;

        __android_log_print(ANDROID_LOG_DEBUG,
                            "WebP", "Adicionando frame ao encoder, timestamp: %d ms",
                            timestampMs);

        WebPPicture webPPicture;
        if (!WebPPictureInit(&webPPicture)) {
            __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao inicializar WebPPicture");
            return 0;
        }
        webPPicture.width = width;
        webPPicture.height = height;
        webPPicture.use_argb = 1;

        if (!WebPPictureImportRGB(&webPPicture, frame->data[0], frame->linesize[0])) {
            __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao importar dados RGB");
            WebPPictureFree(&webPPicture);
            return 0;
        }

        if (!WebPAnimEncoderAdd(encoder.get(), &webPPicture, timestampMs, &webPConfig)) {
            __android_log_print(ANDROID_LOG_ERROR,
                                "WebP", "Erro ao adicionar frame ao encoder: %s",
                                WebPAnimEncoderGetError(encoder.get()));
            WebPPictureFree(&webPPicture);
            return 0;
        }

        WebPPictureFree(&webPPicture);
        timestampMs += durationMs;
    }

    if (!WebPAnimEncoderAdd(encoder.get(), nullptr, timestampMs, nullptr)) {
        __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao finalizar frames");
        return 0;
    }

    WebPData webpData;
    WebPDataInit(&webpData);

    WebPDataPtr webp_data_ptr(&webpData);
    if (!WebPAnimEncoderAssemble(encoder.get(), &webpData)) {
        __android_log_print(ANDROID_LOG_ERROR, "WebP", "Erro ao montar animação");
        return 0;
    }

    FILE *pFile = fopen(outputPath, "wb");
    if (!pFile) {
        __android_log_print(ANDROID_LOG_ERROR,
                            "WebP", "Erro ao abrir arquivo de saída: %s",
                            outputPath);
        return 0;
    }

    fwrite(webpData.bytes, 1, webpData.size, pFile);
    fclose(pFile);
    __android_log_print(ANDROID_LOG_INFO,
                        "WebP", "Arquivo WebP salvo com sucesso em: %s (%zu bytes)", outputPath,
                        webpData.size);

    return 1;
}

