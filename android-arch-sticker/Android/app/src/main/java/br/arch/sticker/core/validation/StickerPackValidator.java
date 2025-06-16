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

import static br.arch.sticker.domain.data.database.StickerDatabase.CHAR_IDENTIFIER_COUNT_MAX;
import static br.arch.sticker.domain.data.database.StickerDatabase.CHAR_NAME_COUNT_MAX;
import static br.arch.sticker.domain.data.database.StickerDatabase.CHAR_PUBLISHER_COUNT_MAX;

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

import br.arch.sticker.core.exception.factory.StickerPackExceptionFactory;
import br.arch.sticker.core.exception.throwable.content.InvalidWebsiteUrlException;
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

    public static void verifyStickerPackValidity(@NonNull Context context, @NonNull StickerPack stickerPack) throws IllegalStateException {
        if (TextUtils.isEmpty(stickerPack.identifier)) {
            throw StickerPackExceptionFactory.emptyStickerPackIdentifier();
        }

        if (stickerPack.identifier.length() > CHAR_IDENTIFIER_COUNT_MAX) {
            throw StickerPackExceptionFactory.invalidSizeStickerPackIdentifier(CHAR_IDENTIFIER_COUNT_MAX);
        }

        checkStringValidity(stickerPack.identifier);

        if (TextUtils.isEmpty(stickerPack.publisher)) {
            throw StickerPackExceptionFactory.emptyStickerPackPublisher(stickerPack.identifier);
        }

        if (stickerPack.publisher.length() > CHAR_PUBLISHER_COUNT_MAX) {
            throw StickerPackExceptionFactory.invalidSizePublisherStickerPack(
                    CHAR_PUBLISHER_COUNT_MAX,
                    stickerPack.identifier);
        }

        if (TextUtils.isEmpty(stickerPack.name)) {
            throw StickerPackExceptionFactory.emptyStickerPackName(stickerPack.identifier);
        }

        if (stickerPack.name.length() > CHAR_NAME_COUNT_MAX) {
            throw StickerPackExceptionFactory.invalidSizeNameStickerPack(
                    CHAR_NAME_COUNT_MAX,
                    stickerPack.identifier);
        }

        if (TextUtils.isEmpty(stickerPack.trayImageFile)) {
            throw StickerPackExceptionFactory.emptyTrayImage(stickerPack.identifier);
        }

        if (!TextUtils.isEmpty(stickerPack.androidPlayStoreLink) && !isValidWebsiteUrl(stickerPack.androidPlayStoreLink)) {
            throw StickerPackExceptionFactory.invalidAndroidPlayStoreUrl(stickerPack.androidPlayStoreLink);
        }

        if (!TextUtils.isEmpty(stickerPack.androidPlayStoreLink) && !isURLInCorrectDomain(
                stickerPack.androidPlayStoreLink,
                PLAY_STORE_DOMAIN)) {
            throw StickerPackExceptionFactory.invalidAndroidPlayStoreDomain(PLAY_STORE_DOMAIN);
        }

        if (!TextUtils.isEmpty(stickerPack.iosAppStoreLink) && !isValidWebsiteUrl(stickerPack.iosAppStoreLink)) {
            throw StickerPackExceptionFactory.invalidIosAppStoreUrl(stickerPack.iosAppStoreLink);
        }

        if (!TextUtils.isEmpty(stickerPack.iosAppStoreLink) && !isURLInCorrectDomain(
                stickerPack.iosAppStoreLink,
                APPLE_STORE_DOMAIN)) {
            throw StickerPackExceptionFactory.invalidIosAppStoreDomain(APPLE_STORE_DOMAIN);
        }

        if (!TextUtils.isEmpty(stickerPack.licenseAgreementWebsite) && !isValidWebsiteUrl(stickerPack.licenseAgreementWebsite)) {
            throw StickerPackExceptionFactory.invalidLicenseAgreementUrl(stickerPack.licenseAgreementWebsite);
        }

        if (!TextUtils.isEmpty(stickerPack.privacyPolicyWebsite) && !isValidWebsiteUrl(stickerPack.privacyPolicyWebsite)) {
            throw StickerPackExceptionFactory.invalidPrivacyPolicyUrl(stickerPack.privacyPolicyWebsite);
        }

        if (!TextUtils.isEmpty(stickerPack.publisherWebsite) && !isValidWebsiteUrl(stickerPack.publisherWebsite)) {
            throw StickerPackExceptionFactory.invalidPublisherWebsite(stickerPack.publisherWebsite);
        }

        if (!TextUtils.isEmpty(stickerPack.publisherEmail) && !Patterns.EMAIL_ADDRESS.matcher(stickerPack.publisherEmail).matches()) {
            throw StickerPackExceptionFactory.invalidPublisherEmail(stickerPack.publisherEmail);
        }

        try {
            final byte[] stickerAssetBytes = FetchStickerAssetService.fetchStickerAsset(
                    stickerPack.identifier,
                    stickerPack.trayImageFile,
                    context);

            if (stickerAssetBytes.length > TRAY_IMAGE_FILE_SIZE_MAX_KB * 1024) {
                throw StickerPackExceptionFactory.trayImageTooLarge(
                        stickerPack.trayImageFile,
                        TRAY_IMAGE_FILE_SIZE_MAX_KB);
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(stickerAssetBytes, 0, stickerAssetBytes.length);
            if (bitmap.getHeight() > TRAY_IMAGE_DIMENSION_MAX || bitmap.getHeight() < TRAY_IMAGE_DIMENSION_MIN) {
                throw StickerPackExceptionFactory.invalidTrayImageHeight(
                        stickerPack.trayImageFile,
                        bitmap.getHeight(),
                        TRAY_IMAGE_DIMENSION_MIN,
                        TRAY_IMAGE_DIMENSION_MAX);
            }

            if (bitmap.getWidth() > TRAY_IMAGE_DIMENSION_MAX || bitmap.getWidth() < TRAY_IMAGE_DIMENSION_MIN) {
                throw StickerPackExceptionFactory.invalidTrayImageWidth(
                        stickerPack.trayImageFile,
                        bitmap.getWidth(),
                        TRAY_IMAGE_DIMENSION_MIN,
                        TRAY_IMAGE_DIMENSION_MAX);
            }
        } catch (IOException exception) {
            throw StickerPackExceptionFactory.cannotOpenTrayImage(
                    stickerPack.trayImageFile,
                    exception);
        }

        final List<Sticker> stickers = stickerPack.getStickers();

        if (stickers.size() < STICKER_SIZE_MIN || stickers.size() > STICKER_SIZE_MAX) {
            throw StickerPackExceptionFactory.invalidStickerCount(
                    stickers.size(),
                    stickerPack.identifier);
        }
    }

    private static void checkStringValidity(@NonNull String string) {
        String pattern = "[\\w-.,'\\s]+"; // [a-zA-Z0-9_-.' ]
        if (!string.matches(pattern)) {
            throw StickerPackExceptionFactory.invalidStickerPackString(string);
        }

        if (string.contains("..")) {
            throw StickerPackExceptionFactory.stickerPackStringContainsDotDot(string);
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
