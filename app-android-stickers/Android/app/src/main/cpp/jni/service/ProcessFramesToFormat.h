/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */

#ifndef ANDROID_PROCESSFRAMESTOFORMAT_H
#define ANDROID_PROCESSFRAMESTOFORMAT_H

#include <jni.h>

#include "../raii/AVFrameDeleter.h"
#include "../raii/AVBufferDeleter.h"

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


#endif //ANDROID_PROCESSFRAMESTOFORMAT_H
