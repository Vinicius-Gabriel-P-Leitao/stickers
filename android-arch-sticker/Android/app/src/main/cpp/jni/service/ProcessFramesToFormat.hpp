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

    bool allocate(ProcessFramesToFormat &processor, int width, int height, AVPixelFormat format);
};

class ProcessFramesToFormat {
public:
    ProcessFramesToFormat(JNIEnv *env, jclass nativeMediaException);

    AVFramePtr createAvFrame(int width, int height, AVPixelFormat format);

    void processFrame(AVFramePtr &rgbFrame, int width, int height, std::vector<FrameWithBuffer> &frames);

private:
    JNIEnv *env;
    jclass nativeMediaException;

    bool cropFrame(const AVFramePtr &srcFrame, FrameWithBuffer &dstFrame, int targetWidth, int targetHeight);
};

#endif //ANDROID_PROCESSFRAMESTOFORMAT_HPP
