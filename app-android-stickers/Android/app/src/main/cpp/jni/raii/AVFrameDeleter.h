//
// Created by vinicius on 13/05/2025.
//

#ifndef ANDROID_AVFRAMEDELETER_H
#define ANDROID_AVFRAMEDELETER_H

#include <string>

extern "C" {
#include "libavutil/frame.h"
}

class AVFrameDeleter {
public:
    void operator()(AVFrame *frame) const {
        av_frame_free(&frame);
    }
};

using AVFramePtr = std::unique_ptr<AVFrame, AVFrameDeleter>;

#endif //ANDROID_AVFRAMEDELETER_H
