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

public class DeleteStickerAssetService {
    private final static String TAG_LOG = DeleteStickerAssetService.class.getSimpleName();

    private final Context context;

    public DeleteStickerAssetService(Context context)
        {
            this.context = context.getApplicationContext();
        }

    public CallbackResult<Boolean> deleteStickerAsset(@NonNull String stickerPackIdentifier, @NonNull String fileName)
        {
            File mainDirectory = new File(context.getFilesDir(), STICKERS_ASSET);
            File stickerDirectory = new File(mainDirectory, stickerPackIdentifier + File.separator + fileName);

            if (stickerDirectory.exists() && mainDirectory.exists()) {
                boolean deleted = stickerDirectory.delete();

                if (deleted) {
                    Log.i(TAG_LOG, "Arquivo deletado: " + stickerDirectory.getAbsolutePath());
                    return CallbackResult.success(Boolean.TRUE);
                } else {
                    return CallbackResult.failure(
                            new DeleteStickerException(String.format("Falha ao deletar arquivo: %s", stickerDirectory.getAbsolutePath()),
                                    DeleteErrorCode.ERROR_PACK_DELETE_SERVICE));
                }
            } else {
                return CallbackResult.failure(
                        new DeleteStickerException(String.format("Arquivo não encontrado para deletar: %s", stickerDirectory.getAbsolutePath()),
                                DeleteErrorCode.ERROR_PACK_DELETE_SERVICE));
            }
        }

    public CallbackResult<Boolean> deleteAllStickerAssetsByPack(@NonNull String stickerPackIdentifier)
        {
            File mainDirectory = new File(context.getFilesDir(), STICKERS_ASSET);
            File stickerPackDirectory = new File(mainDirectory, stickerPackIdentifier);

            if (stickerPackDirectory.exists() && stickerPackDirectory.isDirectory()) {
                File[] files = stickerPackDirectory.listFiles();

                if (files != null) {
                    for (File file : files) {
                        if (!file.delete()) {
                            return CallbackResult.failure(
                                    new DeleteStickerException(String.format("Falha ao deletar o arquivo: %s", file.getAbsolutePath()),
                                            DeleteErrorCode.ERROR_PACK_DELETE_SERVICE));
                        }

                    }
                }

                return CallbackResult.success(true);
            } else {
                return CallbackResult.warning("Diretório não encontrado: " + stickerPackDirectory.getAbsolutePath());
            }
        }
}
