/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */


#ifndef ANDROID_WEBPANIMATIONCONVERTER_H
#define ANDROID_WEBPANIMATIONCONVERTER  _H

#include <string>
#include <jni.h>
#include "ProcessFramesToFormat.hpp"

#include "../raii/AVFrameDestroyer.hpp"

class WebpAnimationConverter {
public:
    static int convertToWebp(JNIEnv *env,
                             const char *outputPath,
                             std::vector<FrameWithBuffer> &frames,
                             int width,
                             int height,
                             int durationMs);

private:
};

#endif //ANDROID_WEBPANIMATIONCONVERTER_H
