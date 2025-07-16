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

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import br.arch.sticker.BuildConfig;

public class WhatsappWhitelistValidator {
    public static final String CONSUMER_WHATSAPP_PACKAGE_NAME = "com.whatsapp";
    public static final String SMB_WHATSAPP_PACKAGE_NAME = "com.whatsapp.w4b";
    private static final String AUTHORITY_QUERY_PARAM = "authority";
    private static final String IDENTIFIER_QUERY_PARAM = "identifier";
    private static final String STICKER_APP_AUTHORITY = BuildConfig.CONTENT_PROVIDER_AUTHORITY;
    private static final String CONTENT_PROVIDER = ".provider.sticker_whitelist_check";
    private static final String QUERY_PATH = "is_whitelisted";
    private static final String QUERY_RESULT_COLUMN_NAME = "result";

    private final Context context;

    public WhatsappWhitelistValidator(Context context) {
        this.context = context;
    }

    public boolean isWhitelisted(@NonNull String identifier) {
        try {
            if (isWhatsAppConsumerAppInstalled(context.getPackageManager()) &&
                    isWhatsAppSmbAppInstalled(context.getPackageManager())) {
                return false;
            }

            boolean consumerResult = isStickerPackWhitelistedInWhatsAppConsumer(identifier);
            boolean smbResult = isStickerPackWhitelistedInWhatsAppSmb(identifier);

            return consumerResult && smbResult;
        } catch (Exception exception) {
            return false;
        }
    }

    private boolean isWhitelistedFromProvider(@NonNull String identifier, String whatsappPackageName) {
        final PackageManager packageManager = context.getPackageManager();

        if (isPackageInstalled(whatsappPackageName, packageManager)) {
            final String whatsappProviderAuthority = whatsappPackageName + CONTENT_PROVIDER;

            final ProviderInfo providerInfo = packageManager.resolveContentProvider(
                    whatsappProviderAuthority, PackageManager.GET_META_DATA);
            // provider is not there. The WhatsApp app may be an old version.
            if (providerInfo == null) {
                return false;
            }

            final Uri queryUri = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
                    .authority(whatsappProviderAuthority).appendPath(QUERY_PATH)
                    .appendQueryParameter(AUTHORITY_QUERY_PARAM, STICKER_APP_AUTHORITY)
                    .appendQueryParameter(IDENTIFIER_QUERY_PARAM, identifier).build();

            try (final Cursor cursor = context.getContentResolver()
                    .query(queryUri, null, null, null, null)) {

                if (cursor != null && cursor.moveToFirst()) {
                    final int whiteListResult = cursor.getInt(
                            cursor.getColumnIndexOrThrow(QUERY_RESULT_COLUMN_NAME));
                    return whiteListResult == 1;
                }
            }
        } else {
            //if app is not installed, then don't need to take into its whitelist info into account.
            return true;
        }

        return false;
    }

    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            final ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName,
                    0
            );

            //noinspection SimplifiableIfStatement
            return applicationInfo.enabled;
        } catch (PackageManager.NameNotFoundException exception) {
            return false;
        }
    }

    public boolean isWhatsAppConsumerAppInstalled(PackageManager packageManager) {
        return !WhatsappWhitelistValidator.isPackageInstalled(CONSUMER_WHATSAPP_PACKAGE_NAME,
                packageManager
        );
    }

    public boolean isWhatsAppSmbAppInstalled(PackageManager packageManager) {
        return !WhatsappWhitelistValidator.isPackageInstalled(SMB_WHATSAPP_PACKAGE_NAME,
                packageManager
        );
    }

    public boolean isStickerPackWhitelistedInWhatsAppConsumer(@NonNull String identifier) {
        return isWhitelistedFromProvider(identifier, CONSUMER_WHATSAPP_PACKAGE_NAME);
    }

    public boolean isStickerPackWhitelistedInWhatsAppSmb(@NonNull String identifier) {
        return isWhitelistedFromProvider(identifier, SMB_WHATSAPP_PACKAGE_NAME);
    }
}
