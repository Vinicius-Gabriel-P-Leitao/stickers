/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.validation;

import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.CHAR_IDENTIFIER_COUNT_MAX;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.CHAR_NAME_COUNT_MAX;
import static br.arch.sticker.domain.data.database.StickerDatabaseHelper.CHAR_PUBLISHER_COUNT_MAX;
import static br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Patterns;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import br.arch.sticker.R;
import br.arch.sticker.core.error.code.InvalidUrlErrorCode;
import br.arch.sticker.core.error.code.StickerPackErrorCode;
import br.arch.sticker.core.error.throwable.content.InvalidWebsiteUrlException;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerException;
import br.arch.sticker.core.error.throwable.sticker.StickerPackValidatorException;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.service.fetch.FetchStickerAssetService;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class StickerPackValidator {
    private final static String TAG_LOG = StickerPackValidator.class.getSimpleName();
    public static final int STICKER_SIZE_MIN = 3;
    public static final int STICKER_SIZE_MAX = 30;
    public static final int TRAY_IMAGE_FILE_SIZE_MAX_KB = 50;
    public static final int TRAY_IMAGE_DIMENSION_MIN = 24;
    public static final int TRAY_IMAGE_DIMENSION_MAX = 512;
    public static final String PLAY_STORE_DOMAIN = "play.google.com";
    public static final String APPLE_STORE_DOMAIN = "itunes.apple.com";

    private final FetchStickerAssetService fetchStickerAssetService;
    private final ApplicationTranslate applicationTranslate;

    public StickerPackValidator(Context paramContext) {
        Context context = paramContext.getApplicationContext();
        this.fetchStickerAssetService = new FetchStickerAssetService(context);
        this.applicationTranslate = new ApplicationTranslate(context.getResources());
    }

    public void verifyStickerPackValidity(@NonNull StickerPack stickerPack)
            throws IllegalStateException {
        if (TextUtils.isEmpty(stickerPack.identifier)) {

            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_identifier_empty)
                            .log(TAG_LOG, Level.ERROR).get(),
                    StickerPackErrorCode.INVALID_IDENTIFIER
            );
        }

        if (stickerPack.identifier.length() > CHAR_IDENTIFIER_COUNT_MAX) {
            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_identifier_exceeds_max_chars,
                            CHAR_IDENTIFIER_COUNT_MAX
                    ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_IDENTIFIER
            );
        }

        checkStringValidity(stickerPack.identifier);

        if (TextUtils.isEmpty(stickerPack.publisher)) {
            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_publisher_empty,
                            stickerPack.identifier
                    ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_PUBLISHER
            );
        }

        if (stickerPack.publisher.length() > CHAR_PUBLISHER_COUNT_MAX) {
            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_publisher_exceeds_max_chars,
                            CHAR_PUBLISHER_COUNT_MAX, stickerPack.identifier
                    ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_PUBLISHER
            );
        }

        if (TextUtils.isEmpty(stickerPack.name)) {
            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_name_empty,
                            stickerPack.identifier
                    ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_STICKERPACK_NAME
            );
        }

        if (stickerPack.name.length() > CHAR_NAME_COUNT_MAX) {
            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_name_exceeds_max_chars,
                            CHAR_NAME_COUNT_MAX, stickerPack.identifier
                    ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_PUBLISHER
            );
        }

        if (TextUtils.isEmpty(stickerPack.trayImageFile)) {

            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_thumbnail_empty,
                            stickerPack.identifier
                    ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_THUMBNAIL
            );
        }

        if (!TextUtils.isEmpty(stickerPack.androidPlayStoreLink) &&
                !isValidWebsiteUrl(stickerPack.androidPlayStoreLink)) {
            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_android_play_store_invalid_url,
                            stickerPack.androidPlayStoreLink
                    ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_ANDROID_URL_SITE
            );
        }

        if (!TextUtils.isEmpty(stickerPack.androidPlayStoreLink) &&
                !isURLInCorrectDomain(stickerPack.androidPlayStoreLink, PLAY_STORE_DOMAIN)) {

            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_android_play_store_wrong_domain,
                            PLAY_STORE_DOMAIN
                    ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_ANDROID_URL_SITE
            );
        }

        if (!TextUtils.isEmpty(stickerPack.iosAppStoreLink) &&
                !isValidWebsiteUrl(stickerPack.iosAppStoreLink)) {
            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_ios_app_store_invalid_url,
                            stickerPack.iosAppStoreLink
                    ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_IOS_URL_SITE
            );
        }

        if (!TextUtils.isEmpty(stickerPack.iosAppStoreLink) &&
                !isURLInCorrectDomain(stickerPack.iosAppStoreLink, APPLE_STORE_DOMAIN)) {
            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_ios_app_store_wrong_domain,
                            APPLE_STORE_DOMAIN
                    ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_IOS_URL_SITE
            );
        }

        if (!TextUtils.isEmpty(stickerPack.licenseAgreementWebsite) &&
                !isValidWebsiteUrl(stickerPack.licenseAgreementWebsite)) {
            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_license_agreement_invalid_url,
                            stickerPack.licenseAgreementWebsite
                    ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_WEBSITE
            );
        }

        if (!TextUtils.isEmpty(stickerPack.privacyPolicyWebsite) &&
                !isValidWebsiteUrl(stickerPack.privacyPolicyWebsite)) {
            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_privacy_policy_invalid_url,
                            stickerPack.publisherWebsite
                    ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_WEBSITE
            );
        }

        if (!TextUtils.isEmpty(stickerPack.publisherWebsite) &&
                !isValidWebsiteUrl(stickerPack.publisherWebsite)) {
            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_publisher_website_invalid_url,
                            stickerPack.publisherWebsite
                    ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_WEBSITE
            );
        }

        if (!TextUtils.isEmpty(stickerPack.publisherEmail) &&
                !Patterns.EMAIL_ADDRESS.matcher(stickerPack.publisherEmail).matches()) {
            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_publisher_email_invalid,
                            stickerPack.publisherEmail
                    ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_EMAIL
            );
        }

        try {
            final byte[] stickerAssetBytes = fetchStickerAssetService.fetchStickerAsset(
                    stickerPack.identifier, stickerPack.trayImageFile);

            if (stickerAssetBytes.length > TRAY_IMAGE_FILE_SIZE_MAX_KB * 1024) {
                throw new StickerPackValidatorException(
                        applicationTranslate.translate(R.string.throw_thumbnail_size_exceeds_max,
                                TRAY_IMAGE_FILE_SIZE_MAX_KB, stickerPack.trayImageFile
                        ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_THUMBNAIL
                );
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(stickerAssetBytes, 0,
                    stickerAssetBytes.length
            );
            if (bitmap.getHeight() > TRAY_IMAGE_DIMENSION_MAX ||
                    bitmap.getHeight() < TRAY_IMAGE_DIMENSION_MIN) {
                throw new StickerPackValidatorException(
                        applicationTranslate.translate(R.string.throw_thumbnail_height_invalid,
                                TRAY_IMAGE_DIMENSION_MIN, TRAY_IMAGE_DIMENSION_MAX,
                                bitmap.getHeight(), stickerPack.trayImageFile
                        ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_THUMBNAIL
                );
            }

            if (bitmap.getWidth() > TRAY_IMAGE_DIMENSION_MAX ||
                    bitmap.getWidth() < TRAY_IMAGE_DIMENSION_MIN) {
                throw new StickerPackValidatorException(
                        applicationTranslate.translate(R.string.throw_thumbnail_width_invalid,
                                TRAY_IMAGE_DIMENSION_MIN, TRAY_IMAGE_DIMENSION_MAX,
                                bitmap.getWidth(), stickerPack.trayImageFile
                        ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_THUMBNAIL
                );
            }
        } catch (FetchStickerException exception) {
            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_thumbnail_cannot_open,
                            stickerPack.trayImageFile
                    ).log(TAG_LOG, Level.ERROR).get(), exception,
                    StickerPackErrorCode.INVALID_THUMBNAIL
            );
        }

        final List<Sticker> stickers = stickerPack.getStickers();

        if (stickers.size() < STICKER_SIZE_MIN || stickers.size() > STICKER_SIZE_MAX) {
            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_sticker_count_invalid,
                            stickers.size(), stickerPack.identifier
                    ).log(TAG_LOG, Level.ERROR).get(), StickerPackErrorCode.INVALID_STICKERPACK_SIZE
            );
        }
    }

    private void checkStringValidity(@NonNull String string) {
        String pattern = "[\\w-.,'\\s]+"; // [a-zA-Z0-9_-.' ]
        if (!string.matches(pattern)) {
            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_invalid_characters, string)
                            .log(TAG_LOG, Level.ERROR).get(),
                    StickerPackErrorCode.INVALID_STICKERPACK_NAME
            );
        }

        if (string.contains("..")) {
            throw new StickerPackValidatorException(
                    applicationTranslate.translate(R.string.throw_contains_double_dot, string)
                            .log(TAG_LOG, Level.WARN).get(),
                    StickerPackErrorCode.INVALID_STICKERPACK_NAME
            );
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isValidWebsiteUrl(String websiteUrl) throws InvalidWebsiteUrlException {
        try {
            new URL(websiteUrl);
        } catch (MalformedURLException exception) {
            throw new InvalidWebsiteUrlException(
                    applicationTranslate.translate(R.string.throw_url_malformed, websiteUrl)
                            .log(TAG_LOG, Level.ERROR, exception).get(), exception,
                    InvalidUrlErrorCode.INVALID_URL, websiteUrl
            );
        }

        return URLUtil.isHttpUrl(websiteUrl) || URLUtil.isHttpsUrl(websiteUrl);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isURLInCorrectDomain(String urlString, @NonNull String domain)
            throws InvalidWebsiteUrlException {
        try {
            URL url = new URL(urlString);

            if (domain.equals(url.getHost())) {
                return true;
            }

        } catch (MalformedURLException exception) {
            throw new InvalidWebsiteUrlException(
                    applicationTranslate.translate(R.string.throw_url_malformed, urlString)
                            .log(TAG_LOG, Level.ERROR, exception).get(), exception,
                    InvalidUrlErrorCode.INVALID_URL, urlString
            );
        }

        return false;
    }
}