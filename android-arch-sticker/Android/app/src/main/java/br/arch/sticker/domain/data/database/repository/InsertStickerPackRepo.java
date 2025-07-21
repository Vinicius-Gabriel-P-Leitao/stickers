/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.database.repository;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.ANIMATED_STICKER_PACK;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.AVOID_CACHE;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.IMAGE_DATA_VERSION;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.LICENSE_AGREEMENT_WEBSITE;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.PRIVACY_POLICY_WEBSITE;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.PUBLISHER_EMAIL;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.PUBLISHER_WEBSITE;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_PACK_NAME_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_PACK_PUBLISHER_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_PACK_TRAY_IMAGE_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.TABLE_STICKER;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.TABLE_STICKER_PACK;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import br.arch.sticker.core.error.code.SaveErrorCode;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;

public class InsertStickerPackRepo {
    private final SQLiteDatabase database;

    public InsertStickerPackRepo(SQLiteDatabase database) {
        this.database = database;
    }

    @NonNull
    public CallbackResult<StickerPack> insertStickerPack(StickerPack stickerPack)
            throws SQLException {
        ContentValues stickerPackValues = writeStickerPackToContentValues(stickerPack);
        long result = database.insertOrThrow(TABLE_STICKER_PACK, null, stickerPackValues);

        if (result != -1) {
            for (Sticker sticker : stickerPack.getStickers()) {
                ContentValues stickerValues = InsertStickerRepo.writeStickerToContentValues(
                        sticker);
                database.insertOrThrow(TABLE_STICKER, null, stickerValues);
            }

            return CallbackResult.success(stickerPack);
        } else {
            return CallbackResult.failure(new StickerPackSaveException("Erro ao inserir pacote.",
                    SaveErrorCode.ERROR_PACK_SAVE_DB
            ));
        }
    }

    @NonNull
    private static ContentValues writeStickerPackToContentValues(StickerPack stickerPack) {
        ContentValues stickerPackValues = new ContentValues();
        stickerPackValues.put(STICKER_PACK_IDENTIFIER_IN_QUERY, stickerPack.identifier);
        stickerPackValues.put(STICKER_PACK_NAME_IN_QUERY, stickerPack.name);
        stickerPackValues.put(STICKER_PACK_PUBLISHER_IN_QUERY, stickerPack.publisher);
        stickerPackValues.put(STICKER_PACK_TRAY_IMAGE_IN_QUERY, stickerPack.trayImageFile);
        stickerPackValues.put(PUBLISHER_EMAIL, stickerPack.publisherEmail);
        stickerPackValues.put(PUBLISHER_WEBSITE, stickerPack.publisherWebsite);
        stickerPackValues.put(PRIVACY_POLICY_WEBSITE, stickerPack.privacyPolicyWebsite);
        stickerPackValues.put(LICENSE_AGREEMENT_WEBSITE, stickerPack.licenseAgreementWebsite);
        stickerPackValues.put(ANIMATED_STICKER_PACK, stickerPack.animatedStickerPack ? 1 : 0);
        stickerPackValues.put(IMAGE_DATA_VERSION, stickerPack.imageDataVersion);
        stickerPackValues.put(AVOID_CACHE, stickerPack.avoidCache ? 1 : 0);
        stickerPackValues.put(ANDROID_APP_DOWNLOAD_LINK_IN_QUERY, stickerPack.androidPlayStoreLink);
        stickerPackValues.put(IOS_APP_DOWNLOAD_LINK_IN_QUERY, stickerPack.iosAppStoreLink);
        stickerPackValues.put(ANDROID_APP_DOWNLOAD_LINK_IN_QUERY, stickerPack.androidPlayStoreLink);
        stickerPackValues.put(IOS_APP_DOWNLOAD_LINK_IN_QUERY, stickerPack.iosAppStoreLink);

        return stickerPackValues;
    }
}
