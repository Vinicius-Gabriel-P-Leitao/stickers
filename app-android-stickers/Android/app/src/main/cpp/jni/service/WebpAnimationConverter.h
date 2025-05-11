//
// Created by vinicius on 11/05/2025.
//

#ifndef ANDROID_WEBPANIMATIONCONVERTER_H
#define ANDROID_WEBPANIMATIONCONVERTER_H

#include <string>

struct FrameWithBuffer;

class WebpAnimationConverter {
public:
    static int convertToWebp(const char *outputPath,
                             std::vector<FrameWithBuffer> &frames,
                             int width,
                             int height,
                             int durationMs);

private:
};


#endif //ANDROID_WEBPANIMATIONCONVERTER_H
