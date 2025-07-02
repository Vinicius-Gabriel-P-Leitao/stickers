/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */
#ifndef ANDROID_WEBPITERATORPTR_HPP
#define ANDROID_WEBPITERATORPTR_HPP

#include <ostream>

extern "C" {
#include "demux.h"
}

struct WebpIteratorPtr {
    WebPIterator iterator{};
    bool valid = false;

    ~WebpIteratorPtr() {
        if (valid) WebPDemuxReleaseIterator(&iterator);
    }

    bool init(WebPDemuxer *demuxer, int frame_index) {
        valid = WebPDemuxGetFrame(demuxer, frame_index, &iterator) != 0;
        return valid;
    }

    bool next() {
        if (!valid) return false;
        valid = WebPDemuxNextFrame(&iterator) != 0;
        return valid;
    }

    const WebPIterator *operator->() const { return &iterator; }
};

#endif //ANDROID_WEBPITERATORPTR_HPP
