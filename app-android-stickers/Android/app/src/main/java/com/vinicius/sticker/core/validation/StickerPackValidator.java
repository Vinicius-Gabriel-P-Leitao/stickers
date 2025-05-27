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

package com.vinicius.sticker.core.validation;

import static com.vinicius.sticker.core.validation.StickerValidator.KB_IN_BYTES;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;

import com.vinicius.sticker.core.exception.InvalidWebsiteUrlException;
import com.vinicius.sticker.core.exception.PackValidatorException;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.domain.service.load.StickerLoaderService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class StickerPackValidator {
    public static final int STICKER_SIZE_MIN = 3;
    public static final int STICKER_SIZE_MAX = 30;
    public static final int CHAR_COUNT_MAX = 128;
    public static final int TRAY_IMAGE_FILE_SIZE_MAX_KB = 50;
    public static final int TRAY_IMAGE_DIMENSION_MIN = 24;
    public static final int TRAY_IMAGE_DIMENSION_MAX = 512;
    public static final String PLAY_STORE_DOMAIN = "play.google.com";
    public static final String APPLE_STORE_DOMAIN = "itunes.apple.com";

    /**
     * Checks whether a sticker pack contains valid data
     */
    public static void verifyStickerPackValidity(@NonNull Context context, @NonNull StickerPack stickerPack) throws IllegalStateException {
        if (TextUtils.isEmpty(stickerPack.identifier)) {
            throw new PackValidatorException("O identificador do pacote de figurinhas está vazio");
        }
        if (stickerPack.identifier.length() > CHAR_COUNT_MAX) {
            throw new PackValidatorException("O identificador do pacote de figurinhas não pode exceder " + CHAR_COUNT_MAX + " caracteres");
        }

        checkStringValidity(stickerPack.identifier);

        if (TextUtils.isEmpty(stickerPack.publisher)) {
            throw new PackValidatorException(
                    "O publisher do pacote de figurinhas está vazio, identificador do pacote de figurinhas: " + stickerPack.identifier);
        }
        if (stickerPack.publisher.length() > CHAR_COUNT_MAX) {
            throw new PackValidatorException(
                    "O publisher do pacote de figurinhas não pode exceder " + CHAR_COUNT_MAX + " caracteres, identificador do pacote de figurinhas:" +
                    stickerPack.identifier);
        }
        if (TextUtils.isEmpty(stickerPack.name)) {
            throw new PackValidatorException(
                    "Nome do pacote de figurinhas está vazio, identificador do pacote de figurinhas: " + stickerPack.identifier);
        }
        if (stickerPack.name.length() > CHAR_COUNT_MAX) {
            throw new PackValidatorException(
                    "O nome do pacote de figurinhas não pode exceder " + CHAR_COUNT_MAX + " caracteres, identificador do pacote de figurinhas:" +
                    stickerPack.identifier);
        }
        if (TextUtils.isEmpty(stickerPack.trayImageFile)) {
            throw new PackValidatorException(
                    "O ID do pacote de figurinhas está vazio, identificador do pacote de figurinhas:" + stickerPack.identifier);
        }
        if (!TextUtils.isEmpty(stickerPack.androidPlayStoreLink) && !isValidWebsiteUrl(stickerPack.androidPlayStoreLink)) {
            throw new PackValidatorException(
                    "Certifique-se de incluir http ou https nas URL, o link da Android Play Store não é uma URL válida:" +
                    stickerPack.androidPlayStoreLink);
        }
        if (!TextUtils.isEmpty(stickerPack.androidPlayStoreLink) && !isURLInCorrectDomain(stickerPack.androidPlayStoreLink, PLAY_STORE_DOMAIN)) {
            throw new PackValidatorException("O link da Android Play Store deve usar o domínio da Play Store:" + PLAY_STORE_DOMAIN);
        }
        if (!TextUtils.isEmpty(stickerPack.iosAppStoreLink) && !isValidWebsiteUrl(stickerPack.iosAppStoreLink)) {
            throw new PackValidatorException(
                    "Certifique-se de incluir http ou https nos links de URL, o link da loja de aplicativos iOS não é uma URL válida:" +
                    stickerPack.iosAppStoreLink);
        }
        if (!TextUtils.isEmpty(stickerPack.iosAppStoreLink) && !isURLInCorrectDomain(stickerPack.iosAppStoreLink, APPLE_STORE_DOMAIN)) {
            throw new PackValidatorException(
                    "O link da loja de aplicativos iOS deve usar o domínio da loja de aplicativos:" + APPLE_STORE_DOMAIN);
        }
        if (!TextUtils.isEmpty(stickerPack.licenseAgreementWebsite) && !isValidWebsiteUrl(stickerPack.licenseAgreementWebsite)) {
            throw new PackValidatorException(
                    "Certifique-se de incluir http ou https nos links de URL, o link do contrato de licença não é uma URL válida:" +
                    stickerPack.licenseAgreementWebsite);
        }
        if (!TextUtils.isEmpty(stickerPack.privacyPolicyWebsite) && !isValidWebsiteUrl(stickerPack.privacyPolicyWebsite)) {
            throw new PackValidatorException(
                    "Certifique-se de incluir http ou https nos links de URL, o link da política de privacidade não é uma URL válida:" +
                    stickerPack.privacyPolicyWebsite);
        }
        if (!TextUtils.isEmpty(stickerPack.publisherWebsite) && !isValidWebsiteUrl(stickerPack.publisherWebsite)) {
            throw new PackValidatorException(
                    "Certifique-se de incluir http ou https nos links de URL, o link do site do editor não é uma URL válida:" +
                    stickerPack.publisherWebsite);
        }
        if (!TextUtils.isEmpty(stickerPack.publisherEmail) && !Patterns.EMAIL_ADDRESS.matcher(stickerPack.publisherEmail).matches()) {
            throw new PackValidatorException("O e-mail do publisher não parece válido, o e-mail é:" + stickerPack.publisherEmail);
        }
        try {
            final byte[] stickerAssetBytes =
                    StickerLoaderService.fetchStickerAsset(stickerPack.identifier, stickerPack.trayImageFile, context.getContentResolver());
            if (stickerAssetBytes.length > TRAY_IMAGE_FILE_SIZE_MAX_KB * KB_IN_BYTES) {
                throw new PackValidatorException(
                        "A imagem da thumbnail deve ter menos de " + TRAY_IMAGE_FILE_SIZE_MAX_KB + " KB, arquivo de thumbnail:" +
                        stickerPack.trayImageFile);
            }
            Bitmap bitmap = BitmapFactory.decodeByteArray(stickerAssetBytes, 0, stickerAssetBytes.length);
            if (bitmap.getHeight() > TRAY_IMAGE_DIMENSION_MAX || bitmap.getHeight() < TRAY_IMAGE_DIMENSION_MIN) {
                throw new PackValidatorException(
                        "A altura da thumbnail deve estar entre" + TRAY_IMAGE_DIMENSION_MIN + " e " + TRAY_IMAGE_DIMENSION_MAX +
                        " pixels, a altura atual da imagem da bandeja é" + bitmap.getHeight() + ", arquivo: " + stickerPack.trayImageFile);
            }
            if (bitmap.getWidth() > TRAY_IMAGE_DIMENSION_MAX || bitmap.getWidth() < TRAY_IMAGE_DIMENSION_MIN) {
                throw new PackValidatorException(
                        "A largura da thumbnail deve estar entre " + TRAY_IMAGE_DIMENSION_MIN + " e " + TRAY_IMAGE_DIMENSION_MAX +
                        " pixels, a largura atual da imagem da bandeja é " + bitmap.getWidth() + ", arquivo: " + stickerPack.trayImageFile);
            }
        } catch (IOException exception) {
            throw new PackValidatorException("Não é possível abrir a thumbnail, " + stickerPack.trayImageFile, exception);
        }

        final List<Sticker> stickers = stickerPack.getStickers();
        if (stickers.size() < STICKER_SIZE_MIN || stickers.size() > STICKER_SIZE_MAX) {
            throw new PackValidatorException(
                    "A quantidade de figurinhas do pacote deve estar entre 3 a 30, atualmente tem" + stickers.size() +
                    ", identificador do pacote de figurinhas: " + stickerPack.identifier);
        }

    }

    /**
     * <p><b>Descrição:</b>Fáz validação simples de string para validar o identifer do stickerpack, mesmo que seja uuid vale validar.</p>
     *
     * @param string String a ser validada por um regex.
     */
    private static void checkStringValidity(@NonNull String string) {
        String pattern = "[\\w-.,'\\s]+"; // [a-zA-Z0-9_-.' ]
        if (!string.matches(pattern)) {
            throw new IllegalStateException(
                    string + " contém caracteres inválidos, os caracteres permitidos são de a a z, A a Z, _, ' - . e caractere de espaço");
        }
        if (string.contains("..")) {
            throw new IllegalStateException(string + " não pode conter ..");
        }
    }

    /**
     * <p><b>Descrição:</b>Fáz validação simples de url.</p>
     *
     * @param websiteUrl Url do site que está no pacote.
     * @throws IllegalStateException Caso a url não seja válida.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isValidWebsiteUrl(String websiteUrl) throws IllegalStateException {
        try {
            new URL(websiteUrl);
        } catch (MalformedURLException exception) {
            Log.e("StickerPackValidator", "url: " + websiteUrl + " é malformado");
            throw new InvalidWebsiteUrlException("url está malformada", exception, websiteUrl);
        }

        return URLUtil.isHttpUrl(websiteUrl) || URLUtil.isHttpsUrl(websiteUrl);
    }

    /**
     * <p><b>Descrição:</b>Fáz validação simples de dominio do link.</p>
     *
     * @param urlString Url do app que está no pacote.
     * @param domain    Dominio do site que fica o app.
     * @throws IllegalStateException Caso a url não seja válida.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isURLInCorrectDomain(String urlString, @NonNull String domain) throws IllegalStateException {
        try {
            URL url = new URL(urlString);
            if (domain.equals(url.getHost())) {
                return true;
            }
        } catch (MalformedURLException exception) {
            Log.e("StickerPackValidator", "url: " + urlString + " é malformado");
            throw new InvalidWebsiteUrlException("url está malformada", exception.getCause(), urlString);
        }

        return false;
    }
}
