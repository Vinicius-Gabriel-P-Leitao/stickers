/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.feature.stickerpack.creation.activity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import com.vinicius.sticker.R;
import com.vinicius.sticker.view.core.usecase.activity.StickerPackCreationBaseActivity;
import com.vinicius.sticker.view.core.usecase.definition.MimeTypesSupported;
import com.vinicius.sticker.view.feature.stickerpack.creation.viewmodel.GalleryMediaPickerViewModel;

public class StickerPackCreationActivity extends StickerPackCreationBaseActivity {
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
            GalleryMediaPickerViewModel.launchOwnGallery(this, MimeTypesSupported.IMAGE.getMimeTypes(), namePack);
            return;
        }

        if (selectedFormat != null && selectedFormat.equals(ANIMATED_STICKER)) {
            GalleryMediaPickerViewModel.launchOwnGallery(this, MimeTypesSupported.ANIMATED.getMimeTypes(), namePack);
            return;
        }

        Toast.makeText(this, "Erro ao abrir galeria!", Toast.LENGTH_SHORT).show();
    }
}
