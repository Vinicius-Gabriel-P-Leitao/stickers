/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.mapper;

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

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerException;
import br.arch.sticker.domain.data.model.StickerPack;

public class StickerPackMapper {
    @NonNull
    public static ContentValues writeStickerPackToContentValues(StickerPack stickerPack) {
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

    @NonNull
    public static ContentValues writeCleanUrlStickerPackToContentValues() {
        ContentValues contentValueStickerPack = new ContentValues();
        contentValueStickerPack.put(PUBLISHER_EMAIL, "");
        contentValueStickerPack.put(PUBLISHER_WEBSITE, "");
        contentValueStickerPack.put(PRIVACY_POLICY_WEBSITE, "");
        contentValueStickerPack.put(LICENSE_AGREEMENT_WEBSITE, "");
        contentValueStickerPack.put(AVOID_CACHE, "");
        contentValueStickerPack.put(ANDROID_APP_DOWNLOAD_LINK_IN_QUERY, "");
        contentValueStickerPack.put(IOS_APP_DOWNLOAD_LINK_IN_QUERY, "");

        return contentValueStickerPack;
    }

    @NonNull
    public static StickerPack writeCursorToStickerPack(Cursor cursor) throws FetchStickerException {
        try {
            final String identifier = getStringOrThrow(cursor, STICKER_PACK_IDENTIFIER_IN_QUERY);
            final String name = getStringOrThrow(cursor, STICKER_PACK_NAME_IN_QUERY);
            final String publisher = getStringOrThrow(cursor, STICKER_PACK_PUBLISHER_IN_QUERY);
            final String trayImage = getStringOrThrow(cursor, STICKER_PACK_TRAY_IMAGE_IN_QUERY);
            final String androidPlayStoreLink = getStringOrThrow(cursor, ANDROID_APP_DOWNLOAD_LINK_IN_QUERY);
            final String iosAppLink = getStringOrThrow(cursor, IOS_APP_DOWNLOAD_LINK_IN_QUERY);
            final String publisherEmail = getStringOrThrow(cursor, PUBLISHER_EMAIL);
            final String publisherWebsite = getStringOrThrow(cursor, PUBLISHER_WEBSITE);
            final String privacyPolicyWebsite = getStringOrThrow(cursor, PRIVACY_POLICY_WEBSITE);
            final String licenseAgreementWebsite = getStringOrThrow(cursor, LICENSE_AGREEMENT_WEBSITE);
            final String imageDataVersion = getStringOrThrow(cursor, IMAGE_DATA_VERSION);
            final boolean avoidCache = getBooleanOrThrow(cursor, AVOID_CACHE);
            final boolean animatedStickerPack = getBooleanOrThrow(cursor, ANIMATED_STICKER_PACK);

            final StickerPack stickerPack = new StickerPack(identifier, name, publisher, trayImage, publisherEmail, publisherWebsite,
                    privacyPolicyWebsite, licenseAgreementWebsite, imageDataVersion, avoidCache, animatedStickerPack
            );

            stickerPack.setAndroidPlayStoreLink(androidPlayStoreLink);
            stickerPack.setIosAppStoreLink(iosAppLink);

            return stickerPack;

        } catch (IllegalArgumentException exception) {
            throw new FetchStickerException("Error mapping cursor!", exception, ErrorCode.ERROR_CURSOR_NULL);
        }
    }

    private static String getStringOrThrow(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
    }

    private static boolean getBooleanOrThrow(Cursor cursor, String columnName) {
        return cursor.getShort(cursor.getColumnIndexOrThrow(columnName)) > 0;
    }
}
