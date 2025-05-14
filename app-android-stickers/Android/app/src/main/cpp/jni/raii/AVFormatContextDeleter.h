//
// Created by vinicius on 13/05/2025.
//

#ifndef ANDROID_AVFORMATCONTEXTDELETER_H
#define ANDROID_AVFORMATCONTEXTDELETER_H

#include <string>

extern "C" {
#include "libavformat/avformat.h"
}

struct AVFormatContextDeleter {
    void operator()(AVFormatContext* ctx) const {
        avformat_close_input(&ctx);
    }
};

using AVFormatContextPtr = std::unique_ptr<AVFormatContext, AVFormatContextDeleter>;

#endif //ANDROID_AVFORMATCONTEXTDELETER_H
