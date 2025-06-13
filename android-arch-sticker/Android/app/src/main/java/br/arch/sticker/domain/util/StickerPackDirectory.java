/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.util;

import java.io.File;

import br.arch.sticker.core.exception.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.service.save.SaveStickerPackService;

public class StickerPackDirectory {
    public static boolean createMainDirectory(File mainDirectory, SaveStickerPackService.SaveStickerPackCallback callback) {
        if (!mainDirectory.exists()) {
            boolean created = mainDirectory.mkdirs();

            if (!created) {
                callback.onStickerPackSaveResult(
                        CallbackResult.failure(new StickerPackSaveException("Falha ao criar o mainDirectory: " + mainDirectory.getPath())));
                return false;
            }
            callback.onStickerPackSaveResult(CallbackResult.debug("mainDirectory criado com sucesso: " + mainDirectory.getPath()));
        } else {
            callback.onStickerPackSaveResult(CallbackResult.debug("mainDirectory já existe: " + mainDirectory.getPath()));
        }

        return true;
    }

    public static File createStickerPackDirectory(
            File mainDirectory, String stickerPackIdentifier, SaveStickerPackService.SaveStickerPackCallback callback) {
        File stickerPackDirectory = new File(mainDirectory, stickerPackIdentifier);

        if (!stickerPackDirectory.exists()) {
            boolean created = stickerPackDirectory.mkdirs();

            if (!created) {
                callback.onStickerPackSaveResult(
                        CallbackResult.failure(new StickerPackSaveException("Falha ao criar a pasta: " + stickerPackDirectory.getPath())));
                return null;
            }

            callback.onStickerPackSaveResult(CallbackResult.debug("Pasta criada com sucesso: " + stickerPackDirectory.getPath()));
        } else {
            callback.onStickerPackSaveResult(CallbackResult.warning("Pasta já existe: " + stickerPackDirectory.getPath()));
        }
        return stickerPackDirectory;
    }

}
