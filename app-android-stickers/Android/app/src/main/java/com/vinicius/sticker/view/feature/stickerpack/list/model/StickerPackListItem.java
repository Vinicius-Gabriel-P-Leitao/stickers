/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.feature.stickerpack.list.model;

public class StickerPackListItem {
    private final Object stickerPack;
    private final Status status;

    public enum Status {
        VALID, INVALID, WITH_INVALID_STICKER
    }

    public StickerPackListItem(Object stickerPack, Status status) {
        this.stickerPack = stickerPack;
        this.status = status;
    }

    public Object getStickerPack() {
        return stickerPack;
    }

    public Status getStatus() {
        return status;
    }
}