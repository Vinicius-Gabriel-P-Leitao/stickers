/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.model;

public record StickerPackListItem(Object stickerPack, Status status) {
    public enum Status {
        VALID, INVALID, WITH_INVALID_STICKER
    }
}