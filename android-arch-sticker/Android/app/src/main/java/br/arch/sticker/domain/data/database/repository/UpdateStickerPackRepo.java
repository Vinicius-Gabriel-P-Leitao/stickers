/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.database.repository;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.FK_STICKER_PACK;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_IS_VALID;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.TABLE_STICKER;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UpdateStickerPackRepo {
    private final static String TAG_LOG = UpdateStickerPackRepo.class.getSimpleName();

    private final SQLiteDatabase database;

    public UpdateStickerPackRepo(SQLiteDatabase database) {
        this.database = database;
    }

    public boolean updateStickerFileName(String stickerPackIdentifier, String newFileName, String oldFileName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(STICKER_FILE_NAME_IN_QUERY, newFileName);
        contentValues.put(STICKER_IS_VALID, ""); // NOTE: Retirar o erro

        String whereClause = FK_STICKER_PACK + " = ? AND " + STICKER_FILE_NAME_IN_QUERY + " = ?";
        String[] whereArgs = {stickerPackIdentifier, oldFileName};


        try {
            int rowsUpdated = database.update(TABLE_STICKER, contentValues, whereClause, whereArgs);

            if (rowsUpdated == 0) {
                Log.w(TAG_LOG, "Nenhum registro atualizado ao renomear figurinha.");
            }

            return rowsUpdated > 0;
        } catch (SQLException | IllegalStateException exception) {
            Log.e(TAG_LOG, "Erro ao atualizar nome da figurinha: " + exception.getMessage(),
                    exception);
            return false;
        }
    }

    public boolean updateInvalidSticker(String stickerPackIdentifier, String fileName, String errorMessage) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(STICKER_IS_VALID, errorMessage);

        String whereClause = FK_STICKER_PACK + " = ? AND " + STICKER_FILE_NAME_IN_QUERY + " = ?";
        String[] whereArgs = {stickerPackIdentifier, fileName};

        try {
            int rowsUpdated = database.update(TABLE_STICKER, contentValues, whereClause, whereArgs);

            if (rowsUpdated == 0) {
                Log.w(TAG_LOG,
                        "Nenhum registro marcado como inválido. Verifique o identificador e nome do arquivo.");
            }

            return rowsUpdated > 0;
        } catch (SQLException | IllegalStateException exception) {
            Log.e(TAG_LOG, "Erro ao marcar figurinha como inválida: " + exception.getMessage(),
                    exception);
            return false;
        }
    }
}
