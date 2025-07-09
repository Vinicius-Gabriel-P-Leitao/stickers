/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */


#ifndef ANDROID_AVFRAMEDESTROYER_HPP
#define ANDROID_AVFRAMEDESTROYER_HPP

#include <memory>

extern "C" {
#include "libavutil/frame.h"
}

class AVFrameDestroyer {
public:
    void operator()(AVFrame *frame) const {
        av_frame_free(&frame);
    }
};

using AVFramePtr = std::unique_ptr<AVFrame, AVFrameDestroyer>;

#endif //ANDROID_AVFRAMEDESTROYER_HPP
