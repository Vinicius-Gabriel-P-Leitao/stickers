/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.data.content.helpers;

import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.ANIMATED_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.AVOID_CACHE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.IMAGE_DATA_VERSION;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.LICENSE_AGREEMENT_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.PRIVACY_POLICY_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.PUBLISHER_EMAIL;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.PUBLISHER_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_FILE_EMOJI_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_ICON_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_PUBLISHER_IN_QUERY;
import static com.vinicius.sticker.domain.manager.StickerPackLoaderManager.getStickerPackList;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.domain.manager.StickerPackLoaderManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StickerProviderQueryHelper {
    private final Context context;

    public StickerProviderQueryHelper(Context context) {
        this.context = context;
    }

    public Cursor getPackForAllStickerPacks(@NonNull Uri uri, StickerDatabaseHelper dbHelper) {
        return getStickerPackInfo(uri, StickerPackLoaderManager.getStickerPackList(dbHelper));
    }

    public Cursor getCursorForSingleStickerPack(@NonNull Uri uri, StickerDatabaseHelper dbHelper) {
        final String identifier = uri.getLastPathSegment();

        for (StickerPack stickerPack : StickerPackLoaderManager.getStickerPackList(dbHelper)) {
            if (Objects.equals(identifier, stickerPack.identifier)) {
                return getStickerPackInfo(uri, Collections.singletonList(stickerPack));
            }
        }

        return getStickerPackInfo(uri, new ArrayList<>());
    }

    @NonNull
    public Cursor getStickersForAStickerPack(@NonNull Uri uri, StickerDatabaseHelper dbHelper) {
        final String identifier = uri.getLastPathSegment();

        for (StickerPack stickerPack : getStickerPackList(dbHelper)) {
            if (Objects.equals(identifier, stickerPack.identifier)) {
                return getStickerInfo(uri, stickerPack.getStickers());
            }
        }

        return getStickerInfo(uri, new ArrayList<>());
    }

    @NonNull
    private Cursor getStickerPackInfo(
            @NonNull Uri uri, @NonNull List<StickerPack> stickerPackList) {
        MatrixCursor cursor =
                new MatrixCursor(
                        new String[] {
                            STICKER_PACK_IDENTIFIER_IN_QUERY,
                            STICKER_PACK_NAME_IN_QUERY,
                            STICKER_PACK_PUBLISHER_IN_QUERY,
                            STICKER_PACK_ICON_IN_QUERY,
                            ANDROID_APP_DOWNLOAD_LINK_IN_QUERY,
                            IOS_APP_DOWNLOAD_LINK_IN_QUERY,
                            PUBLISHER_EMAIL,
                            PUBLISHER_WEBSITE,
                            PRIVACY_POLICY_WEBSITE,
                            LICENSE_AGREEMENT_WEBSITE,
                            IMAGE_DATA_VERSION,
                            AVOID_CACHE,
                            ANIMATED_STICKER_PACK,
                        });
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
        cursor.setNotificationUri(Objects.requireNonNull(context).getContentResolver(), uri);
        return cursor;
    }

    @NonNull
    private Cursor getStickerInfo(@NonNull Uri uri, @NonNull List<Sticker> stickerList) {
        MatrixCursor cursor =
                new MatrixCursor(
                        new String[] {
                            STICKER_FILE_NAME_IN_QUERY,
                            STICKER_FILE_EMOJI_IN_QUERY,
                            STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY
                        });
        for (Sticker sticker : stickerList) {
            MatrixCursor.RowBuilder builder = cursor.newRow();
            builder.add(sticker.imageFileName);
            builder.add(sticker.emojis);
            builder.add(sticker.accessibilityText);
        }
        cursor.setNotificationUri(Objects.requireNonNull(context).getContentResolver(), uri);
        return cursor;
    }
}
