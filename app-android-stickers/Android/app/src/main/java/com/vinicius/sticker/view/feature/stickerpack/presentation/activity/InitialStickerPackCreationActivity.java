/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.feature.stickerpack.presentation.activity;

import static com.vinicius.sticker.view.feature.media.viewholder.GalleryMediaPickerViewHolder.launchOwnGallery;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import com.vinicius.sticker.R;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.view.core.component.FormatStickerPopupWindow;
import com.vinicius.sticker.view.feature.stickerpack.usecase.MimeTypesSupported;
import com.vinicius.sticker.view.feature.stickerpack.usecase.StickerPackCreationFlow;

public class InitialStickerPackCreationActivity extends StickerPackCreationFlow {
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

        viewModel.getStickerPackToPreview().observe(this, this::setStateSupportActionBar);

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
                                viewModel.openFragmentState();
                                createStickerPackFlow();
                            }

                            @Override
                            public void onAnimatedStickerSelected() {
                                setFormat(ANIMATED_STICKER);
                                viewModel.openFragmentState();
                                createStickerPackFlow();
                            }
                        });
            }
        });
    }

    @Override
    public void openGallery(String namePack) {
        if (selectedFormat != null && selectedFormat.equals(STATIC_STICKER)) {
            launchOwnGallery(this, MimeTypesSupported.IMAGE.getMimeTypes(), namePack);
            return;
        }

        if (selectedFormat != null && selectedFormat.equals(ANIMATED_STICKER)) {
            launchOwnGallery(this, MimeTypesSupported.ANIMATED.getMimeTypes(), namePack);
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
