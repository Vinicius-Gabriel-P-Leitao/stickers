/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.exception.throwable.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class InternalAppException extends AppCoreStateException {
    public InternalAppException(@NonNull String message) {
        super(message, "INTERNAL_ERROR");
    }

    public InternalAppException(@NonNull String message, Exception exception) {
        super(message, exception, "INTERNAL_ERROR");
    }

    public InternalAppException(@NonNull String message, @Nullable Throwable cause, @Nullable String errorDetails) {
        super(message, cause, "INTERNAL_ERROR", new Object[]{errorDetails});
    }

    @Nullable
    public String getErrorDetails() {
        Object[] details = getDetails();
        return details != null && details.length > 0 ? (String) details[0] : null;
    }
}
