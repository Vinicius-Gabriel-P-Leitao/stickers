/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.service.delete;

import static com.vinicius.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vinicius.sticker.core.exception.sticker.DeleteStickerException;
import com.vinicius.sticker.core.pattern.CallbackResult;

import java.io.File;

public class DeleteStickerAssetService {
    private final static String TAG_LOG = DeleteStickerAssetService.class.getSimpleName();

    public static CallbackResult<Boolean> deleteStickerAsset(
            @NonNull Context context, @NonNull String stickerPackIdentifier, @NonNull String fileName) {
        File mainDirectory = new File(context.getFilesDir(), STICKERS_ASSET);
        File stickerDirectory = new File(mainDirectory, stickerPackIdentifier + File.separator + fileName);

        if (stickerDirectory.exists() && mainDirectory.exists()) {
            boolean deleted = stickerDirectory.delete();

            if (deleted) {
                Log.i(TAG_LOG, "Arquivo deletado: " + stickerDirectory.getAbsolutePath());
                return CallbackResult.success(Boolean.TRUE);
            } else {
                return CallbackResult.failure(new DeleteStickerException("Falha ao deletar arquivo: " + stickerDirectory.getAbsolutePath()));
            }
        } else {
            return CallbackResult.failure(new DeleteStickerException("Arquivo não encontrado para deletar: " + stickerDirectory.getAbsolutePath()));
        }
    }

        public static CallbackResult<Boolean> deleteAllStickerAssetsInPack(@NonNull Context context, @NonNull String stickerPackIdentifier) {
            File mainDirectory = new File(context.getFilesDir(), STICKERS_ASSET);
            File stickerPackDirectory = new File(mainDirectory, stickerPackIdentifier);

            if (stickerPackDirectory.exists() && stickerPackDirectory.isDirectory()) {
                File[] files = stickerPackDirectory.listFiles();

                if (files != null) {
                    for (File file : files) {
                        if (!file.delete()) {
                            return CallbackResult.failure(new DeleteStickerException("Falha ao deletar o arquivo: " + file.getAbsolutePath()));
                        }

                    }
                }

                return CallbackResult.success(true);
            } else {
                return CallbackResult.warning("Diretório não encontrado: " + stickerPackDirectory.getAbsolutePath());
            }
        }
}
