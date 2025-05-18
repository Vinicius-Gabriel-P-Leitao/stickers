//
// Created by vinicius on 13/05/2025.
//

#ifndef ANDROID_SWSCONTEXTDELETER_H
#define ANDROID_SWSCONTEXTDELETER_H

#include <memory>

extern "C" {
#include "libswscale/swscale.h"
}

struct SwsContextDeleter {
    void operator()(SwsContext *swsContext) const {
        sws_freeContext(swsContext);
    }
};

using SwsContextPtr = std::unique_ptr<SwsContext, SwsContextDeleter>;

#endif //ANDROID_SWSCONTEXTDELETER_H
