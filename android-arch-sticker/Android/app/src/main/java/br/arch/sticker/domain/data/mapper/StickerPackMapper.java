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

import br.arch.sticker.core.error.throwable.sticker.FetchStickerException;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
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
    public static StickerPack writeCursorToStickerPack(Cursor cursor)
            throws FetchStickerException, StickerPackSaveException {
        final String identifier = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_IDENTIFIER_IN_QUERY));
        final String name = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_NAME_IN_QUERY));
        final String publisher = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_PUBLISHER_IN_QUERY));
        final String trayImage = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_TRAY_IMAGE_IN_QUERY));
        final String androidPlayStoreLink = cursor.getString(
                cursor.getColumnIndexOrThrow(ANDROID_APP_DOWNLOAD_LINK_IN_QUERY));
        final String iosAppLink = cursor.getString(cursor.getColumnIndexOrThrow(IOS_APP_DOWNLOAD_LINK_IN_QUERY));
        final String publisherEmail = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_EMAIL));
        final String publisherWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_WEBSITE));
        final String privacyPolicyWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PRIVACY_POLICY_WEBSITE));
        final String licenseAgreementWebsite = cursor.getString(
                cursor.getColumnIndexOrThrow(LICENSE_AGREEMENT_WEBSITE));
        final String imageDataVersion = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_DATA_VERSION));
        final boolean avoidCache = cursor.getShort(cursor.getColumnIndexOrThrow(AVOID_CACHE)) > 0;
        final boolean animatedStickerPack = cursor.getShort(cursor.getColumnIndexOrThrow(ANIMATED_STICKER_PACK)) > 0;

        final StickerPack stickerPack = new StickerPack(identifier, name, publisher, trayImage, publisherEmail,
                publisherWebsite, privacyPolicyWebsite, licenseAgreementWebsite, imageDataVersion, avoidCache,
                animatedStickerPack
        );

        stickerPack.setAndroidPlayStoreLink(androidPlayStoreLink);
        stickerPack.setIosAppStoreLink(iosAppLink);

        return stickerPack;
    }
}
