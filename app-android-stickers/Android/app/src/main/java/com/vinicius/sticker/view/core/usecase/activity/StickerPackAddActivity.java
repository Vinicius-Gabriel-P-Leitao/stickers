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

package com.vinicius.sticker.view.core.usecase.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.vinicius.sticker.BuildConfig;
import com.vinicius.sticker.R;
import com.vinicius.sticker.core.validation.WhatsappWhitelistValidator;
import com.vinicius.sticker.view.core.base.BaseActivity;
import com.vinicius.sticker.view.feature.stickerpack.details.activity.StickerPackDetailsActivity;

public abstract class StickerPackAddActivity extends BaseActivity {
    private static final int ADD_PACK = 200;
    private static final String TAG_LOG = StickerPackAddActivity.class.getSimpleName();

    protected void addStickerPackToWhatsApp(String identifier, String stickerPackName) {
        try {
            //if neither WhatsApp Consumer or WhatsApp Business is installed, then tell user to install the apps.
            if (WhatsappWhitelistValidator.isWhatsAppConsumerAppInstalled(getPackageManager()) &&
                    WhatsappWhitelistValidator.isWhatsAppSmbAppInstalled(getPackageManager())) {
                Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show();
                return;
            }

            final boolean stickerPackWhitelistedInWhatsAppConsumer = WhatsappWhitelistValidator.isStickerPackWhitelistedInWhatsAppConsumer(
                    this, identifier);
            final boolean stickerPackWhitelistedInWhatsAppSmb = WhatsappWhitelistValidator.isStickerPackWhitelistedInWhatsAppSmb(this, identifier);

            if (!stickerPackWhitelistedInWhatsAppConsumer && !stickerPackWhitelistedInWhatsAppSmb) {
                //ask users which app to add the pack to.
                launchIntentToAddPackToChooser(identifier, stickerPackName);
            } else if (!stickerPackWhitelistedInWhatsAppConsumer) {
                launchIntentToAddPackToSpecificPackage(identifier, stickerPackName, WhatsappWhitelistValidator.CONSUMER_WHATSAPP_PACKAGE_NAME);
            } else if (!stickerPackWhitelistedInWhatsAppSmb) {
                launchIntentToAddPackToSpecificPackage(identifier, stickerPackName, WhatsappWhitelistValidator.SMB_WHATSAPP_PACKAGE_NAME);
            } else {
                Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show();
            }
        } catch (Exception exception) {
            Log.e(TAG_LOG, "error adding sticker pack to WhatsApp", exception);
            Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show();
        }

    }

    private void launchIntentToAddPackToSpecificPackage(String identifier, String stickerPackName, String whatsappPackageName) {
        Intent intent = createIntentToAddStickerPack(identifier, stickerPackName);
        intent.setPackage(whatsappPackageName);

        try {
            startActivityForResult(intent, ADD_PACK);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show();
        }
    }

    //Handle cases either of WhatsApp are set as default app to handle this intent. We still want users to see both options.
    private void launchIntentToAddPackToChooser(String identifier, String stickerPackName) {
        Intent intent = createIntentToAddStickerPack(identifier, stickerPackName);

        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.add_to_whatsapp)), ADD_PACK);
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, R.string.add_pack_fail_prompt_update_whatsapp, Toast.LENGTH_LONG).show();
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
                    new StickerPackNotAddedMessageFragment().show(getSupportFragmentManager(), "sticker_pack_not_added");
                }
            }
        }
    }

    public static final class StickerPackNotAddedMessageFragment extends DialogFragment {
        @NonNull
        @Override
        public AlertDialog onCreateDialog(Bundle savedInstanceState) {
            FragmentActivity activity = getActivity();
            if (activity == null) {
                Log.w(TAG_LOG, "Ocorreu um erro ao criar o diálogo.");
                return new AlertDialog.Builder(requireContext()).setMessage("Ocorreu um erro ao criar o diálogo.").setPositiveButton("OK", null)
                        .create();
            }

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity).setMessage(R.string.add_pack_fail_prompt_update_whatsapp)
                    .setCancelable(true).setPositiveButton(android.R.string.ok, (dialog, which) -> dismiss())
                    .setNeutralButton(R.string.add_pack_fail_prompt_update_play_link, (dialog, which) -> launchWhatsAppPlayStorePage());

            return dialogBuilder.create();
        }

        private void launchWhatsAppPlayStorePage() {
            if (getActivity() != null) {
                final PackageManager packageManager = getActivity().getPackageManager();

                final boolean whatsAppInstalled = WhatsappWhitelistValidator.isPackageInstalled(
                        WhatsappWhitelistValidator.CONSUMER_WHATSAPP_PACKAGE_NAME, packageManager);

                final boolean smbAppInstalled = WhatsappWhitelistValidator.isPackageInstalled(
                        WhatsappWhitelistValidator.SMB_WHATSAPP_PACKAGE_NAME, packageManager);

                final String playPackageLinkPrefix = "http://play.google.com/store/apps/details?id=";

                if (whatsAppInstalled && smbAppInstalled) {
                    launchPlayStoreWithUri("https://play.google.com/store/apps/developer?id=WhatsApp+LLC");
                } else if (whatsAppInstalled) {
                    launchPlayStoreWithUri(playPackageLinkPrefix + WhatsappWhitelistValidator.CONSUMER_WHATSAPP_PACKAGE_NAME);
                } else if (smbAppInstalled) {
                    launchPlayStoreWithUri(playPackageLinkPrefix + WhatsappWhitelistValidator.SMB_WHATSAPP_PACKAGE_NAME);
                }
            }
        }

        private void launchPlayStoreWithUri(String uriString) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(uriString));
            intent.setPackage("com.android.vending");

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException exception) {
                Toast.makeText(getActivity(), R.string.cannot_find_play_store, Toast.LENGTH_LONG).show();
            }
        }
    }
}
