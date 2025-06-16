/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.database.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import br.arch.sticker.core.exception.throwable.sticker.DeleteStickerException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.database.StickerDatabase;
import br.arch.sticker.domain.dto.StickerPackValidationResult;
import br.arch.sticker.domain.service.fetch.FetchStickerPackService;

import java.sql.SQLException;

// @formatter:off
public class DeleteStickerPackRepo {
    public static int deleteSticker(Context context, String stickerPackIdentifier, String fileName) throws SQLException {
        StickerDatabase dbHelper = StickerDatabase.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        StickerPackValidationResult fetchStickerPack = FetchStickerPackService.fetchStickerPackFromContentProvider(context, stickerPackIdentifier);
        if (fetchStickerPack.stickerPack().identifier == null) {
            throw new DeleteStickerException("Erro ao encontrar o id do pacote para deletar.");
        }

        return db.delete(
                StickerDatabase.TABLE_STICKER, StickerDatabase.FK_STICKER_PACK + " = ? AND " + StickerDatabase.STICKER_FILE_NAME_IN_QUERY + " = ?",
                new String[]{String.valueOf(fetchStickerPack), fileName}
        );
    }

    public static CallbackResult<Integer> deleteAllStickerOfPack(Context context, String stickerPackIdentifier) {
        try {
            StickerDatabase dbHelper = StickerDatabase.getInstance(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            int stickerDeleted = db.delete(
                    StickerDatabase.TABLE_STICKER, StickerDatabase.FK_STICKER_PACK + " = ?",
                    new String[]{stickerPackIdentifier});

            if (stickerDeleted == 0) {
                return CallbackResult.warning("Nenhuma linha foi deletada. Talvez o ID não exista.");
            }

            return CallbackResult.success(stickerDeleted);
        } catch (Exception exception) {
            return CallbackResult.failure(exception);
        }
    }

    public static CallbackResult<Integer> deleteStickerPackFromDatabase(Context context, String stickerPackIdentifier) {
        try {
            StickerDatabase dbHelper = StickerDatabase.getInstance(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            int stickerPackDeleted = db.delete(
                    StickerDatabase.TABLE_STICKER_PACK, StickerDatabase.STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?",
                    new String[]{stickerPackIdentifier});

            if (stickerPackDeleted == 0) {
                return CallbackResult.warning("Nenhuma linha foi deletada. Talvez o ID não exista.");
            }

            return CallbackResult.success(stickerPackDeleted);
        } catch (Exception exception) {
            return CallbackResult.failure(exception);
        }
    }
}
