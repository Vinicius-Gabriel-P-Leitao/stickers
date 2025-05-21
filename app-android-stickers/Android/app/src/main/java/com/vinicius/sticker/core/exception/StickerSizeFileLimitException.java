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

package com.vinicius.sticker.core.exception;

public class StickerSizeFileLimitException extends IllegalStateException {
    private final String stickerPackIdentifier;

    private final String fileName;

    public StickerSizeFileLimitException(String message) {
        super(message);
        this.stickerPackIdentifier = null;
        this.fileName = null;
    }

    public StickerSizeFileLimitException(String message, String stickerPackIdentifier, String fileName) {
        super(message);
        this.stickerPackIdentifier = stickerPackIdentifier;
        this.fileName = fileName;
    }

    public String getStickerPackIdentifier() {
        return stickerPackIdentifier;
    }

    public String getFileName() {
        return fileName;
    }
}
