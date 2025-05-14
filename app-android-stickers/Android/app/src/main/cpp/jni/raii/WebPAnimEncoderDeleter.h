//
// Created by vinicius on 13/05/2025.
//

#ifndef ANDROID_WEBPANIMENCODERDELETER_H
#define ANDROID_WEBPANIMENCODERDELETER_H

#include <string>

extern "C" {
#include <mux.h>
#include "libswscale/swscale.h"
}

struct WebPAnimEncoderDeleter {
    void operator()(WebPAnimEncoder *enc) const {
        WebPAnimEncoderDelete(enc);
    }
};

using WebPAnimEncoderPtr = std::unique_ptr<WebPAnimEncoder, WebPAnimEncoderDeleter>;

#endif //ANDROID_WEBPANIMENCODERDELETER_H
