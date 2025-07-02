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

import java.sql.SQLException;

import br.arch.sticker.core.error.code.DeleteErrorCode;
import br.arch.sticker.core.error.throwable.sticker.DeleteStickerException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.database.StickerDatabase;

public class DeleteStickerPackRepo {
    private final Context context;

    public DeleteStickerPackRepo(Context context)
        {
            this.context = context.getApplicationContext();
        }

    public int deleteSticker(String stickerPackIdentifier, String fileName) throws SQLException
        {
            StickerDatabase dbHelper = StickerDatabase.getInstance(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            if (stickerPackIdentifier == null) {
                throw new DeleteStickerException("Erro ao encontrar o id do pacote para deletar figurinha.", DeleteErrorCode.ERROR_PACK_DELETE_DB);
            }

            return db.delete(StickerDatabase.TABLE_STICKER,
                    StickerDatabase.FK_STICKER_PACK + " = ? AND " + StickerDatabase.STICKER_FILE_NAME_IN_QUERY + " = ?",
                    new String[]{stickerPackIdentifier, fileName});
        }

    public CallbackResult<Integer> deleteAllStickerOfPack(String stickerPackIdentifier)
        {
            try {
                StickerDatabase dbHelper = StickerDatabase.getInstance(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                int stickerDeleted = db.delete(StickerDatabase.TABLE_STICKER, StickerDatabase.FK_STICKER_PACK + " = ?",
                        new String[]{stickerPackIdentifier});

                if (stickerDeleted == 0) {
                    return CallbackResult.warning("Nenhuma linha foi deletada. Talvez o ID não exista.");
                }

                return CallbackResult.success(stickerDeleted);
            } catch (Exception exception) {
                return CallbackResult.failure(exception);
            }
        }

    public CallbackResult<Integer> deleteStickerPackFromDatabase(String stickerPackIdentifier)
        {
            try {
                StickerDatabase dbHelper = StickerDatabase.getInstance(context);
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                int stickerPackDeleted = db.delete(StickerDatabase.TABLE_STICKER_PACK, StickerDatabase.STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?",
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
