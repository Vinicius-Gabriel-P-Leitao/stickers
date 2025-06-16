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

import static br.arch.sticker.core.validation.StickerValidator.KB_IN_BYTES;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import br.arch.sticker.core.exception.throwable.content.InvalidWebsiteUrlException;
import br.arch.sticker.core.exception.throwable.sticker.PackValidatorException;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.service.fetch.FetchStickerAssetService;

public class StickerPackValidator {
    public static final int STICKER_SIZE_MIN = 3;
    public static final int STICKER_SIZE_MAX = 30;
    public static final int CHAR_COUNT_MAX = 128;
    public static final int TRAY_IMAGE_FILE_SIZE_MAX_KB = 50;
    public static final int TRAY_IMAGE_DIMENSION_MIN = 24;
    public static final int TRAY_IMAGE_DIMENSION_MAX = 512;
    public static final String PLAY_STORE_DOMAIN = "play.google.com";
    public static final String APPLE_STORE_DOMAIN = "itunes.apple.com";

    public static void verifyStickerPackValidity(@NonNull Context context, @NonNull StickerPack stickerPack) throws IllegalStateException {
        if (TextUtils.isEmpty(stickerPack.identifier)) {
            throw new PackValidatorException(
                    "O identificador do pacote de figurinhas está vazio", PackValidatorException.ErrorCode.INVALID_IDENTIFIER);
        }

        if (stickerPack.identifier.length() > CHAR_COUNT_MAX) {
            throw new PackValidatorException(
                    "O identificador do pacote de figurinhas não pode exceder " + CHAR_COUNT_MAX + " caracteres",
                    PackValidatorException.ErrorCode.INVALID_IDENTIFIER);
        }

        checkStringValidity(stickerPack.identifier);

        if (TextUtils.isEmpty(stickerPack.publisher)) {
            throw new PackValidatorException(
                    "O publisher do pacote de figurinhas está vazio, identificador do pacote de figurinhas: " + stickerPack.identifier,
                    PackValidatorException.ErrorCode.INVALID_PUBLISHER);
        }

        if (stickerPack.publisher.length() > CHAR_COUNT_MAX) {
            throw new PackValidatorException(
                    "O publisher do pacote de figurinhas não pode exceder " + CHAR_COUNT_MAX + " caracteres, identificador do pacote de figurinhas:" +
                            stickerPack.identifier, PackValidatorException.ErrorCode.INVALID_PUBLISHER);
        }

        if (TextUtils.isEmpty(stickerPack.name)) {
            throw new PackValidatorException(
                    "Nome do pacote de figurinhas está vazio, identificador do pacote de figurinhas: " + stickerPack.identifier,
                    PackValidatorException.ErrorCode.INVALID_STICKERPACK_NAME);
        }

        if (stickerPack.name.length() > CHAR_COUNT_MAX) {
            throw new PackValidatorException(
                    "O nome do pacote de figurinhas não pode exceder " + CHAR_COUNT_MAX + " caracteres, identificador do pacote de figurinhas:" +
                            stickerPack.identifier, PackValidatorException.ErrorCode.STICKERPACK_SIZE);
        }

        if (TextUtils.isEmpty(stickerPack.trayImageFile)) {
            throw new PackValidatorException(
                    "A thumbnail do pacote de figurinhas está vazio, identificador do pacote de figurinhas:" + stickerPack.identifier,
                    PackValidatorException.ErrorCode.INVALID_THUMBNAIL);
        }

        if (!TextUtils.isEmpty(stickerPack.androidPlayStoreLink) && !isValidWebsiteUrl(stickerPack.androidPlayStoreLink)) {
            throw new PackValidatorException("Certifique-se de incluir http ou https nas URL, o link da Android Play Store não é uma URL válida:" +
                    stickerPack.androidPlayStoreLink, PackValidatorException.ErrorCode.INVALID_ANDROID_URL_SITE);
        }

        if (!TextUtils.isEmpty(stickerPack.androidPlayStoreLink) && !isURLInCorrectDomain(stickerPack.androidPlayStoreLink, PLAY_STORE_DOMAIN)) {
            throw new PackValidatorException(
                    "O link da Android Play Store deve usar o domínio da Play Store:" + PLAY_STORE_DOMAIN,
                    PackValidatorException.ErrorCode.INVALID_ANDROID_URL_SITE);
        }

        if (!TextUtils.isEmpty(stickerPack.iosAppStoreLink) && !isValidWebsiteUrl(stickerPack.iosAppStoreLink)) {
            throw new PackValidatorException(
                    "Certifique-se de incluir http ou https nos links de URL, o link da loja de aplicativos iOS não é uma URL válida:" +
                            stickerPack.iosAppStoreLink, PackValidatorException.ErrorCode.INVALID_IOS_URL_SITE);
        }

        if (!TextUtils.isEmpty(stickerPack.iosAppStoreLink) && !isURLInCorrectDomain(stickerPack.iosAppStoreLink, APPLE_STORE_DOMAIN)) {
            throw new PackValidatorException(
                    "O link da loja de aplicativos iOS deve usar o domínio da loja de aplicativos:" + APPLE_STORE_DOMAIN,
                    PackValidatorException.ErrorCode.INVALID_IOS_URL_SITE);
        }

        if (!TextUtils.isEmpty(stickerPack.licenseAgreementWebsite) && !isValidWebsiteUrl(stickerPack.licenseAgreementWebsite)) {
            throw new PackValidatorException(
                    "Certifique-se de incluir http ou https nos links de URL, o link do contrato de licença não é uma URL válida:" +
                            stickerPack.licenseAgreementWebsite, PackValidatorException.ErrorCode.INVALID_WEBSITE);
        }

        if (!TextUtils.isEmpty(stickerPack.privacyPolicyWebsite) && !isValidWebsiteUrl(stickerPack.privacyPolicyWebsite)) {
            throw new PackValidatorException(
                    "Certifique-se de incluir http ou https nos links de URL, o link da política de privacidade não é uma URL válida:" +
                            stickerPack.privacyPolicyWebsite, PackValidatorException.ErrorCode.INVALID_WEBSITE);
        }

        if (!TextUtils.isEmpty(stickerPack.publisherWebsite) && !isValidWebsiteUrl(stickerPack.publisherWebsite)) {
            throw new PackValidatorException(
                    "Certifique-se de incluir http ou https nos links de URL, o link do site do editor não é uma URL válida:" +
                            stickerPack.publisherWebsite, PackValidatorException.ErrorCode.INVALID_WEBSITE);
        }

        if (!TextUtils.isEmpty(stickerPack.publisherEmail) && !Patterns.EMAIL_ADDRESS.matcher(stickerPack.publisherEmail).matches()) {
            throw new PackValidatorException(
                    "O e-mail do publisher não parece válido, o e-mail é:" + stickerPack.publisherEmail,
                    PackValidatorException.ErrorCode.INVALID_EMAIL);
        }

        try {
            final byte[] stickerAssetBytes = FetchStickerAssetService.fetchStickerAsset(stickerPack.identifier, stickerPack.trayImageFile, context);

            if (stickerAssetBytes.length > TRAY_IMAGE_FILE_SIZE_MAX_KB * KB_IN_BYTES) {
                throw new PackValidatorException(
                        "A imagem da thumbnail deve ter menos de " + TRAY_IMAGE_FILE_SIZE_MAX_KB + " KB, arquivo de thumbnail:" +
                                stickerPack.trayImageFile, PackValidatorException.ErrorCode.INVALID_THUMBNAIL);
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(stickerAssetBytes, 0, stickerAssetBytes.length);
            if (bitmap.getHeight() > TRAY_IMAGE_DIMENSION_MAX || bitmap.getHeight() < TRAY_IMAGE_DIMENSION_MIN) {
                throw new PackValidatorException(
                        "A altura da thumbnail deve estar entre" + TRAY_IMAGE_DIMENSION_MIN + " e " + TRAY_IMAGE_DIMENSION_MAX +
                                " pixels, a altura atual da imagem da bandeja é" + bitmap.getHeight() + ", arquivo: " + stickerPack.trayImageFile,
                        PackValidatorException.ErrorCode.INVALID_THUMBNAIL);
            }

            if (bitmap.getWidth() > TRAY_IMAGE_DIMENSION_MAX || bitmap.getWidth() < TRAY_IMAGE_DIMENSION_MIN) {
                throw new PackValidatorException(
                        "A largura da thumbnail deve estar entre " + TRAY_IMAGE_DIMENSION_MIN + " e " + TRAY_IMAGE_DIMENSION_MAX +
                                " pixels, a largura atual da imagem da bandeja é " + bitmap.getWidth() + ", arquivo: " + stickerPack.trayImageFile,
                        PackValidatorException.ErrorCode.INVALID_THUMBNAIL);
            }
        } catch (IOException exception) {
            throw new PackValidatorException(
                    "Não é possível abrir a thumbnail: " + stickerPack.trayImageFile, exception, PackValidatorException.ErrorCode.INVALID_THUMBNAIL);
        }

        final List<Sticker> stickers = stickerPack.getStickers();

        if (stickers.size() < STICKER_SIZE_MIN || stickers.size() > STICKER_SIZE_MAX) {
            throw new PackValidatorException("A quantidade de figurinhas do pacote deve estar entre 3 a 30, atualmente tem" + stickers.size() +
                    ", identificador do pacote de figurinhas: " + stickerPack.identifier, PackValidatorException.ErrorCode.STICKERPACK_SIZE);
        }
    }

    private static void checkStringValidity(@NonNull String string) {
        String pattern = "[\\w-.,'\\s]+"; // [a-zA-Z0-9_-.' ]
        if (!string.matches(pattern)) {
            throw new PackValidatorException(
                    string + " contém caracteres inválidos, os caracteres permitidos são de a a z, A a Z, _, ' - . e caractere de espaço",
                    PackValidatorException.ErrorCode.INVALID_STICKERPACK_NAME);
        }

        if (string.contains("..")) {
            throw new PackValidatorException(string + " não pode conter ..", PackValidatorException.ErrorCode.INVALID_STICKERPACK_NAME);
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isValidWebsiteUrl(String websiteUrl) throws InvalidWebsiteUrlException {
        try {
            new URL(websiteUrl);
        } catch (MalformedURLException exception) {
            Log.e("StickerPackValidator", "url: " + websiteUrl + " é malformado");
            throw new InvalidWebsiteUrlException("Url: " + websiteUrl + " está malformada", exception, websiteUrl);
        }

        return URLUtil.isHttpUrl(websiteUrl) || URLUtil.isHttpsUrl(websiteUrl);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isURLInCorrectDomain(String urlString, @NonNull String domain) throws InvalidWebsiteUrlException {
        try {
            URL url = new URL(urlString);

            if (domain.equals(url.getHost())) {
                return true;
            }
        } catch (MalformedURLException exception) {
            Log.e("StickerPackValidator", "url: " + urlString + " é malformado");
            throw new InvalidWebsiteUrlException("Url: " + urlString + " está malformada", exception.getCause(), urlString);
        }

        return false;
    }
}
