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

package br.arch.sticker.core.validation;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.CHAR_IDENTIFIER_COUNT_MAX;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.CHAR_NAME_COUNT_MAX;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.CHAR_PUBLISHER_COUNT_MAX;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import br.arch.sticker.core.error.code.InvalidUrlErrorCode;
import br.arch.sticker.core.error.code.StickerPackErrorCode;
import br.arch.sticker.core.error.throwable.content.InvalidWebsiteUrlException;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerException;
import br.arch.sticker.core.error.throwable.sticker.StickerPackValidatorException;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.service.fetch.FetchStickerAssetService;

public class StickerPackValidator {
    public static final int STICKER_SIZE_MIN = 3;
    public static final int STICKER_SIZE_MAX = 30;
    public static final int TRAY_IMAGE_FILE_SIZE_MAX_KB = 50;
    public static final int TRAY_IMAGE_DIMENSION_MIN = 24;
    public static final int TRAY_IMAGE_DIMENSION_MAX = 512;
    public static final String PLAY_STORE_DOMAIN = "play.google.com";
    public static final String APPLE_STORE_DOMAIN = "itunes.apple.com";

    private final FetchStickerAssetService fetchStickerAssetService;

    public StickerPackValidator(Context paramContext) {
        Context context = paramContext.getApplicationContext();
        this.fetchStickerAssetService = new FetchStickerAssetService(context);
    }

    public void verifyStickerPackValidity(@NonNull StickerPack stickerPack)
            throws IllegalStateException {
        if (TextUtils.isEmpty(stickerPack.identifier)) {
            throw new StickerPackValidatorException(
                    "O identificador do pacote de figurinhas está vazio!",
                    StickerPackErrorCode.INVALID_IDENTIFIER);
        }

        if (stickerPack.identifier.length() > CHAR_IDENTIFIER_COUNT_MAX) {
            throw new StickerPackValidatorException(String.format(Locale.ROOT,
                    "O identificador do pacote de figurinhas não pode exceder %d caracteres",
                    CHAR_IDENTIFIER_COUNT_MAX), StickerPackErrorCode.INVALID_IDENTIFIER);
        }

        checkStringValidity(stickerPack.identifier);

        if (TextUtils.isEmpty(stickerPack.publisher)) {
            throw new StickerPackValidatorException(String.format(Locale.ROOT,
                    "O publisher do pacote de figurinhas está vazio, identificador do pacote de figurinhas: %s",
                    stickerPack.identifier), StickerPackErrorCode.INVALID_PUBLISHER);
        }

        if (stickerPack.publisher.length() > CHAR_PUBLISHER_COUNT_MAX) {
            throw new StickerPackValidatorException(String.format(Locale.ROOT,
                    "O publisher do pacote de figurinhas não pode exceder %d caracteres, identificador do pacote de figurinhas: %s",
                    CHAR_PUBLISHER_COUNT_MAX, stickerPack.identifier),
                    StickerPackErrorCode.INVALID_PUBLISHER);
        }

        if (TextUtils.isEmpty(stickerPack.name)) {
            throw new StickerPackValidatorException(String.format(Locale.ROOT,
                    "Nome do pacote de figurinhas está vazio, identificador do pacote de figurinhas: %s",
                    stickerPack.identifier), StickerPackErrorCode.INVALID_STICKERPACK_NAME);
        }

        if (stickerPack.name.length() > CHAR_NAME_COUNT_MAX) {
            throw new StickerPackValidatorException(String.format(Locale.ROOT,
                    "O nome do pacote de figurinhas não pode exceder %d caracteres, identificador do pacote de figurinhas: %s",
                    CHAR_NAME_COUNT_MAX, stickerPack.identifier),
                    StickerPackErrorCode.INVALID_PUBLISHER);
        }

        if (TextUtils.isEmpty(stickerPack.trayImageFile)) {
            throw new StickerPackValidatorException(String.format(Locale.ROOT,
                    "A thumbnail do pacote de figurinhas está vazia, identificador do pacote de figurinhas: %s",
                    stickerPack.identifier), StickerPackErrorCode.INVALID_THUMBNAIL);
        }

        if (!TextUtils.isEmpty(stickerPack.androidPlayStoreLink) &&
                !isValidWebsiteUrl(stickerPack.androidPlayStoreLink)) {
            throw new StickerPackValidatorException(String.format(Locale.ROOT,
                    "Certifique-se de incluir http ou https nas URL, o link da Android Play Store não é uma URL válida: %s",
                    stickerPack.androidPlayStoreLink),
                    StickerPackErrorCode.INVALID_ANDROID_URL_SITE);
        }

        if (!TextUtils.isEmpty(stickerPack.androidPlayStoreLink) &&
                !isURLInCorrectDomain(stickerPack.androidPlayStoreLink, PLAY_STORE_DOMAIN)) {
            throw new StickerPackValidatorException(String.format(Locale.ROOT,
                    "O link da Android Play Store deve usar o domínio da Play Store: %s",
                    PLAY_STORE_DOMAIN), StickerPackErrorCode.INVALID_ANDROID_URL_SITE);
        }

        if (!TextUtils.isEmpty(stickerPack.iosAppStoreLink) &&
                !isValidWebsiteUrl(stickerPack.iosAppStoreLink)) {
            throw new StickerPackValidatorException(String.format(Locale.ROOT,
                    "Certifique-se de incluir http ou https nos links de URL, o link da loja de aplicativos iOS não é uma URL válida: %s",
                    stickerPack.iosAppStoreLink), StickerPackErrorCode.INVALID_IOS_URL_SITE);
        }

        if (!TextUtils.isEmpty(stickerPack.iosAppStoreLink) &&
                !isURLInCorrectDomain(stickerPack.iosAppStoreLink, APPLE_STORE_DOMAIN)) {
            throw new StickerPackValidatorException(String.format(Locale.ROOT,
                    "O link da loja de aplicativos iOS deve usar o domínio da loja de aplicativos: %s",
                    APPLE_STORE_DOMAIN), StickerPackErrorCode.INVALID_IOS_URL_SITE);
        }

        if (!TextUtils.isEmpty(stickerPack.licenseAgreementWebsite) &&
                !isValidWebsiteUrl(stickerPack.licenseAgreementWebsite)) {
            throw new StickerPackValidatorException(String.format(Locale.ROOT,
                    "Certifique-se de incluir http ou https nos links de URL, o link do contrato de licença não é uma URL válida: %s",
                    stickerPack.licenseAgreementWebsite), StickerPackErrorCode.INVALID_WEBSITE);
        }

        if (!TextUtils.isEmpty(stickerPack.privacyPolicyWebsite) &&
                !isValidWebsiteUrl(stickerPack.privacyPolicyWebsite)) {
            throw new StickerPackValidatorException(String.format(Locale.ROOT,
                    "Certifique-se de incluir http ou https nos links de URL, o link da política de privacidade não é uma URL válida: %s",
                    stickerPack.privacyPolicyWebsite), StickerPackErrorCode.INVALID_WEBSITE);
        }

        if (!TextUtils.isEmpty(stickerPack.publisherWebsite) &&
                !isValidWebsiteUrl(stickerPack.publisherWebsite)) {
            throw new StickerPackValidatorException(String.format(Locale.ROOT,
                    "Certifique-se de incluir http ou https nos links de URL, o link do site do editor não é uma URL válida: %s",
                    stickerPack.publisherWebsite), StickerPackErrorCode.INVALID_WEBSITE);
        }

        if (!TextUtils.isEmpty(stickerPack.publisherEmail) &&
                !Patterns.EMAIL_ADDRESS.matcher(stickerPack.publisherEmail).matches()) {
            throw new StickerPackValidatorException(String.format(Locale.ROOT,
                    "O e-mail do publisher não parece válido, o e-mail é: %s",
                    stickerPack.publisherEmail), StickerPackErrorCode.INVALID_EMAIL);
        }

        try {
            final byte[] stickerAssetBytes = fetchStickerAssetService.fetchStickerAsset(
                    stickerPack.identifier, stickerPack.trayImageFile);

            if (stickerAssetBytes.length > TRAY_IMAGE_FILE_SIZE_MAX_KB * 1024) {
                throw new StickerPackValidatorException(String.format(Locale.ROOT,
                        "A imagem da thumbnail deve ter menos de %d KB, arquivo de thumbnail: %s",
                        TRAY_IMAGE_FILE_SIZE_MAX_KB, stickerPack.trayImageFile),
                        StickerPackErrorCode.INVALID_THUMBNAIL);
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(stickerAssetBytes, 0,
                    stickerAssetBytes.length);
            if (bitmap.getHeight() > TRAY_IMAGE_DIMENSION_MAX ||
                    bitmap.getHeight() < TRAY_IMAGE_DIMENSION_MIN) {
                throw new StickerPackValidatorException(String.format(Locale.ROOT,
                        "A altura da thumbnail deve estar entre %d e %d pixels, a altura atual da imagem da bandeja é %d, arquivo: %s",
                        TRAY_IMAGE_DIMENSION_MIN, TRAY_IMAGE_DIMENSION_MAX, bitmap.getHeight(),
                        stickerPack.trayImageFile), StickerPackErrorCode.INVALID_THUMBNAIL);
            }

            if (bitmap.getWidth() > TRAY_IMAGE_DIMENSION_MAX ||
                    bitmap.getWidth() < TRAY_IMAGE_DIMENSION_MIN) {
                throw new StickerPackValidatorException(String.format(Locale.ROOT,
                        "A largura da thumbnail deve estar entre %d e %d pixels, a largura atual da imagem da bandeja é %d, arquivo: %s",
                        TRAY_IMAGE_DIMENSION_MIN, TRAY_IMAGE_DIMENSION_MAX, bitmap.getWidth(),
                        stickerPack.trayImageFile), StickerPackErrorCode.INVALID_THUMBNAIL);
            }
        } catch (FetchStickerException exception) {
            throw new StickerPackValidatorException(
                    String.format(Locale.ROOT, "Não é possível abrir a thumbnail: %s",
                            stickerPack.trayImageFile), exception,
                    StickerPackErrorCode.INVALID_THUMBNAIL);
        }

        final List<Sticker> stickers = stickerPack.getStickers();

        if (stickers.size() < STICKER_SIZE_MIN || stickers.size() > STICKER_SIZE_MAX) {
            throw new StickerPackValidatorException(String.format(Locale.ROOT,
                    "A quantidade de figurinhas do pacote deve estar entre 3 a 30, atualmente tem %d, identificador do pacote de figurinhas: %s",
                    stickers.size(), stickerPack.identifier),
                    StickerPackErrorCode.INVALID_STICKERPACK_SIZE);
        }
    }

    private static void checkStringValidity(@NonNull String string) {
        String pattern = "[\\w-.,'\\s]+"; // [a-zA-Z0-9_-.' ]
        if (!string.matches(pattern)) {
            throw new StickerPackValidatorException(String.format(Locale.ROOT,
                    "%s contém caracteres inválidos, os caracteres permitidos são de a a z, A a Z, _, ' - . e caractere de espaço",
                    string), StickerPackErrorCode.INVALID_STICKERPACK_NAME);
        }

        if (string.contains("..")) {
            throw new StickerPackValidatorException(
                    String.format(Locale.ROOT, "%s não pode conter ..", string),
                    StickerPackErrorCode.INVALID_STICKERPACK_NAME);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isValidWebsiteUrl(String websiteUrl) throws InvalidWebsiteUrlException {
        try {
            new URL(websiteUrl);
        } catch (MalformedURLException exception) {
            Log.e("StickerPackValidator", "url: " + websiteUrl + " é malformado");
            throw new InvalidWebsiteUrlException(
                    String.format("Url: %s está malformada", websiteUrl), exception,
                    InvalidUrlErrorCode.INVALID_URL, websiteUrl);
        }

        return URLUtil.isHttpUrl(websiteUrl) || URLUtil.isHttpsUrl(websiteUrl);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isURLInCorrectDomain(String urlString, @NonNull String domain)
            throws InvalidWebsiteUrlException {
        try {
            URL url = new URL(urlString);

            if (domain.equals(url.getHost())) {
                return true;
            }

        } catch (MalformedURLException exception) {
            Log.e("StickerPackValidator", "url: " + urlString + " é malformado");
            throw new InvalidWebsiteUrlException(
                    String.format("Url: %s está malformada", urlString), exception.getCause(),
                    InvalidUrlErrorCode.INVALID_URL, urlString);
        }

        return false;
    }
}