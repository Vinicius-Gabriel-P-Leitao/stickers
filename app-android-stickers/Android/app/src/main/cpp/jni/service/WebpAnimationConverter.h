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
#ifndef ANDROID_WEBPANIMATIONCONVERTER_H
#define ANDROID_WEBPANIMATIONCONVERTER_H

#include <string>
#include <jni.h>

struct FrameWithBuffer;

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
