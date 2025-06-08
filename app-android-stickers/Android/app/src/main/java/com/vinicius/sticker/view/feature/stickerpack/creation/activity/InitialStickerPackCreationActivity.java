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
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.view.core.usecase.activity.StickerPackCreationBaseActivity;
import com.vinicius.sticker.view.core.usecase.component.FormatStickerPopupWindow;
import com.vinicius.sticker.view.core.usecase.definition.MimeTypesSupported;

public class InitialStickerPackCreationActivity extends StickerPackCreationBaseActivity {
    public static final String EXTRA_SHOW_UP_BUTTON = "show_up_button";
    public static final String DATABASE_EMPTY = "database_empty";

    public String selectedFormat = null;

    public void setFormat(String format) {
        this.selectedFormat = format;
    }

    @Override
    public void setupUI(Bundle savedInstanceState) {
        boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, false);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(showUpButton);
            getSupportActionBar().setTitle(R.string.title_activity_sticker_packs_creator_first);
        }

        galleryMediaPickerViewModel.getStickerPackPreview().observe(this, this::setStateSupportActionBar);

        ImageButton buttonSelectMedia = findViewById(R.id.button_select_media);
        buttonSelectMedia.setOnClickListener(view -> {
            ObjectAnimator rotation = ObjectAnimator.ofFloat(buttonSelectMedia, "rotation", 0f, 360f);
            rotation.setDuration(500);
            rotation.start();

            if (getIntent().getBooleanExtra(DATABASE_EMPTY, false)) {
                FormatStickerPopupWindow.popUpButtonChooserStickerModel(
                        this, buttonSelectMedia, new FormatStickerPopupWindow.OnOptionClickListener() {
                            @Override
                            public void onStaticStickerSelected() {
                                setFormat(STATIC_STICKER);
                                galleryMediaPickerViewModel.setFragmentVisibility(true);
                                createStickerPackFlow();
                            }

                            @Override
                            public void onAnimatedStickerSelected() {
                                setFormat(ANIMATED_STICKER);
                                galleryMediaPickerViewModel.setFragmentVisibility(true);
                                createStickerPackFlow();
                            }
                        });
            }
        });
    }

    @Override
    public void openGallery(String namePack) {
        galleryMediaPickerViewModel.setNameStickerPack(namePack);

        if (selectedFormat != null && selectedFormat.equals(STATIC_STICKER)) {
            StickerPackCreationBaseActivity.launchOwnGallery(this);
            galleryMediaPickerViewModel.setIsAnimatedPack(false);
            galleryMediaPickerViewModel.setMimeTypesSupported(MimeTypesSupported.IMAGE);

            return;
        }

        if (selectedFormat != null && selectedFormat.equals(ANIMATED_STICKER)) {
            StickerPackCreationBaseActivity.launchOwnGallery(this);
            galleryMediaPickerViewModel.setIsAnimatedPack(true);
            galleryMediaPickerViewModel.setMimeTypesSupported(MimeTypesSupported.ANIMATED);

            return;
        }

        Toast.makeText(this, "Erro ao abrir galeria!", Toast.LENGTH_SHORT).show();
    }

    private void setStateSupportActionBar(StickerPack stickerPack) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
