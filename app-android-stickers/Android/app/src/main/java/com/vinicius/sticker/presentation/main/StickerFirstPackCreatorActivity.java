/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */

package com.vinicius.sticker.presentation.main;

import static com.vinicius.sticker.presentation.feature.permission.util.DefinePermissionsToRequest.getPermissionsToRequest;
import static com.vinicius.sticker.presentation.feature.stickerpack.util.LaunchOwenGallery.ANIMATED_MIME_TYPES;
import static com.vinicius.sticker.presentation.feature.stickerpack.util.LaunchOwenGallery.IMAGE_MIME_TYPES;
import static com.vinicius.sticker.presentation.feature.stickerpack.util.LaunchOwenGallery.launchOwnGallery;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.vinicius.sticker.R;
import com.vinicius.sticker.core.BaseActivity;
import com.vinicius.sticker.presentation.component.FormatStickerPopupWindow;
import com.vinicius.sticker.presentation.feature.permission.fragment.PermissionRequestBottomSheetDialogFragment;
import com.vinicius.sticker.presentation.feature.stickerpack.presentation.fragment.PackMetadataBottomSheetDialogFragment;

import java.util.Arrays;

public class StickerFirstPackCreatorActivity extends BaseActivity {
    public static final String DATABASE_EMPTY = "database_empty";
    public static final String STATIC_STICKER = "animated";
    public static final String ANIMATED_STICKER = "static";
    private String namePack;
    private String selectedFormat = null;

    private void setFormat(String format) {
        this.selectedFormat = format;
    }

    private void saveNamePack(String namePack) {
        this.namePack = namePack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_first_sticker_pack);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.title_activity_sticker_packs_creator);
        }

        ImageButton buttonSelectMedia = findViewById(R.id.button_select_media);
        buttonSelectMedia.setOnClickListener(view -> {
            ObjectAnimator rotation = ObjectAnimator.ofFloat(buttonSelectMedia, "rotation", 0f, 360f);
            rotation.setDuration(500);
            rotation.start();

            if (getIntent().getBooleanExtra(DATABASE_EMPTY, false)) {
                FormatStickerPopupWindow.popUpButtonChooserStickerModel(this, buttonSelectMedia, new FormatStickerPopupWindow.OnOptionClickListener() {
                    @Override
                    public void onStaticStickerSelected() {
                        setFormat(STATIC_STICKER);
                        createStickerPackFlow();
                    }

                    @Override
                    public void onAnimatedStickerSelected() {
                        setFormat(ANIMATED_STICKER);
                        createStickerPackFlow();
                    }
                });
            }
        });
    }

    private void createStickerPackFlow() {
        PermissionRequestBottomSheetDialogFragment permissionRequestBottomSheetDialogFragment = new PermissionRequestBottomSheetDialogFragment();

        String[] permissions = getPermissionsToRequest(this);
        Log.i("Permissions Media", Arrays.toString(permissions));
        if (permissions.length > 0) {
            permissionRequestBottomSheetDialogFragment.setPermissions(permissions);
            permissionRequestBottomSheetDialogFragment.setCallback(new PermissionRequestBottomSheetDialogFragment.PermissionCallback() {
                @Override
                public void onPermissionsGranted() {
                    if (namePack == null || namePack.isEmpty()) {
                        openMetadataGetter();
                    } else {
                        openGallery(namePack);
                    }
                }

                @Override
                public void onPermissionsDenied() {
                    Toast.makeText(StickerFirstPackCreatorActivity.this, "Galeria não foi liberada.", Toast.LENGTH_SHORT).show();
                }
            });

            permissionRequestBottomSheetDialogFragment.show(getSupportFragmentManager(), "permissionRequestBottomSheetDialogFragment");
        } else {
            if (namePack == null || namePack.isEmpty()) {
                openMetadataGetter();
            } else {
                openGallery(namePack);
            }
        }
    }

    private void openMetadataGetter() {
        PackMetadataBottomSheetDialogFragment packMetadataBottomSheetDialogFragment = new PackMetadataBottomSheetDialogFragment();
        packMetadataBottomSheetDialogFragment.setCallback(new PackMetadataBottomSheetDialogFragment.MetadataCallback() {
            @Override
            public void onGetMetadata(String namePack) {
                saveNamePack(namePack);
                openGallery(namePack);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(StickerFirstPackCreatorActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        packMetadataBottomSheetDialogFragment.show(getSupportFragmentManager(), "PackMetadataBottomSheetDialogFragment");
    }

    private void openGallery(String namePack) {
        if (selectedFormat != null && selectedFormat.equals(STATIC_STICKER)) {
            launchOwnGallery(this, IMAGE_MIME_TYPES, namePack);
            return;
        }

        if (selectedFormat != null && selectedFormat.equals(ANIMATED_STICKER)) {
            launchOwnGallery(this, ANIMATED_MIME_TYPES, namePack);
            return;
        }

        Toast.makeText(this, "Erro ao abrir galeria!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("namePack", namePack);
        outState.putString("selectedFormat", selectedFormat);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri selectedUri = data.getData();
            Log.d("MediaPicker", "Selected URI: " + selectedUri);
        }
    }
}
