//
// Created by vinicius on 13/05/2025.
//

#ifndef ANDROID_AVBUFFERDELETER_H
#define ANDROID_AVBUFFERDELETER_H

#include <string>

extern "C" {
#include "libavutil/avutil.h"
}

struct AVBufferDeleter {
    void operator()(void *ptr) const {
        av_free(ptr);
    }
};

using AVBufferPtr = std::unique_ptr<void, AVBufferDeleter>;

#endif //ANDROID_AVBUFFERDELETER_H
