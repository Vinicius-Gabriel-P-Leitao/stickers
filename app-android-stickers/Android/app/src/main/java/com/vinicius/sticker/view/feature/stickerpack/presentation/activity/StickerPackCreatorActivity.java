/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.feature.stickerpack.presentation.activity;

import static com.vinicius.sticker.view.feature.media.launcher.GalleryMediaPickerLauncher.ANIMATED_MIME_TYPES;
import static com.vinicius.sticker.view.feature.media.launcher.GalleryMediaPickerLauncher.IMAGE_MIME_TYPES;
import static com.vinicius.sticker.view.feature.media.launcher.GalleryMediaPickerLauncher.launchOwnGallery;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.vinicius.sticker.R;
import com.vinicius.sticker.view.feature.media.launcher.GalleryMediaPickerLauncher;
import com.vinicius.sticker.view.feature.stickerpack.usecase.CreateStickerPackActivity;

public class StickerPackCreatorActivity extends CreateStickerPackActivity {
    public static final String EXTRA_STICKER_FORMAT = "sticker_format";

    @Override
    public void setupUI(Bundle savedInstanceState) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_sticker_packs_creator);
        }

        ImageButton buttonSelectMedia = findViewById(R.id.button_select_media);
        buttonSelectMedia.setOnClickListener(view -> {
            ObjectAnimator rotation = ObjectAnimator.ofFloat(buttonSelectMedia, "rotation", 0f, 360f);
            rotation.setDuration(500);
            rotation.start();

            viewModel.openFragmentState();
            createStickerPackFlow();
        });
    }

    @Override
    public void openGallery(String namePack) {
        String selectedFormat = getIntent().getStringExtra(EXTRA_STICKER_FORMAT);

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
}
