/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.data.content.provider;

import static com.vinicius.sticker.domain.data.database.StickerDatabase.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.ANIMATED_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.AVOID_CACHE;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.IMAGE_DATA_VERSION;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.LICENSE_AGREEMENT_WEBSITE;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.PRIVACY_POLICY_WEBSITE;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.PUBLISHER_EMAIL;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.PUBLISHER_WEBSITE;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_FILE_EMOJI_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_FILE_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_PACK_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_PACK_PUBLISHER_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_PACK_TRAY_IMAGE_IN_QUERY;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vinicius.sticker.domain.data.database.StickerDatabase;
import com.vinicius.sticker.domain.data.database.repository.SelectStickerPackRepo;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;

import java.util.ArrayList;
import java.util.List;

public class StickerPackQueryProvider {
    @NonNull
    public static Cursor getListStickerPackInfo(@NonNull Context context, @NonNull Uri uri, @NonNull List<StickerPack> stickerPackList) {
        MatrixCursor cursor = new MatrixCursor(
                new String[]{STICKER_PACK_IDENTIFIER_IN_QUERY, STICKER_PACK_NAME_IN_QUERY, STICKER_PACK_PUBLISHER_IN_QUERY, STICKER_PACK_TRAY_IMAGE_IN_QUERY, ANDROID_APP_DOWNLOAD_LINK_IN_QUERY, IOS_APP_DOWNLOAD_LINK_IN_QUERY, PUBLISHER_EMAIL, PUBLISHER_WEBSITE, PRIVACY_POLICY_WEBSITE, LICENSE_AGREEMENT_WEBSITE, IMAGE_DATA_VERSION, AVOID_CACHE, ANIMATED_STICKER_PACK,});

        for (StickerPack stickerPack : stickerPackList) {
            MatrixCursor.RowBuilder builder = cursor.newRow();
            builder.add(stickerPack.identifier);
            builder.add(stickerPack.name);
            builder.add(stickerPack.publisher);
            builder.add(stickerPack.trayImageFile);
            builder.add(stickerPack.androidPlayStoreLink);
            builder.add(stickerPack.iosAppStoreLink);
            builder.add(stickerPack.publisherEmail);
            builder.add(stickerPack.publisherWebsite);
            builder.add(stickerPack.privacyPolicyWebsite);
            builder.add(stickerPack.licenseAgreementWebsite);
            builder.add(stickerPack.imageDataVersion);
            builder.add(stickerPack.avoidCache ? 1 : 0);
            builder.add(stickerPack.animatedStickerPack ? 1 : 0);
        }

        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    @NonNull
    public static Cursor getStickerPackInfo(@NonNull Context context, @NonNull Uri uri, @NonNull StickerPack stickerPack) {
        MatrixCursor cursor = new MatrixCursor(
                new String[]{STICKER_PACK_IDENTIFIER_IN_QUERY, STICKER_PACK_NAME_IN_QUERY, STICKER_PACK_PUBLISHER_IN_QUERY, STICKER_PACK_TRAY_IMAGE_IN_QUERY, ANDROID_APP_DOWNLOAD_LINK_IN_QUERY, IOS_APP_DOWNLOAD_LINK_IN_QUERY, PUBLISHER_EMAIL, PUBLISHER_WEBSITE, PRIVACY_POLICY_WEBSITE, LICENSE_AGREEMENT_WEBSITE, IMAGE_DATA_VERSION, AVOID_CACHE, ANIMATED_STICKER_PACK,});

        MatrixCursor.RowBuilder builder = cursor.newRow();
        builder.add(stickerPack.identifier);
        builder.add(stickerPack.name);
        builder.add(stickerPack.publisher);
        builder.add(stickerPack.trayImageFile);
        builder.add(stickerPack.androidPlayStoreLink);
        builder.add(stickerPack.iosAppStoreLink);
        builder.add(stickerPack.publisherEmail);
        builder.add(stickerPack.publisherWebsite);
        builder.add(stickerPack.privacyPolicyWebsite);
        builder.add(stickerPack.licenseAgreementWebsite);
        builder.add(stickerPack.imageDataVersion);
        builder.add(stickerPack.avoidCache ? 1 : 0);
        builder.add(stickerPack.animatedStickerPack ? 1 : 0);

        cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    public static List<StickerPack> getListStickerPackFromDatabase(StickerDatabase dbHelper) {
        Cursor cursor = SelectStickerPackRepo.getAllStickerPacks(dbHelper);
        List<StickerPack> stickerPackList = new ArrayList<>();

        try {
            String currentIdentifier = null;
            StickerPack currentStickerPack = null;

            if (cursor.moveToFirst()) {
                do {
                    String identifier = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_IDENTIFIER_IN_QUERY));

                    if (!identifier.equals(currentIdentifier)) {
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_NAME_IN_QUERY));
                        String publisher = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_PUBLISHER_IN_QUERY));
                        String trayImageFile = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_TRAY_IMAGE_IN_QUERY));
                        String imageDataVersion = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_DATA_VERSION));
                        boolean avoidCache = cursor.getInt(cursor.getColumnIndexOrThrow(AVOID_CACHE)) != 0;
                        String publisherEmail = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_EMAIL));
                        String publisherWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_WEBSITE));
                        String privacyPolicyWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PRIVACY_POLICY_WEBSITE));
                        String licenseAgreementWebsite = cursor.getString(cursor.getColumnIndexOrThrow(LICENSE_AGREEMENT_WEBSITE));
                        boolean animatedStickerPack = cursor.getInt(cursor.getColumnIndexOrThrow(ANIMATED_STICKER_PACK)) != 0;

                        currentStickerPack = new StickerPack(
                                identifier, name, publisher, trayImageFile, publisherEmail, publisherWebsite, privacyPolicyWebsite,
                                licenseAgreementWebsite, imageDataVersion, avoidCache, animatedStickerPack);

                        currentStickerPack.setStickers(new ArrayList<>());
                        stickerPackList.add(currentStickerPack);
                        currentIdentifier = identifier;
                    }

                    String imageFile = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_NAME_IN_QUERY));
                    String emojis = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_EMOJI_IN_QUERY));
                    String accessibilityText = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY));

                    Sticker sticker = new Sticker(imageFile, emojis, accessibilityText);
                    currentStickerPack.getStickers().add(sticker);

                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return stickerPackList;
    }

    public static StickerPack getStickerPackFromDatabase(@NonNull StickerDatabase dbHelper, @Nullable String stickerPackIdentifier) {
        Cursor cursor = SelectStickerPackRepo.getStickerPackByIdentifier(dbHelper, stickerPackIdentifier);
        StickerPack stickerPack = null;

        try {
            if (cursor.moveToFirst()) {
                String identifier = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_IDENTIFIER_IN_QUERY));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_NAME_IN_QUERY));
                String publisher = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_PUBLISHER_IN_QUERY));
                String trayImageFile = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_TRAY_IMAGE_IN_QUERY));
                String publisherEmail = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_EMAIL));
                String publisherWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_WEBSITE));
                String privacyPolicyWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PRIVACY_POLICY_WEBSITE));
                String licenseAgreementWebsite = cursor.getString(cursor.getColumnIndexOrThrow(LICENSE_AGREEMENT_WEBSITE));
                boolean animatedStickerPack = cursor.getInt(cursor.getColumnIndexOrThrow(ANIMATED_STICKER_PACK)) != 0;
                String imageDataVersion = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_DATA_VERSION));
                boolean avoidCache = cursor.getInt(cursor.getColumnIndexOrThrow(AVOID_CACHE)) != 0;

                List<Sticker> stickerList = new ArrayList<>();
                do {
                    String imageFile = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_NAME_IN_QUERY));
                    String emojis = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_EMOJI_IN_QUERY));
                    String accessibilityText = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY));

                    Sticker sticker = new Sticker(imageFile, emojis, accessibilityText);
                    stickerList.add(sticker);
                } while (cursor.moveToNext());

                stickerPack = new StickerPack(
                        identifier, name, publisher, trayImageFile, publisherEmail, publisherWebsite, privacyPolicyWebsite, licenseAgreementWebsite,
                        imageDataVersion, avoidCache, animatedStickerPack);

                stickerPack.setStickers(stickerList);
            }
        } finally {
            cursor.close();
        }

        return stickerPack;
    }
}
