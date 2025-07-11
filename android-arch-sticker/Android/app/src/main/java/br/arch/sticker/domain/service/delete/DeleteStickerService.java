/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.delete;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import br.arch.sticker.core.error.throwable.sticker.DeleteStickerException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.database.StickerDatabaseHelper;
import br.arch.sticker.domain.data.database.repository.DeleteStickerRepo;

public class DeleteStickerService {
    private final static String TAG_LOG = DeleteStickerService.class.getSimpleName();

    private final DeleteStickerRepo deleteStickerPackRepo;

    public DeleteStickerService(Context paramContext) {
        Context context = paramContext.getApplicationContext();
        SQLiteDatabase database = StickerDatabaseHelper.getInstance(context).getWritableDatabase();
        this.deleteStickerPackRepo = new DeleteStickerRepo(database);
    }

    public CallbackResult<Boolean> deleteStickerByPack(@NonNull String stickerPackIdentifier, @NonNull String fileName) {
        try {
            int deletedSticker = deleteStickerPackRepo.deleteSticker(stickerPackIdentifier,
                    fileName);

            if (deletedSticker > 0) {
                Log.i(TAG_LOG, "Figurinha deletado com sucesso");
                return CallbackResult.success(Boolean.TRUE);
            } else {
                return CallbackResult.warning(
                        "Nenhuma Figurinha deletada para fileName: " + fileName);
            }
        } catch (DeleteStickerException exception) {
            return CallbackResult.failure(exception);
        }
    }
}
