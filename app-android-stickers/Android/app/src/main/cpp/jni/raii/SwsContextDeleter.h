//
// Created by vinicius on 13/05/2025.
//

#ifndef ANDROID_SWSCONTEXTDELETER_H
#define ANDROID_SWSCONTEXTDELETER_H

#include <string>

extern "C" {
#include "libswscale/swscale.h"
}

struct SwsContextDeleter {
    void operator()(SwsContext *ctx) const {
        sws_freeContext(ctx);
    }
};

using SwsContextPtr = std::unique_ptr<SwsContext, SwsContextDeleter>;

#endif //ANDROID_SWSCONTEXTDELETER_H
