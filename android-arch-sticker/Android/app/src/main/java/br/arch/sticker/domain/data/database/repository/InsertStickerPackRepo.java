/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.database.repository;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.TABLE_STICKER;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.TABLE_STICKER_PACK;
import static br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import androidx.annotation.NonNull;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.mapper.StickerMapper;
import br.arch.sticker.domain.data.mapper.StickerPackMapper;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class InsertStickerPackRepo {
    private final static String TAG_LOG = InsertStickerPackRepo.class.getSimpleName();

    private final SQLiteDatabase database;
    private final ApplicationTranslate applicationTranslate;

    public InsertStickerPackRepo(SQLiteDatabase database, Resources resources) {
        this.database = database;
        this.applicationTranslate = new ApplicationTranslate(resources);
    }

    @NonNull
    public CallbackResult<StickerPack> insertStickerPack(StickerPack stickerPack) {
        try {
            ContentValues stickerPackValues = StickerPackMapper.writeStickerPackToContentValues(stickerPack);
            long resultStickerPack = database.insertOrThrow(TABLE_STICKER_PACK, null, stickerPackValues);

            if (resultStickerPack != -1) {
                for (Sticker sticker : stickerPack.getStickers()) {
                    ContentValues stickerValues = StickerMapper.writeStickerToContentValues(sticker);
                    database.insertOrThrow(TABLE_STICKER, null, stickerValues);
                }

                return CallbackResult.success(stickerPack);
            } else {
                return CallbackResult.failure(new StickerPackSaveException(
                        applicationTranslate.translate(R.string.error_save_sticker_pack_general).log(TAG_LOG, Level.ERROR).get(),
                        ErrorCode.ERROR_PACK_SAVE_DB
                ));
            }
        } catch (SQLiteException sqLiteException) {
            return CallbackResult.failure(new StickerPackSaveException(
                    applicationTranslate.translate(R.string.error_save_sticker_pack_db).log(TAG_LOG, Level.ERROR, sqLiteException).get(),
                    sqLiteException, ErrorCode.ERROR_PACK_SAVE_DB
            ));
        } catch (Exception exception) {
            return CallbackResult.failure(new StickerPackSaveException(
                    applicationTranslate.translate(R.string.error_unexpected_save_sticker_pack_db).log(TAG_LOG, Level.ERROR, exception).get(),
                    ErrorCode.ERROR_PACK_SAVE_DB
            ));
        }
    }

}
