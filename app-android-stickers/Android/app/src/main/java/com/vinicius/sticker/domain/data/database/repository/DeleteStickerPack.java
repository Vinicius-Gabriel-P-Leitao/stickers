/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.data.database.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.vinicius.sticker.core.exception.DeleteStickerException;
import com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.domain.pattern.CallbackResult;
import com.vinicius.sticker.domain.service.load.StickerPackLoaderService;

import java.sql.SQLException;
import java.util.Optional;

public class DeleteStickerPack {
    public static int deleteSticker(Context context, String stickerPackIdentifier, String fileName) throws SQLException {
        StickerDatabaseHelper dbHelper = StickerDatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        StickerPack fetchStickerPack = StickerPackLoaderService.fetchStickerPack(context, stickerPackIdentifier);
        if (fetchStickerPack.identifier ==
                null) {
            throw new DeleteStickerException("Erro ao encontrar o id do pacote para deletar.");
        }
        return db.delete(
                StickerDatabaseHelper.TABLE_STICKER, StickerDatabaseHelper.FK_STICKER_PACK +
                        " = ? AND " +
                        StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY +
                        " = ?", new String[]{String.valueOf(fetchStickerPack), fileName});
    }

    public static CallbackResult<Integer> deleteStickersOfPack(Context context, String stickerPackIdentifier) {
        try {
            StickerDatabaseHelper dbHelper = StickerDatabaseHelper.getInstance(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            int stickerDeleted = db.delete(
                    StickerDatabaseHelper.TABLE_STICKER, StickerDatabaseHelper.FK_STICKER_PACK +
                            " = ?", new String[]{stickerPackIdentifier});

            if (stickerDeleted ==
                    0) {
                return CallbackResult.warning("Nenhuma linha foi deletada. Talvez o ID não exista.");
            }

            return CallbackResult.success(stickerDeleted);
        } catch (Exception exception) {
            return CallbackResult.failure(exception);
        }
    }

    public static CallbackResult<Integer> deleteStickerPackFromDatabase(Context context, String stickerPackIdentifier) {
        try {
            StickerDatabaseHelper dbHelper = StickerDatabaseHelper.getInstance(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            int stickerPackDeleted = db.delete(
                    StickerDatabaseHelper.TABLE_STICKER_PACK, StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY +
                            " = ?", new String[]{stickerPackIdentifier});

            if (stickerPackDeleted ==
                    0) {
                return CallbackResult.warning("Nenhuma linha foi deletada. Talvez o ID não exista.");
            }

            return CallbackResult.success(stickerPackDeleted);
        } catch (Exception exception) {
            return CallbackResult.failure(exception);
        }
    }
}
