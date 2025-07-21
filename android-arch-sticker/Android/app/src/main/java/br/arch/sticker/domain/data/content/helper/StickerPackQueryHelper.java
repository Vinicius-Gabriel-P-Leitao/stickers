/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.data.content.helper;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.ANIMATED_STICKER_PACK;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.AVOID_CACHE;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.IMAGE_DATA_VERSION;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.LICENSE_AGREEMENT_WEBSITE;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.PRIVACY_POLICY_WEBSITE;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.PUBLISHER_EMAIL;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.PUBLISHER_WEBSITE;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_EMOJI_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_FILE_NAME_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_IS_VALID;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_PACK_NAME_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_PACK_PUBLISHER_IN_QUERY;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.STICKER_PACK_TRAY_IMAGE_IN_QUERY;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import br.arch.sticker.R;
import br.arch.sticker.core.error.throwable.content.ContentProviderException;
import br.arch.sticker.domain.data.database.StickerDatabaseHelper;
import br.arch.sticker.domain.data.database.repository.SelectStickerPackRepo;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;

public class StickerPackQueryHelper {
    private final static String TAG_LOG = StickerPackQueryHelper.class.getSimpleName();

    private final Context context;
    private final ApplicationTranslate applicationTranslate;
    private final SelectStickerPackRepo selectStickerPackRepo;

    public StickerPackQueryHelper(Context context) {
        this.context = context.getApplicationContext();
        SQLiteDatabase database = StickerDatabaseHelper.getInstance(this.context)
                .getReadableDatabase();
        this.selectStickerPackRepo = new SelectStickerPackRepo(database);
        this.applicationTranslate = new ApplicationTranslate(this.context.getResources());
    }

    @NonNull
    public Cursor fetchListStickerPackData(@NonNull Uri uri, @NonNull List<StickerPack> stickerPackList) {
        MatrixCursor cursor = new MatrixCursor(
                new String[]{STICKER_PACK_IDENTIFIER_IN_QUERY, STICKER_PACK_NAME_IN_QUERY,
                        STICKER_PACK_PUBLISHER_IN_QUERY, STICKER_PACK_TRAY_IMAGE_IN_QUERY,
                        ANDROID_APP_DOWNLOAD_LINK_IN_QUERY, IOS_APP_DOWNLOAD_LINK_IN_QUERY,
                        PUBLISHER_EMAIL, PUBLISHER_WEBSITE, PRIVACY_POLICY_WEBSITE,
                        LICENSE_AGREEMENT_WEBSITE, IMAGE_DATA_VERSION, AVOID_CACHE,
                        ANIMATED_STICKER_PACK,});

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

    public List<StickerPack> fetchListStickerPackFromDatabase() {
        Cursor cursor = selectStickerPackRepo.getAllStickerPacks();
        if (cursor == null) {
            throw new ContentProviderException(
                    applicationTranslate.translate(R.string.error_null_cursor)
                            .log(TAG_LOG, Level.ERROR).get());
        }

        List<StickerPack> stickerPackList = new ArrayList<>();

        try {
            String currentIdentifier = null;
            StickerPack currentStickerPack = null;

            if (cursor.moveToFirst()) {
                do {
                    String identifier = cursor.getString(
                            cursor.getColumnIndexOrThrow(STICKER_PACK_IDENTIFIER_IN_QUERY));

                    if (!identifier.equals(currentIdentifier)) {
                        String name = cursor.getString(
                                cursor.getColumnIndexOrThrow(STICKER_PACK_NAME_IN_QUERY));
                        String publisher = cursor.getString(
                                cursor.getColumnIndexOrThrow(STICKER_PACK_PUBLISHER_IN_QUERY));
                        String trayImageFile = cursor.getString(
                                cursor.getColumnIndexOrThrow(STICKER_PACK_TRAY_IMAGE_IN_QUERY));
                        String imageDataVersion = cursor.getString(
                                cursor.getColumnIndexOrThrow(IMAGE_DATA_VERSION));
                        boolean avoidCache =
                                cursor.getInt(cursor.getColumnIndexOrThrow(AVOID_CACHE)) != 0;
                        String publisherEmail = cursor.getString(
                                cursor.getColumnIndexOrThrow(PUBLISHER_EMAIL));
                        String publisherWebsite = cursor.getString(
                                cursor.getColumnIndexOrThrow(PUBLISHER_WEBSITE));
                        String privacyPolicyWebsite = cursor.getString(
                                cursor.getColumnIndexOrThrow(PRIVACY_POLICY_WEBSITE));
                        String licenseAgreementWebsite = cursor.getString(
                                cursor.getColumnIndexOrThrow(LICENSE_AGREEMENT_WEBSITE));
                        boolean animatedStickerPack = cursor.getInt(
                                cursor.getColumnIndexOrThrow(ANIMATED_STICKER_PACK)) != 0;

                        currentStickerPack = new StickerPack(identifier, name, publisher,
                                trayImageFile, publisherEmail, publisherWebsite,
                                privacyPolicyWebsite, licenseAgreementWebsite, imageDataVersion,
                                avoidCache, animatedStickerPack
                        );

                        currentStickerPack.setStickers(new ArrayList<>());
                        stickerPackList.add(currentStickerPack);
                        currentIdentifier = identifier;
                    }

                    String imageFile = cursor.getString(
                            cursor.getColumnIndexOrThrow(STICKER_FILE_NAME_IN_QUERY));
                    String emojis = cursor.getString(
                            cursor.getColumnIndexOrThrow(STICKER_FILE_EMOJI_IN_QUERY));
                    String stickerIsValid = cursor.getString(
                            cursor.getColumnIndexOrThrow(STICKER_IS_VALID));
                    String accessibilityText = cursor.getString(
                            cursor.getColumnIndexOrThrow(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY));

                    Sticker sticker = new Sticker(imageFile, emojis, stickerIsValid,
                            accessibilityText, identifier
                    );
                    currentStickerPack.getStickers().add(sticker);

                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return stickerPackList;
    }

    @NonNull
    public Cursor fetchStickerPackData(@NonNull Uri uri, @NonNull StickerPack stickerPack) {
        MatrixCursor cursor = new MatrixCursor(
                new String[]{STICKER_PACK_IDENTIFIER_IN_QUERY, STICKER_PACK_NAME_IN_QUERY,
                        STICKER_PACK_PUBLISHER_IN_QUERY, STICKER_PACK_TRAY_IMAGE_IN_QUERY,
                        ANDROID_APP_DOWNLOAD_LINK_IN_QUERY, IOS_APP_DOWNLOAD_LINK_IN_QUERY,
                        PUBLISHER_EMAIL, PUBLISHER_WEBSITE, PRIVACY_POLICY_WEBSITE,
                        LICENSE_AGREEMENT_WEBSITE, IMAGE_DATA_VERSION, AVOID_CACHE,
                        ANIMATED_STICKER_PACK,});

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

    public StickerPack fetchStickerPackFromDatabase(@NonNull String stickerPackIdentifier, boolean isFiltered) {
        Cursor cursor = isFiltered ? selectStickerPackRepo.getFilteredStickerPackByIdentifier(
                stickerPackIdentifier) : selectStickerPackRepo.getStickerPackByIdentifier(
                stickerPackIdentifier);

        if (cursor == null) {
            throw new ContentProviderException(
                    applicationTranslate.translate(R.string.error_null_cursor)
                            .log(TAG_LOG, Level.ERROR).get());
        }

        StickerPack stickerPack = null;

        try {
            if (cursor.moveToFirst()) {
                String identifier = cursor.getString(
                        cursor.getColumnIndexOrThrow(STICKER_PACK_IDENTIFIER_IN_QUERY));
                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow(STICKER_PACK_NAME_IN_QUERY));
                String publisher = cursor.getString(
                        cursor.getColumnIndexOrThrow(STICKER_PACK_PUBLISHER_IN_QUERY));
                String trayImageFile = cursor.getString(
                        cursor.getColumnIndexOrThrow(STICKER_PACK_TRAY_IMAGE_IN_QUERY));
                String publisherEmail = cursor.getString(
                        cursor.getColumnIndexOrThrow(PUBLISHER_EMAIL));
                String publisherWebsite = cursor.getString(
                        cursor.getColumnIndexOrThrow(PUBLISHER_WEBSITE));
                String privacyPolicyWebsite = cursor.getString(
                        cursor.getColumnIndexOrThrow(PRIVACY_POLICY_WEBSITE));
                String licenseAgreementWebsite = cursor.getString(
                        cursor.getColumnIndexOrThrow(LICENSE_AGREEMENT_WEBSITE));
                boolean animatedStickerPack =
                        cursor.getInt(cursor.getColumnIndexOrThrow(ANIMATED_STICKER_PACK)) != 0;
                String imageDataVersion = cursor.getString(
                        cursor.getColumnIndexOrThrow(IMAGE_DATA_VERSION));
                boolean avoidCache = cursor.getInt(cursor.getColumnIndexOrThrow(AVOID_CACHE)) != 0;

                List<Sticker> stickerList = new ArrayList<>();
                do {
                    String imageFile = cursor.getString(
                            cursor.getColumnIndexOrThrow(STICKER_FILE_NAME_IN_QUERY));
                    String emojis = cursor.getString(
                            cursor.getColumnIndexOrThrow(STICKER_FILE_EMOJI_IN_QUERY));
                    String stickerIsValid = cursor.getString(
                            cursor.getColumnIndexOrThrow(STICKER_IS_VALID));
                    String accessibilityText = cursor.getString(
                            cursor.getColumnIndexOrThrow(STICKER_FILE_ACCESSIBILITY_TEXT_IN_QUERY));

                    Sticker sticker = new Sticker(imageFile, emojis, stickerIsValid,
                            accessibilityText, identifier
                    );

                    stickerList.add(sticker);
                } while (cursor.moveToNext());

                stickerPack = new StickerPack(identifier, name, publisher, trayImageFile,
                        publisherEmail, publisherWebsite, privacyPolicyWebsite,
                        licenseAgreementWebsite, imageDataVersion, avoidCache, animatedStickerPack
                );

                stickerPack.setStickers(stickerList);
            }
        } finally {
            cursor.close();
        }

        return stickerPack;
    }
}
