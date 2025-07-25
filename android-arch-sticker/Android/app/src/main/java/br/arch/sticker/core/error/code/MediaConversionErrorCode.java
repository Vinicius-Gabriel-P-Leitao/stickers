/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.error.code;

import androidx.annotation.Nullable;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCodeProvider;

// @formatter:off
public enum MediaConversionErrorCode implements ErrorCodeProvider {
    ERROR_NATIVE_CONVERSION(R.string.throw_error_in_native_conversion),
    ERROR_PACK_CONVERSION_MEDIA(R.string.throw_error_conversion_media);

    private final int message;

    MediaConversionErrorCode(int message) {
        this.message = message;
    }

    @Override
    public int getMessageResId() {
        return message;
    }

    @Nullable
    public static MediaConversionErrorCode fromName(@Nullable String name) {
        if (name == null) return null;

        try {
            return MediaConversionErrorCode.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
