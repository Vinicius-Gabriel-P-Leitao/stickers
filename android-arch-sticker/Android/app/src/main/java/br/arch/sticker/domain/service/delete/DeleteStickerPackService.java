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

import androidx.annotation.NonNull;

import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.database.StickerDatabaseHelper;
import br.arch.sticker.domain.data.database.repository.DeleteStickerPackRepo;

public class DeleteStickerPackService {
    private final DeleteStickerPackRepo deleteStickerPackRepo;

    public DeleteStickerPackService(Context context) {
        SQLiteDatabase database = StickerDatabaseHelper.getInstance(context).getWritableDatabase();
        this.deleteStickerPackRepo = new DeleteStickerPackRepo(database);
    }

    public CallbackResult<Boolean> deleteStickerPack(@NonNull String stickerPackIdentifier) {
        CallbackResult<Integer> stickerPackDeletedInDb = deleteStickerPackRepo.deleteStickerPackFromDatabase(
                stickerPackIdentifier);
        return switch (stickerPackDeletedInDb.getStatus()) {
            case SUCCESS -> CallbackResult.success(Boolean.TRUE);
            case WARNING -> CallbackResult.warning(stickerPackDeletedInDb.getWarningMessage());
            case DEBUG -> CallbackResult.debug(stickerPackDeletedInDb.getDebugMessage());
            case FAILURE -> CallbackResult.failure(stickerPackDeletedInDb.getError());
        };
    }
}
