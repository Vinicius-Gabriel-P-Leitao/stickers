//
// Created by vinicius on 17/05/2025.
//

#ifndef ANDROID_RESIZECROPFRAME_H
#define ANDROID_RESIZECROPFRAME_H

#include <jni.h>

#include "../raii/AVFrameDeleter.h"
#include "../raii/AVBufferDeleter.h"

struct FrameWithBuffer {
    AVFramePtr frame;
    AVBufferPtr buffer;

    bool allocate(JNIEnv *env, jclass exClass, int width, int height, AVPixelFormat format);
};

class ResizeCropFrame {

public:
    static AVFramePtr createAvFrame(JNIEnv *env, jclass exClass, int width, int height, AVPixelFormat format);

    static bool
    resizeAndCropFrame(JNIEnv *env, jclass exClass, const AVFramePtr &srcFrame, FrameWithBuffer &dstFrame, int targetWidth, int targetHeight);

    static void processFrame(JNIEnv *env, jclass exClass, AVFramePtr &rgbFrame, int width, int height, std::vector<FrameWithBuffer> &frames);

};


#endif //ANDROID_RESIZECROPFRAME_H
