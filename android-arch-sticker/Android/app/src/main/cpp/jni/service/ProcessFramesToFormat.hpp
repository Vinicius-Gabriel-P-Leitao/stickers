/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

#ifndef ANDROID_PROCESSFRAMESTOFORMAT_HPP
#define ANDROID_PROCESSFRAMESTOFORMAT_HPP

#include <jni.h>

#include "../raii/AVFrameDestroyer.hpp"
#include "../raii/AVBufferDestroyer.hpp"

class ProcessFramesToFormat;

class FrameWithBuffer {
public:
    AVFramePtr frame;
    AVBufferPtr buffer;

    bool allocateFrameWithBuffer(int width, int height, AVPixelFormat format);
};

class ProcessFramesToFormat {
public:
    ProcessFramesToFormat();

    static AVFramePtr createAvFrame(int width, int height, AVPixelFormat format);

    static void processFrame(AVFramePtr &rgbFrame, int cropX, int cropY, int width, int height, std::vector<FrameWithBuffer> &frames);

private:
    static bool cropFrame(const AVFramePtr &srcFrame, FrameWithBuffer &dstFrame, int cropX, int cropY, int cropWidth, int cropHeight);
};

#endif //ANDROID_PROCESSFRAMESTOFORMAT_HPP
