/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */
#ifndef ANDROID_WEBPDEMUXERPTR_HPP
#define ANDROID_WEBPDEMUXERPTR_HPP

extern "C" {
#include "demux.h"
}

struct WebpDemuxerPtr {
    WebPDemuxer *pDemuxer = nullptr;

    explicit WebpDemuxerPtr(WebPDemuxer *ptr) : pDemuxer(ptr) {}

    ~WebpDemuxerPtr() {
        if (pDemuxer) WebPDemuxDelete(pDemuxer);
    }

    [[nodiscard]] WebPDemuxer *getPDemuxer() const { return pDemuxer; }

    WebPDemuxer *operator->() const { return pDemuxer; }

    explicit operator bool() const { return pDemuxer != nullptr; }
};

#endif //ANDROID_WEBPDEMUXERPTR_HPP
