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

struct FrameWithBuffer {
    AVFramePtr frame;
    AVBufferPtr buffer;

    bool allocate(JNIEnv *env, jclass exClass, int width, int height, AVPixelFormat format);
};

class ProcessFramesToFormat {
public:
    static AVFramePtr createAvFrame(JNIEnv *env, jclass exClass, int width, int height, AVPixelFormat format);

    static void processFrame(JNIEnv *env, jclass exClass, AVFramePtr &rgbFrame, int width, int height, std::vector<FrameWithBuffer> &frames);

private:
    static bool
    cropFrame(JNIEnv *env, jclass exClass, const AVFramePtr &srcFrame, FrameWithBuffer &dstFrame, int targetWidth, int targetHeight);
};


#endif //ANDROID_PROCESSFRAMESTOFORMAT_HPP
