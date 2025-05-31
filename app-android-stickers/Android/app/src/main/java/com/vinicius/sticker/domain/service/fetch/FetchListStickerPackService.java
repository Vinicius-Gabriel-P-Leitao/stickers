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

package com.vinicius.sticker.domain.service.fetch;

import static com.vinicius.sticker.domain.data.content.StickerContentProvider.AUTHORITY_URI;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.ANIMATED_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.AVOID_CACHE;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.IMAGE_DATA_VERSION;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.LICENSE_AGREEMENT_WEBSITE;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.PRIVACY_POLICY_WEBSITE;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.PUBLISHER_EMAIL;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.PUBLISHER_WEBSITE;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_PACK_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_PACK_PUBLISHER_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.StickerDatabase.STICKER_PACK_TRAY_IMAGE_IN_QUERY;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.vinicius.sticker.BuildConfig;
import com.vinicius.sticker.core.exception.ContentProviderException;
import com.vinicius.sticker.core.exception.InvalidWebsiteUrlException;
import com.vinicius.sticker.core.exception.PackValidatorException;
import com.vinicius.sticker.core.exception.StickerFileException;
import com.vinicius.sticker.core.exception.StickerValidatorException;
import com.vinicius.sticker.core.pattern.StickerPackValidationResult;
import com.vinicius.sticker.core.validation.StickerPackValidator;
import com.vinicius.sticker.core.validation.StickerValidator;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Busca lista com pacotes de figurinhas.
 */
public class FetchListStickerPackService {

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
    public static StickerPackValidationResult.ListStickerPackResult fetchStickerPackList(Context context) throws IllegalStateException {
        final Cursor cursor = context.getContentResolver().query(AUTHORITY_URI, null, null, null, null);
        if (cursor == null) {
            throw new ContentProviderException("Não foi possível buscar no content provider, " + BuildConfig.CONTENT_PROVIDER_AUTHORITY);
        }

        HashSet<String> stickerPackIdentifierSet = new HashSet<>();
        final ArrayList<StickerPack> stickerPackList;

        final ArrayList<StickerPack> invalidStickerPackList = new ArrayList<>();
        final ArrayList<Sticker> invalidStickerList = new ArrayList<>();

        if (cursor.moveToFirst()) {
            stickerPackList = new ArrayList<>(fetchListFromContentProvider(cursor, context));
        } else {
            cursor.close();
            throw new ContentProviderException("Nenhum pacote de figurinhas encontrado nocontent provider");
        }

        for (StickerPack stickerPack : stickerPackList) {
            if (!stickerPackIdentifierSet.add(stickerPack.identifier)) {
                throw new ContentProviderException(
                        "Os identificadores dos pacotes de figurinhas devem ser únicos, há mais de um pacote com identificador: " +
                                stickerPack.identifier);
            }
        }

        if (stickerPackList.isEmpty()) {
            throw new ContentProviderException("Deve haver pelo menos um pacote de adesivos no aplicativo");
        }

        stickerPackList.removeIf(stickerPack -> {
            try {
                StickerPackValidator.verifyStickerPackValidity(context, stickerPack);

                stickerPack.getStickers().removeIf(sticker -> {
                    try {
                        StickerValidator.verifyStickerValidity(context, stickerPack.identifier, sticker, stickerPack.animatedStickerPack);
                        return false;
                    } catch (StickerFileException | StickerValidatorException stickerFileException) {
                        invalidStickerList.add(sticker);
                        return true;
                    }
                });

                return stickerPack.getStickers().isEmpty();
            } catch (PackValidatorException | InvalidWebsiteUrlException appCoreStateException) {
                invalidStickerPackList.add(stickerPack);
                return true;
            }
        });

        return new StickerPackValidationResult.ListStickerPackResult(stickerPackList, invalidStickerPackList, invalidStickerList);
    }

    public static StickerPackValidationResult.StickerPackResult fetchStickerPack(
            Context context, String stickerPackIdentifier) throws IllegalStateException {
        Cursor cursor = context.getContentResolver().query(Uri.withAppendedPath(AUTHORITY_URI, stickerPackIdentifier), null, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            throw new ContentProviderException("Não foi possível buscar no content provider, " + BuildConfig.CONTENT_PROVIDER_AUTHORITY);
        }

        final StickerPack stickerPack;

        final Sticker[] invalidSticker = new Sticker[1];

        if (cursor.moveToFirst()) {
            stickerPack = fetchFromContentProvider(cursor, context);
        } else {
            cursor.close();
            throw new ContentProviderException("Nenhum pacote de figurinhas encontrado no content provider");
        }

        try {
            StickerPackValidator.verifyStickerPackValidity(context, stickerPack);

            stickerPack.getStickers().removeIf(sticker -> {
                try {
                    StickerValidator.verifyStickerValidity(context, stickerPack.identifier, sticker, stickerPack.animatedStickerPack);
                    return false;
                } catch (StickerFileException | StickerValidatorException exception) {
                    invalidSticker[0] = sticker;
                    return true;
                }
            });

            if (stickerPack.getStickers().isEmpty()) {
                throw new ContentProviderException("Pacote de figurinhas inválido: não restaram stickers após a validação.");
            }

            return new StickerPackValidationResult.StickerPackResult(stickerPack, invalidSticker[0]);
        } catch (PackValidatorException | InvalidWebsiteUrlException exception) {
            throw new ContentProviderException("Pacote de figurinhas inválido: " + exception.getMessage());
        }
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
        final String trayImage = cursor.getString(cursor.getColumnIndexOrThrow(STICKER_PACK_TRAY_IMAGE_IN_QUERY));
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

        List<Sticker> stickers = FetchListStickerService.fetchListStickerForPack(context, identifier);
        stickerPack.setStickers(stickers);

        stickerPack.setAndroidPlayStoreLink(androidPlayStoreLink);
        stickerPack.setIosAppStoreLink(iosAppLink);

        return stickerPack;
    }
}
