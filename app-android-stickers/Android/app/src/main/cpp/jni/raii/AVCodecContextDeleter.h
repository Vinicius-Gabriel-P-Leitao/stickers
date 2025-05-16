//
// Created by vinicius on 13/05/2025.
//

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
