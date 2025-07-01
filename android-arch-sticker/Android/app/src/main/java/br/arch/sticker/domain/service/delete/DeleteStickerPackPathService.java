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

import br.arch.sticker.core.error.code.DeleteErrorCode;
import br.arch.sticker.core.error.throwable.sticker.DeleteStickerException;
import br.arch.sticker.core.pattern.CallbackResult;

public class DeleteStickerPackPathService {
    private final static String TAG_LOG = DeleteStickerAssetService.class.getSimpleName();

    private final DeleteStickerAssetService deleteStickerAssetService;
    private final Context context;

    public DeleteStickerPackPathService(Context context)
        {
            this.context = context.getApplicationContext();
            this.deleteStickerAssetService = new DeleteStickerAssetService(this.context);
        }

    public CallbackResult<Boolean> deleteStickerPackPath(@NonNull String stickerPackIdentifier)
        {
            // NOTE: Necessário devido a dar erros de recursividade.
            CallbackResult<Boolean> deleteAllStickerAssets = deleteStickerAssetService.deleteAllStickerAssetsByPack(stickerPackIdentifier);

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
                            return CallbackResult.failure(
                                    new DeleteStickerException("Falha ao deletar diretório: " + stickerPackDirectory.getAbsolutePath(),
                                            DeleteErrorCode.ERROR_PACK_DELETE_SERVICE));
                        }
                    } else {
                        return CallbackResult.failure(
                                new DeleteStickerException(String.format("Diretório não encontrado: %s", stickerPackDirectory.getAbsolutePath()),
                                        DeleteErrorCode.ERROR_PACK_DELETE_SERVICE));
                    }

                case WARNING:
                    return CallbackResult.warning(deleteAllStickerAssets.getWarningMessage());

                case FAILURE:
                    return CallbackResult.failure(deleteAllStickerAssets.getError());
            }

            return CallbackResult.failure(
                    new DeleteStickerException("Status inesperado ao deletar pacote de figurinhas.", DeleteErrorCode.ERROR_PACK_DELETE_SERVICE));
        }
}
