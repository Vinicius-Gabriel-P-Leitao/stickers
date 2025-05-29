/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * Modifications by Vinícius, 2025
 * Licensed under the Vinícius Non-Commercial Public License (VNCL)
 */

package com.vinicius.sticker.domain.service.load;

import static com.vinicius.sticker.domain.data.content.provider.StickerContentProvider.AUTHORITY_URI;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.ANIMATED_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.AVOID_CACHE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.IMAGE_DATA_VERSION;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.LICENSE_AGREEMENT_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.PRIVACY_POLICY_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.PUBLISHER_EMAIL;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.PUBLISHER_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.STICKER_PACK_ICON_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.STICKER_PACK_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabase.STICKER_PACK_PUBLISHER_IN_QUERY;
import static com.vinicius.sticker.domain.service.delete.StickerDeleteService.deleteStickerByIdentifier;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vinicius.sticker.BuildConfig;
import com.vinicius.sticker.core.exception.StickerFileException;
import com.vinicius.sticker.core.validation.StickerPackValidator;
import com.vinicius.sticker.core.validation.StickerValidator;
import com.vinicius.sticker.domain.builder.StickerPackParserJsonBuilder;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Busca lista com pacotes de figurinhas.
 */
public class StickerPackConsumer {

    /**
     * <b>Descrição:</b>Busca os pacotes de figurinhas direto do content provider.
     *
     * <pre>{@code
     * ArrayList<StickerPack> stickerPackList = StickerPackLoaderService.fetchStickerPacks(context);
     * }</pre>
     *
     * @param context Contexto da aplicação.
     * @return Lista de pacotes de figurinhas.
     * @throws IllegalStateException Caso não haja pacotes de figurinhas no content provider.
     */
    @NonNull
    public static ArrayList<StickerPack> fetchStickerPackList(Context context) throws IllegalStateException {
        final Cursor cursor = context.getContentResolver().query(AUTHORITY_URI, null, null, null, null);

        if (cursor == null) {
            throw new IllegalStateException("could not fetch from content provider, " + BuildConfig.CONTENT_PROVIDER_AUTHORITY);
        }

        HashSet<String> identifierSet = new HashSet<>();
        final ArrayList<StickerPack> stickerPackList;

        if (cursor.moveToFirst()) {
            stickerPackList = new ArrayList<>(fetchListFromContentProvider(cursor, context));
        } else {
            cursor.close();
            throw new IllegalStateException("No sticker packs found in the content provider");
        }

        for (StickerPack stickerPack : stickerPackList) {
            if (identifierSet.contains(stickerPack.identifier)) {
                throw new IllegalStateException(
                        "sticker pack identifiers should be unique, there are more than one pack with identifier: " + stickerPack.identifier);
            } else {
                identifierSet.add(stickerPack.identifier);
            }
        }

        if (stickerPackList.isEmpty()) {
            throw new IllegalStateException("There should be at least one sticker pack in the app");
        }

        for (StickerPack stickerPack : stickerPackList) {
            try {
                StickerPackValidator.verifyStickerPackValidity(context, stickerPack);

                for (Sticker sticker : stickerPack.getStickers()) {
                    StickerValidator.verifyStickerValidity(context, stickerPack.identifier, sticker, stickerPack.animatedStickerPack);
                }

                stickerPack.setStickers(stickerPack.getStickers());
            } catch (IllegalStateException exception) {
                if (exception instanceof StickerFileException sizeFileLimitException) {
                    // TODO: Trocar por método que vai marcar no banco de dados o pacote e figurinha.
                    deleteStickerByIdentifier(context, sizeFileLimitException.getStickerPackIdentifier(), sizeFileLimitException.getFileName());
                }
            }
        }

        return stickerPackList;
    }

    public static StickerPack fetchStickerPack(Context context, String stickerPackIdentifier) throws IllegalStateException {
        Cursor cursor = context.getContentResolver().query(Uri.withAppendedPath(AUTHORITY_URI, stickerPackIdentifier), null, null, null, null);

        if (cursor == null) {
            throw new IllegalStateException("could not fetch from content provider, " + BuildConfig.CONTENT_PROVIDER_AUTHORITY);
        }

        final StickerPack stickerPack;

        if (cursor.moveToFirst()) {
            stickerPack = fetchFromContentProvider(cursor, context);
        } else {
            cursor.close();
            throw new IllegalStateException("No sticker packs found in the content provider");
        }

        try {
            StickerPackValidator.verifyStickerPackValidity(context, stickerPack);

            for (Sticker sticker : stickerPack.getStickers()) {
                StickerValidator.verifyStickerValidity(context, stickerPack.identifier, sticker, stickerPack.animatedStickerPack);
            }

            stickerPack.setStickers(stickerPack.getStickers());
        } catch (IllegalStateException exception) {
            if (exception instanceof StickerFileException sizeFileLimitException) {
                // TODO: Trocar por método que vai marcar no banco de dados o pacote e figurinha.
                deleteStickerByIdentifier(context, sizeFileLimitException.getStickerPackIdentifier(), sizeFileLimitException.getFileName());
            }
        }

        return stickerPack;
    }

    /**
     * <b>Descrição:</b>De fato busca direto do content provider instanciando o .
     *
     * @param cursor Cursor com os pacotes de figurinhas.
     * @return Lista de pacotes de figurinhas.
     */
    @NonNull
    private static ArrayList<StickerPack> fetchListFromContentProvider(Cursor cursor, Context context) {
        ArrayList<StickerPack> stickerPackList = new ArrayList<>();
        cursor.moveToFirst();

        do {
            StickerPack stickerPack = fetchFromContentProvider(cursor, context);
            stickerPackList.add(stickerPack);
        } while (cursor.moveToNext());

        return stickerPackList;
    }

    @NonNull
    public static StickerPack fetchFromContentProvider(Cursor cursor, Context context) {
        final String identifier = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_IDENTIFIER_IN_QUERY));
        final String name = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_NAME_IN_QUERY));
        final String publisher = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_PUBLISHER_IN_QUERY));
        final String trayImage = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_ICON_IN_QUERY));
        final String androidPlayStoreLink = cursor.getString(cursor.getColumnIndexOrThrow(ANDROID_APP_DOWNLOAD_LINK_IN_QUERY));
        final String iosAppLink = cursor.getString(cursor.getColumnIndexOrThrow(IOS_APP_DOWNLOAD_LINK_IN_QUERY));
        final String publisherEmail = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_EMAIL));
        final String publisherWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHER_WEBSITE));
        final String privacyPolicyWebsite = cursor.getString(cursor.getColumnIndexOrThrow(PRIVACY_POLICY_WEBSITE));
        final String licenseAgreementWebsite = cursor.getString(cursor.getColumnIndexOrThrow(LICENSE_AGREEMENT_WEBSITE));
        final String imageDataVersion = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_DATA_VERSION));
        final boolean avoidCache = cursor.getShort(cursor.getColumnIndexOrThrow(AVOID_CACHE)) > 0;
        final boolean animatedStickerPack = cursor.getShort(cursor.getColumnIndexOrThrow(ANIMATED_STICKER_PACK)) > 0;

        final StickerPack stickerPack = new StickerPack(
                identifier, name, publisher, trayImage, publisherEmail, publisherWebsite, privacyPolicyWebsite, licenseAgreementWebsite,
                imageDataVersion, avoidCache, animatedStickerPack);

        List<Sticker> stickers = StickerConsumer.getStickersForPack(context, identifier);
        stickerPack.setStickers(stickers);

        stickerPack.setAndroidPlayStoreLink(androidPlayStoreLink);
        stickerPack.setIosAppStoreLink(iosAppLink);

        return stickerPack;
    }
}
