/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

#ifndef ANDROID_PROCESSINPUTMEDIA_HPP
#define ANDROID_PROCESSINPUTMEDIA_HPP

#include <map>
#include <any>
#include <jni.h>
#include <string>
#include <vector>
#include <functional>

#include "../service/ProcessFramesToFormat.hpp"

#include "../raii/AVFrameDestroyer.hpp"
#include "../raii/AVBufferDestroyer.hpp"

using ParamsMap = std::map<std::string, std::any>;

using FrameProcessor = std::function<
        void(JNIEnv *, jclass, AVFramePtr &, int, int, std::vector<FrameWithBuffer> &, const ParamsMap &)>;

class ProcessInputMedia {
public:
    ProcessInputMedia(JNIEnv *env, jclass nativeMediaException);

    std::vector<FrameWithBuffer>
    processVideoFrames(const char *inPath, const char *outPath, const FrameProcessor &frameProcessor, const ParamsMap &params = {});

private:
    JNIEnv *env;
    jclass nativeMediaException;
};

#endif //ANDROID_PROCESSINPUTMEDIA_HPP
