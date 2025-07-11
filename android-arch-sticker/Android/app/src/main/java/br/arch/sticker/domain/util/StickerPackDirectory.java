/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.util;

import androidx.annotation.NonNull;

import java.io.File;

import br.arch.sticker.core.error.code.SaveErrorCode;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;

public class StickerPackDirectory {
    public static CallbackResult<Boolean> createMainDirectory(@NonNull File mainDirectory) throws StickerPackSaveException
        {
            if (!mainDirectory.exists()) {
                boolean created = mainDirectory.mkdirs();

                if (!created) {
                    return CallbackResult.failure(
                            new StickerPackSaveException(
                                    String.format("Falha ao criar o mainDirectory: %s", mainDirectory.getPath()),
                                    SaveErrorCode.ERROR_PACK_SAVE_UTIL));
                }

                return CallbackResult.success(created);
            } else {
                return CallbackResult.debug("mainDirectory já existe: " + mainDirectory.getPath());
            }
        }

    public static CallbackResult<File> createStickerPackDirectory(
            @NonNull File mainDirectory, @NonNull String stickerPackIdentifier) throws StickerPackSaveException
        {
            File stickerPackDirectory = new File(mainDirectory, stickerPackIdentifier);

            if (!stickerPackDirectory.exists()) {
                boolean created = stickerPackDirectory.mkdirs();

                if (!created) {
                    return CallbackResult.failure(
                            new StickerPackSaveException(
                                    String.format("Falha ao criar a pasta: %s", stickerPackDirectory.getPath()),
                                    SaveErrorCode.ERROR_PACK_SAVE_UTIL));

                }

                return CallbackResult.success(stickerPackDirectory);
            } else {
                return CallbackResult.debug("Pasta já existe: " + stickerPackDirectory.getPath());
            }
        }
}
