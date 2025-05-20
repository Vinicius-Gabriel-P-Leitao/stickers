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

#ifndef ANDROID_SWSCONTEXTDELETER_H
#define ANDROID_SWSCONTEXTDELETER_H

#include <memory>

extern "C" {
#include "libswscale/swscale.h"
}

struct SwsContextDeleter {
    void operator()(SwsContext *swsContext) const {
        sws_freeContext(swsContext);
    }
};

using SwsContextPtr = std::unique_ptr<SwsContext, SwsContextDeleter>;

#endif //ANDROID_SWSCONTEXTDELETER_H
