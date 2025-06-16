/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.save;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import br.arch.sticker.core.exception.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.view.core.util.convert.ConvertThumbnail;

public class SaveStickerAssetService {
    public static boolean copyStickerFromCache(
            Context context, StickerPack stickerPack, File stickerPackDirectory,
            SaveStickerPackService.SaveStickerPackCallback callback
    ) {
        List<Sticker> stickerList = stickerPack.getStickers();
        Sticker firstSticker = stickerList.get(0);

        File thumbnailSticker = new File(context.getCacheDir(), firstSticker.imageFileName);
        ConvertThumbnail.createThumbnail(thumbnailSticker, stickerPackDirectory, callback);

        for (Sticker sticker : stickerList) {
            String fileName = sticker.imageFileName;
            File sourceFile = new File(context.getCacheDir(), fileName);
            File destFile = new File(stickerPackDirectory, fileName);

            if (sourceFile.exists()) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Path sourcePath = sourceFile.toPath();
                        Path destPath = destFile.toPath();

                        Files.copy(sourcePath, destPath);

                        callback.onStickerPackSaveResult(CallbackResult.debug("Arquivo copiado para: " + destFile.getPath()));
                    }
                } catch (IOException exception) {
                    callback.onStickerPackSaveResult(
                            CallbackResult.failure(new StickerPackSaveException("Arquivo não encontrado: " + fileName, exception)));
                    return false;
                }
            } else {
                callback.onStickerPackSaveResult(CallbackResult.failure(new StickerPackSaveException("Arquivo não encontrado: " + fileName)));
                return false;
            }
        }
        return true;
    }
}
