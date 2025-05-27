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
import static com.vinicius.sticker.view.feature.permission.usecase.DefinePermissionsToRequest.getPermissionsToRequest;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.vinicius.sticker.R;
import com.vinicius.sticker.view.core.base.BaseActivity;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.view.feature.media.launcher.GalleryMediaPickerLauncher;
import com.vinicius.sticker.view.feature.permission.fragment.PermissionRequestBottomSheetDialogFragment;
import com.vinicius.sticker.view.feature.stickerpack.adapter.StickerPreviewAdapter;
import com.vinicius.sticker.view.feature.stickerpack.presentation.fragment.PackMetadataBottomSheetDialogFragment;

import java.util.Arrays;

public class StickerPackCreatorActivity extends BaseActivity {
    public static final String EXTRA_STICKER_FORMAT = "sticker_format";
    public static final String STATIC_STICKER = "animated";
    public static final String ANIMATED_STICKER = "static";

    private StickerPreviewAdapter stickerPreviewAdapter;
    private GridLayoutManager layoutManager;
    private RecyclerView recyclerView;
    private String namePack;
    private int numColumns;
    private View divider;

    private void saveNamePack(String namePack) {
        this.namePack = namePack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_sticker_pack);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.title_activity_sticker_packs_creator);
        }

        GalleryMediaPickerLauncher viewModel = new ViewModelProvider(this).get(GalleryMediaPickerLauncher.class);
        viewModel.getStickerPackToPreview().observe(this, this::setupStickerPackView);

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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("namePack", namePack);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri selectedUri = data.getData();
            Log.d("MediaPicker", "Selected URI: " + selectedUri);
        }
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
                    Toast.makeText(StickerPackCreatorActivity.this, "Galeria não foi liberada.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(StickerPackCreatorActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        packMetadataBottomSheetDialogFragment.show(getSupportFragmentManager(), "PackMetadataBottomSheetDialogFragment");
    }

    private void openGallery(String namePack) {
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

    private final ViewTreeObserver.OnGlobalLayoutListener pageLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            setNumColumns(recyclerView.getWidth() /
                          recyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size));
        }
    };

    private void setupStickerPackView(StickerPack stickerPack) {
        Log.d("StickerPack", "StickerPack recebido: " + stickerPack.toString());

        SimpleDraweeView expandedStickerView = findViewById(R.id.sticker_details_expanded_sticker);
        layoutManager = new GridLayoutManager(this, 1);

        recyclerView = findViewById(R.id.sticker_list_to_package);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(pageLayoutListener);
        recyclerView.addOnScrollListener(dividerScrollListener);

        divider = findViewById(R.id.divider);

        if (stickerPreviewAdapter == null) {
            stickerPreviewAdapter =
                    new StickerPreviewAdapter(getLayoutInflater(), R.drawable.sticker_error, getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size), getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_padding), stickerPack, expandedStickerView);
            recyclerView.setAdapter(stickerPreviewAdapter);
        }
    }

    private final RecyclerView.OnScrollListener dividerScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            updateDivider(recyclerView);
        }

        @Override
        public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
            super.onScrolled(recyclerView, dx, dy);
            updateDivider(recyclerView);
        }

        private void updateDivider(RecyclerView recyclerView) {
            boolean showDivider = recyclerView.computeVerticalScrollOffset() > 0;
            if (divider != null) {
                divider.setVisibility(showDivider ?
                                      View.VISIBLE :
                                      View.INVISIBLE);
            }
        }
    };

    private void setNumColumns(int numColumns) {
        if (this.numColumns != numColumns) {
            layoutManager.setSpanCount(numColumns);
            this.numColumns = numColumns;
            if (stickerPreviewAdapter != null) {
                stickerPreviewAdapter.notifyDataSetChanged();
            }
        }
    }
}
