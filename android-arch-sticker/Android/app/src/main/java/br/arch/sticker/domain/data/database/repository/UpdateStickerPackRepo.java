/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.database.repository;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.AVOID_CACHE;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.LICENSE_AGREEMENT_WEBSITE;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.PRIVACY_POLICY_WEBSITE;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.PUBLISHER_EMAIL;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.PUBLISHER_WEBSITE;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_PACK_NAME_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.TABLE_STICKER_PACK;

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

    public boolean updateStickerPackName(String stickerPackIdentifier, String newName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(STICKER_PACK_NAME_IN_QUERY, newName);

        String whereClause = STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?";
        String[] whereArgs = {stickerPackIdentifier};

        try {
            int rowsUpdated = database.update(TABLE_STICKER_PACK, contentValues, whereClause, whereArgs);

            if (rowsUpdated == 0) {
                Log.w(TAG_LOG, "Nenhum registro atualizado ao renomear pacote de figurinhas.");
            }

            return rowsUpdated > 0;
        } catch (SQLException | IllegalStateException exception) {
            Log.e(TAG_LOG, "Erro ao atualizar nome do pacote de figurinhas: " +
                    exception.getMessage(), exception);
            return false;
        }
    }

    public boolean cleanStickerPackUrl(String stickerPackIdentifier) {
        ContentValues contentValueStickerPack = new ContentValues();
        contentValueStickerPack.put(PUBLISHER_EMAIL, "");
        contentValueStickerPack.put(PUBLISHER_WEBSITE, "");
        contentValueStickerPack.put(PRIVACY_POLICY_WEBSITE, "");
        contentValueStickerPack.put(LICENSE_AGREEMENT_WEBSITE, "");
        contentValueStickerPack.put(AVOID_CACHE, "");
        contentValueStickerPack.put(ANDROID_APP_DOWNLOAD_LINK_IN_QUERY, "");
        contentValueStickerPack.put(IOS_APP_DOWNLOAD_LINK_IN_QUERY, "");

        String whereClause = STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?";
        String[] whereArgs = {stickerPackIdentifier};

        try {
            int rowsUpdated = database.update(TABLE_STICKER_PACK, contentValueStickerPack, whereClause, whereArgs);

            if (rowsUpdated == 0) {
                Log.w(TAG_LOG, "Nenhum registro atualizado ao dar clean pacote de figurinhas.");
            }

            return rowsUpdated > 0;
        } catch (SQLException | IllegalStateException exception) {
            Log.e(TAG_LOG, "Erro ao dar clean nas URL do pacote de figurinhas: " +
                    exception.getMessage(), exception);
            return false;
        }
    }
}
