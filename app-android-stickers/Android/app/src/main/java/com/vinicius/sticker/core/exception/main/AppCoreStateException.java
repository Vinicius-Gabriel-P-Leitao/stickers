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

package com.vinicius.sticker.core.exception.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AppCoreStateException extends IllegalStateException {
    private final String errorCode;
    private final Object[] details;

    public AppCoreStateException(@NonNull String message) {
        this(message, null, null);
    }

    public AppCoreStateException(@NonNull String message, @Nullable Throwable cause) {
        this(message, cause, null);
    }

    public AppCoreStateException(@NonNull String message, @Nullable String errorCode) {
        this(message, null, errorCode);
    }

    public AppCoreStateException(@NonNull String message, @Nullable Throwable cause, @Nullable String errorCode) {
        this(message, cause, errorCode, null);
    }

    public AppCoreStateException(@NonNull String message, @Nullable Throwable cause, @Nullable String errorCode, @Nullable Object[] details) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    @Nullable
    public String getErrorCode() {
        return errorCode;
    }

    @Nullable
    public Object[] getDetails() {
        return details;
    }
}
