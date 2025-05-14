//
// Created by vinicius on 13/05/2025.
//

#ifndef ANDROID_WEBPDATADELETER_H
#define ANDROID_WEBPDATADELETER_H

#include <string>

extern "C" {
#include <mux.h>
#include "libswscale/swscale.h"
}

struct WebPDataDeleter {
    void operator()(WebPData *data) const {
        WebPDataClear(data);
    }
};

using WebPDataPtr = std::unique_ptr<WebPData, WebPDataDeleter>;

#endif //ANDROID_WEBPDATADELETER_H
