/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

#ifndef ANDROID_AVBUFFERDESTROYER_HPP
#define ANDROID_AVBUFFERDESTROYER_HPP

#include <string>

extern "C" {
#include "libavutil/avutil.h"
}

struct AVBufferDestroyer {
    void operator()(uint8_t *ptr) const {
        av_free(ptr);
    }
};

using AVBufferPtr = std::unique_ptr<uint8_t, AVBufferDestroyer>;

#endif //ANDROID_AVBUFFERDESTROYER_HPP
