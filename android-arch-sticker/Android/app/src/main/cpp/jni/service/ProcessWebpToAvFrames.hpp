/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */
#ifndef ANDROID_PROCESSWEBPTOAVFRAMES_HPP
#define ANDROID_PROCESSWEBPTOAVFRAMES_HPP

#include <jni.h>

#include "../raii/AVFrameDestroyer.hpp"
#include "../raii/AVBufferDestroyer.hpp"
#include "../raii/WebpDemuxerPtr.hpp"
#include "../raii/WebpIteratorPtr.hpp"
#include "../service/ProcessFramesToFormat.hpp"

class ProcessWebpToAvFrames {
public:
    static bool decodeWebPAsAVFrames(
            JNIEnv *env, const std::string &inputPath, std::vector<FrameWithBuffer> &frames, int targetWidth, int targetHeight);

private:
    static std::vector<uint8_t> loadFileToMemory(const std::string &path);
};

#endif //ANDROID_PROCESSWEBPTOAVFRAMES_HPP
