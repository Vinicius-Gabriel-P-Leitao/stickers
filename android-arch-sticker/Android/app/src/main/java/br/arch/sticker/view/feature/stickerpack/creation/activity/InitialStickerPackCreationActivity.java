/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.stickerpack.creation.activity;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import br.arch.sticker.R;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.view.core.usecase.activity.StickerPackCreationBaseActivity;
import br.arch.sticker.view.core.usecase.component.FormatStickerPopupWindow;
import br.arch.sticker.view.core.usecase.definition.MimeTypesSupported;

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
            getSupportActionBar().setTitle(R.string.title_activity_first_pack_creator);
        }

        stickerPackCreationViewModel.getStickerPackPreview()
                .observe(this, this::setStateSupportActionBar);

        ImageButton buttonSelectMedia = findViewById(R.id.button_select_media);
        buttonSelectMedia.setOnClickListener(view -> {
            ObjectAnimator rotation = ObjectAnimator.ofFloat(buttonSelectMedia, "rotation", 0f,
                    360f
            );
            rotation.setDuration(500);
            rotation.start();

            if (getIntent().getBooleanExtra(DATABASE_EMPTY, false)) {
                FormatStickerPopupWindow.popUpButtonChooserStickerModel(
                        this, buttonSelectMedia,
                        new FormatStickerPopupWindow.OnOptionClickListener() {
                            @Override
                            public void onStaticStickerSelected() {
                                setFormat(STATIC_STICKER);
                                stickerPackCreationViewModel.setFragmentVisibility(true);
                                createStickerPackFlow();
                            }

                            @Override
                            public void onAnimatedStickerSelected() {
                                setFormat(ANIMATED_STICKER);
                                stickerPackCreationViewModel.setFragmentVisibility(true);
                                createStickerPackFlow();
                            }
                        }
                );
            }
        });
    }

    @Override
    public void openGallery(String namePack) {
        stickerPackCreationViewModel.setNameStickerPack(namePack);

        if (selectedFormat != null && selectedFormat.equals(STATIC_STICKER)) {
            StickerPackCreationBaseActivity.launchOwnGallery(this);
            stickerPackCreationViewModel.setIsAnimatedPack(false);
            stickerPackCreationViewModel.setMimeTypesSupported(MimeTypesSupported.IMAGE);

            return;
        }

        if (selectedFormat != null && selectedFormat.equals(ANIMATED_STICKER)) {
            StickerPackCreationBaseActivity.launchOwnGallery(this);
            stickerPackCreationViewModel.setIsAnimatedPack(true);
            stickerPackCreationViewModel.setMimeTypesSupported(MimeTypesSupported.ANIMATED);

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
