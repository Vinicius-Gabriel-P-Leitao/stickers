/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.delete;

import static br.arch.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;

import br.arch.sticker.core.exception.throwable.sticker.DeleteStickerException;
import br.arch.sticker.core.pattern.CallbackResult;

// @formatter:off
public class DeleteStickerPackPathService {
    private final static String TAG_LOG = DeleteStickerAssetService.class.getSimpleName();

    public static CallbackResult<Boolean> deleteStickerPackPath(@NonNull Context context, @NonNull String stickerPackIdentifier) {
        CallbackResult<Boolean> deleteAllStickerAssets = DeleteStickerAssetService.deleteAllStickerAssetsByPack(context, stickerPackIdentifier);

        File mainDirectory = new File(context.getFilesDir(), STICKERS_ASSET);
        File stickerPackDirectory = new File(mainDirectory, stickerPackIdentifier);

        switch (deleteAllStickerAssets.getStatus()) {
            case SUCCESS:
                if (stickerPackDirectory.exists() && mainDirectory.exists()) {
                    boolean deleted = stickerPackDirectory.delete();

                    if (deleted) {
                        Log.i(TAG_LOG, "Pasta deletada: " + stickerPackDirectory.getAbsolutePath());
                        return CallbackResult.success(Boolean.TRUE);
                    } else {
                        return CallbackResult.failure(new DeleteStickerException("Falha ao deletar diretório: " + stickerPackDirectory.getAbsolutePath()));
                    }
                } else {
                    return CallbackResult.failure(new DeleteStickerException("Diretório não encontrado: " + stickerPackDirectory.getAbsolutePath()));
                }

            case WARNING:
                return CallbackResult.warning(deleteAllStickerAssets.getWarningMessage());

            case FAILURE:
                return CallbackResult.failure(deleteAllStickerAssets.getError());
        }

        return CallbackResult.failure(new DeleteStickerException("Status inesperado ao deletar pacote de figurinhas."));
    }
}
