/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */


#ifndef ANDROID_AVFORMATCONTEXTDELETER_H
#define ANDROID_AVFORMATCONTEXTDELETER_H

#include <memory>

extern "C" {
#include "libavformat/avformat.h"
}

struct AVFormatContextDeleter {
    void operator()(AVFormatContext* ctx) const {
        avformat_close_input(&ctx);
    }
};

using AVFormatContextPtr = std::unique_ptr<AVFormatContext, AVFormatContextDeleter>;

#endif //ANDROID_AVFORMATCONTEXTDELETER_H
