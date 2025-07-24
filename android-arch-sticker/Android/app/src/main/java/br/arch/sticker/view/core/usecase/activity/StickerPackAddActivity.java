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

package br.arch.sticker.view.core.usecase.activity;

import static br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.*;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import br.arch.sticker.BuildConfig;
import br.arch.sticker.R;
import br.arch.sticker.core.validation.WhatsappWhitelistValidator;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.view.core.base.BaseActivity;
import br.arch.sticker.view.core.usecase.component.AlertStickerDialog;
import br.arch.sticker.view.core.usecase.definition.StickerPackHandler;
import br.arch.sticker.view.feature.stickerpack.details.activity.StickerPackDetailsActivity;

public abstract class StickerPackAddActivity extends BaseActivity implements StickerPackHandler {
    private static final int ADD_PACK = 200;
    private static final String TAG_LOG = StickerPackAddActivity.class.getSimpleName();

    private ApplicationTranslate applicationTranslate;
    private WhatsappWhitelistValidator whatsappWhitelistValidator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applicationTranslate = new ApplicationTranslate(getResources());
        whatsappWhitelistValidator = new WhatsappWhitelistValidator(this);
    }

    @Override
    public void addStickerPackToWhatsApp(String stickerPackIdentifier, String stickerPackName) {
        try {
            //if neither WhatsApp Consumer or WhatsApp Business is installed, then tell user to install the apps.
            if (whatsappWhitelistValidator.isWhatsAppConsumerAppInstalled(getPackageManager()) &&
                    whatsappWhitelistValidator.isWhatsAppSmbAppInstalled(getPackageManager())) {
                Toast.makeText(this, R.string.dialog_add_stickerpack_fail_prompt, Toast.LENGTH_LONG).show();
                return;
            }

            final boolean stickerPackWhitelistedInWhatsAppConsumer = whatsappWhitelistValidator.isStickerPackWhitelistedInWhatsAppConsumer(
                    stickerPackIdentifier);
            final boolean stickerPackWhitelistedInWhatsAppSmb = whatsappWhitelistValidator.isStickerPackWhitelistedInWhatsAppSmb(
                    stickerPackIdentifier);

            if (!stickerPackWhitelistedInWhatsAppConsumer && !stickerPackWhitelistedInWhatsAppSmb) {
                //ask users which app to add the stickerPack to.
                launchIntentToAddPackToChooser(stickerPackIdentifier, stickerPackName);
            } else if (!stickerPackWhitelistedInWhatsAppConsumer) {
                launchIntentToAddPackToSpecificPackage(stickerPackIdentifier, stickerPackName,
                        WhatsappWhitelistValidator.CONSUMER_WHATSAPP_PACKAGE_NAME
                );
            } else if (!stickerPackWhitelistedInWhatsAppSmb) {
                launchIntentToAddPackToSpecificPackage(stickerPackIdentifier, stickerPackName, WhatsappWhitelistValidator.SMB_WHATSAPP_PACKAGE_NAME);
            } else {
                Toast.makeText(this, R.string.dialog_add_stickerpack_fail_prompt, Toast.LENGTH_LONG).show();
            }
        } catch (Exception exception) {
            Toast.makeText(this, applicationTranslate.translate(R.string.dialog_add_stickerpack_fail_prompt).log(TAG_LOG, Level.ERROR).get(),
                    Toast.LENGTH_LONG
            ).show();
        }

    }

    private void launchIntentToAddPackToSpecificPackage(String identifier, String stickerPackName, String whatsappPackageName) {
        Intent intent = createIntentToAddStickerPack(identifier, stickerPackName);
        intent.setPackage(whatsappPackageName);

        try {
            startActivityForResult(intent, ADD_PACK);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.dialog_add_stickerpack_fail_prompt, Toast.LENGTH_LONG).show();
        }
    }

    //Handle cases either of WhatsApp are set as default app to handle this intent. We still want users to see both options.
    private void launchIntentToAddPackToChooser(String identifier, String stickerPackName) {
        Intent intent = createIntentToAddStickerPack(identifier, stickerPackName);

        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.add_to_whatsapp)), ADD_PACK);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.dialog_add_stickerpack_fail_prompt, Toast.LENGTH_LONG).show();
        }
    }

    @NonNull
    private Intent createIntentToAddStickerPack(String identifier, String stickerPackName) {
        Intent intent = new Intent();
        intent.setAction("com.whatsapp.intent.action.ENABLE_STICKER_PACK");
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_AUTHORITY, BuildConfig.CONTENT_PROVIDER_AUTHORITY);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_ID, identifier);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_NAME, stickerPackName);
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_PACK) {
            if (resultCode == Activity.RESULT_CANCELED) {
                if (data != null) {
                    final String validationError = data.getStringExtra("validation_error");
                    if (validationError != null) {
                        if (BuildConfig.DEBUG) {
                            //validation error should be shown to developer only, not users.
                            MessageDialogFragment.newInstance(R.string.title_validation_error, validationError)
                                    .show(getSupportFragmentManager(), "validation error");
                        }
                        Log.e(TAG_LOG, "Validation failed:" + validationError);
                    }
                } else {
                    AlertStickerDialog dialog = new AlertStickerDialog(this);
                    dialog.setMessageText(getString(R.string.dialog_add_stickerpack_fail_prompt));

                    dialog.setTextFixButton(getString(R.string.dialog_add_stickerpack_update_link));
                    dialog.setOnFixClick(view -> launchWhatsAppPlayStorePage());

                    dialog.setTextIgnoreButton(getString(android.R.string.ok));
                    dialog.setOnIgnoreClick(view -> dialog.dismiss());

                    dialog.show();
                }
            }
        }
    }

    private void launchWhatsAppPlayStorePage() {
        final PackageManager packageManager = getPackageManager();

        final boolean whatsAppInstalled = WhatsappWhitelistValidator.isPackageInstalled(WhatsappWhitelistValidator.CONSUMER_WHATSAPP_PACKAGE_NAME,
                packageManager
        );

        final boolean smbAppInstalled = WhatsappWhitelistValidator.isPackageInstalled(WhatsappWhitelistValidator.SMB_WHATSAPP_PACKAGE_NAME,
                packageManager
        );

        final String playPackageLinkPrefix = "http://play.google.com/store/apps/details?id=";

        if (whatsAppInstalled && smbAppInstalled) {
            launchPlayStoreWithUri("https://play.google.com/store/apps/developer?id=WhatsApp+LLC");
        } else if (whatsAppInstalled) {
            launchPlayStoreWithUri(playPackageLinkPrefix + WhatsappWhitelistValidator.CONSUMER_WHATSAPP_PACKAGE_NAME);
        } else if (smbAppInstalled) {
            launchPlayStoreWithUri(playPackageLinkPrefix + WhatsappWhitelistValidator.SMB_WHATSAPP_PACKAGE_NAME);
        }

    }

    private void launchPlayStoreWithUri(String uriString) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(uriString));
        intent.setPackage("com.android.vending");

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.error_play_store_not_found, Toast.LENGTH_LONG).show();
        }
    }
}
