/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */


#ifndef ANDROID_SWSCONTEXTDESTROYER_HPP
#define ANDROID_SWSCONTEXTDESTROYER_HPP

#include <memory>

extern "C" {
#include "libswscale/swscale.h"
}

struct SwsContextDestroyer {
    void operator()(SwsContext *swsContext) const {
        sws_freeContext(swsContext);
    }
};

using SwsContextPtr = std::unique_ptr<SwsContext, SwsContextDestroyer>;

#endif //ANDROID_SWSCONTEXTDESTROYER_HPP
