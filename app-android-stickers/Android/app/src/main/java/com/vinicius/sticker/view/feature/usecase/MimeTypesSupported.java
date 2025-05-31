/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.feature.usecase;

// @formatter:off
public enum MimeTypesSupported {
    IMAGE(new String[]{"image/jpeg", "image/png"}),
    ANIMATED(new String[]{"video/mp4", "image/gif"});

    private final String[] mimeTypes;

    MimeTypesSupported(String[] mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public String[] getMimeTypes() {
        return mimeTypes;
    }
}
