/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */


#ifndef ANDROID_AVCODECCONTEXTDELETER_H
#define ANDROID_AVCODECCONTEXTDELETER_H

#include <memory>

extern "C" {
#include "libavcodec/avcodec.h"
}

struct AVCodecContextDeleter {
    void operator()(AVCodecContext *ctx) const {
        avcodec_free_context(&ctx);
    }
};

using AVCodecContextPtr = std::unique_ptr<AVCodecContext, AVCodecContextDeleter>;

#endif //ANDROID_AVCODECCONTEXTDELETER_H
