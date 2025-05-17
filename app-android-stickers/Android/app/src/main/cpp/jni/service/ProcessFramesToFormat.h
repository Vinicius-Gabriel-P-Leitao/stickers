//
// Created by vinicius on 17/05/2025.
//

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
