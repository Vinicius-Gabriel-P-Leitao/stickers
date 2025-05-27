/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */

package com.vinicius.sticker.domain.data.database.repository;

import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.IOS_APP_DOWNLOAD_LINK_IN_QUERY;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.vinicius.sticker.core.exception.StickerPackSaveException;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.domain.pattern.CallbackResult;

public class InsertStickerPacks {

    public interface InsertStickerPackCallback {
        void onInsertResult(CallbackResult<StickerPack> result);
    }

    public void insertStickerPack(SQLiteDatabase dbHelper, StickerPack pack, InsertStickerPackCallback callback) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ContentValues stickerPacksValues = new ContentValues();
            stickerPacksValues.put(ANDROID_APP_DOWNLOAD_LINK_IN_QUERY, pack.androidPlayStoreLink);
            stickerPacksValues.put(IOS_APP_DOWNLOAD_LINK_IN_QUERY, pack.iosAppStoreLink);
            long stickerPackId = dbHelper.insert("sticker_packs", null, stickerPacksValues);

            if (stickerPackId != -1) {
                ContentValues stickerPackValues = StickerPack.toContentValues(pack, stickerPackId);
                long result = dbHelper.insert("sticker_pack", null, stickerPackValues);

                if (result != -1) {
                    for (Sticker sticker : pack.getStickers()) {
                        ContentValues stickerValues = Sticker.toContentValues(sticker, stickerPackId);
                        dbHelper.insert("sticker", null, stickerValues);
                    }

                    if (callback != null) {
                        callback.onInsertResult(CallbackResult.success(pack));
                    }
                } else {
                    if (callback != null) {
                        callback.onInsertResult(CallbackResult.failure(new StickerPackSaveException("Failed to insert sticker pack.")));
                    }
                }
            } else {
                if (callback != null) {
                    callback.onInsertResult(CallbackResult.failure(new StickerPackSaveException("Failed to insert sticker pack details.")));
                }
            }
        }, 1000);
    }
}
