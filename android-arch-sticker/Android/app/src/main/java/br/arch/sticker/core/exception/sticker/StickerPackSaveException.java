/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.exception.sticker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import br.arch.sticker.core.exception.base.AppCoreStateException;

public class StickerPackSaveException extends AppCoreStateException {
    public StickerPackSaveException(@NonNull String message) {
        super(message, "PACK_SAVE");
    }

    public StickerPackSaveException(@NonNull String message, Exception exception) {
        super(message, exception, "PACK_SAVE");
    }

    public StickerPackSaveException(@NonNull String message, @Nullable Throwable cause, @Nullable String packSaveError) {
        super(message, cause, "PACK_SAVE", new Object[]{packSaveError});
    }

    @Nullable
    public String getSaveErrorDetails() {
        Object[] details = getDetails();
        return details != null && details.length > 0 ? (String) details[0] : null;
    }
}
