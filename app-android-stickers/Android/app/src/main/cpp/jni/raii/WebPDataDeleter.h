/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */


#ifndef ANDROID_WEBPDATADELETER_H
#define ANDROID_WEBPDATADELETER_H

#include <memory>

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
