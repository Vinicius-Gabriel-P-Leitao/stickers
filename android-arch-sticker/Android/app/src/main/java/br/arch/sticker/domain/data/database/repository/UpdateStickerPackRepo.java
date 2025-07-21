/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.database.repository;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_PACK_NAME_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.TABLE_STICKER_PACK;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import br.arch.sticker.R;
import br.arch.sticker.domain.data.mapper.StickerPackMapper;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class UpdateStickerPackRepo {
    private final static String TAG_LOG = UpdateStickerPackRepo.class.getSimpleName();

    private final SQLiteDatabase database;
    private final ApplicationTranslate applicationTranslate;

    public UpdateStickerPackRepo(SQLiteDatabase database, Resources resources) {
        this.database = database;
        this.applicationTranslate = new ApplicationTranslate(resources);
    }

    public boolean updateStickerPackName(String stickerPackIdentifier, String newName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(STICKER_PACK_NAME_IN_QUERY, newName);

        String whereClause = STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?";
        String[] whereArgs = {stickerPackIdentifier};

        try {
            int rowsUpdated = database.update(TABLE_STICKER_PACK, contentValues, whereClause, whereArgs);

            if (rowsUpdated == 0) {
                Log.w(TAG_LOG, applicationTranslate.translate(R.string.warn_no_sticker_pack_updated).get());
                return false;
            }

            return rowsUpdated > 0;
        } catch (SQLException | IllegalStateException exception) {
            Log.e(TAG_LOG, applicationTranslate.translate(R.string.error_update_sticker_pack_name).get(), exception);
            return false;
        }
    }

    public boolean cleanStickerPackUrl(String stickerPackIdentifier) {
        ContentValues contentValueStickerPack = StickerPackMapper.writeCleanUrlStickerPackToContentValues();

        String whereClause = STICKER_PACK_IDENTIFIER_IN_QUERY + " = ?";
        String[] whereArgs = {stickerPackIdentifier};

        try {
            int rowsUpdated = database.update(TABLE_STICKER_PACK, contentValueStickerPack, whereClause, whereArgs);

            if (rowsUpdated == 0) {
                Log.w(TAG_LOG, applicationTranslate.translate(R.string.error_clean_sticker_pack_url).get());
                return false;
            }

            return rowsUpdated > 0;
        } catch (SQLException | IllegalStateException exception) {
            Log.e(TAG_LOG, applicationTranslate.translate(R.string.error_clean_sticker_pack_url).get(), exception);
            return false;
        }
    }
}
