/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */


#ifndef ANDROID_WEBPANIMENCODERDESTROYER_HPP
#define ANDROID_WEBPANIMENCODERDESTROYER_HPP

#include <memory>

extern "C" {
#include <mux.h>
#include "libswscale/swscale.h"
}

struct WebpAnimEncoderDestroyer {
    void operator()(WebPAnimEncoder *enc) const {
        WebPAnimEncoderDelete(enc);
    }
};

using WebPAnimEncoderPtr = std::unique_ptr<WebPAnimEncoder, WebpAnimEncoderDestroyer>;

#endif //ANDROID_WEBPANIMENCODERDESTROYER_HPP
