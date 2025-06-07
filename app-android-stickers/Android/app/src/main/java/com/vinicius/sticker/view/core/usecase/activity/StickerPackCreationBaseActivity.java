/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.core.usecase.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vinicius.sticker.R;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.domain.orchestrator.StickerPackOrchestrator;
import com.vinicius.sticker.view.core.base.BaseActivity;
import com.vinicius.sticker.view.core.usecase.definition.DefinePermissionsToRequest;
import com.vinicius.sticker.view.feature.preview.adapter.StickerPreviewAdapter;
import com.vinicius.sticker.view.feature.stickerpack.creation.viewmodel.GalleryMediaPickerViewModel;
import com.vinicius.sticker.view.feature.stickerpack.creation.viewmodel.NameStickerPackViewModel;
import com.vinicius.sticker.view.feature.stickerpack.creation.viewmodel.PermissionRequestViewModel;
import com.vinicius.sticker.view.main.EntryActivity;

public abstract class StickerPackCreationBaseActivity extends BaseActivity {
    private final static String TAG_LOG = StickerPackCreationBaseActivity.class.getSimpleName();

    public static final String STATIC_STICKER = "animated";
    public static final String ANIMATED_STICKER = "static";

    public GalleryMediaPickerViewModel galleryMediaPickerViewModel;
    public PermissionRequestViewModel permissionRequestViewModel;
    public NameStickerPackViewModel nameStickerPackViewModel;

    public StickerPreviewAdapter stickerPreviewAdapter;

    public GridLayoutManager layoutManager;
    public RecyclerView recyclerView;
    public String namePack;
    public int numColumns;
    public View divider;

    public Context context;

    public void saveNamePack(String namePack) {
        this.namePack = namePack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_sticker_pack);

        getViewModelStore().clear();
        galleryMediaPickerViewModel = new ViewModelProvider(this).get(GalleryMediaPickerViewModel.class);
        permissionRequestViewModel = new ViewModelProvider(this).get(PermissionRequestViewModel.class);
        nameStickerPackViewModel = new ViewModelProvider(this).get(NameStickerPackViewModel.class);

        galleryMediaPickerViewModel.getStickerPackToPreview().observe(this, this::setupStickerPackView);

        StickerPackOrchestrator.resetData();
        setupUI(savedInstanceState);
    }

    public abstract void setupUI(Bundle savedInstanceState);

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
            Log.d(TAG_LOG, "URI selecionada: " + selectedUri);
        }
    }

    public void createStickerPackFlow() {
        String[] permissions = DefinePermissionsToRequest.getPermissionsToRequest(this);

        if (DefinePermissionsToRequest.areAllPermissionsGranted(permissions, this)) {
            if (namePack == null || namePack.isEmpty()) {
                openMetadataGetter();
            } else {
                openGallery(namePack);
            }

            return;
        }

        permissionRequestViewModel.setPermissions(DefinePermissionsToRequest.getPermissionsToRequest(this));
        permissionRequestViewModel.getPermissionGranted().observe(
                this, granted -> {
                    if (granted != null && granted) {
                        if (namePack == null || namePack.isEmpty()) {
                            openMetadataGetter();
                        } else {
                            openGallery(namePack);
                            Log.e(TAG_LOG, namePack);
                        }
                    }
                });

        permissionRequestViewModel.getPermissionDenied().observe(
                this, denied -> {
                    if (denied != null && denied) {
                        Toast.makeText(this, "Galeria não foi liberada.", Toast.LENGTH_SHORT).show();
                    }
                });

        PermissionRequestViewModel.launchPermissionRequest(this);
    }

    public void openMetadataGetter() {
        nameStickerPackViewModel.getNameStickerPack().observe(
                this, name -> {
                    saveNamePack(name);
                    openGallery(name);
                });

        nameStickerPackViewModel.getErrorNameStickerPack().observe(
                this, error -> {
                    Log.e("ERRO", error);
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                });

        NameStickerPackViewModel.launchNameStickerPack(this);
    }

    public abstract void openGallery(String namePack);

    public final ViewTreeObserver.OnGlobalLayoutListener pageLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            setNumColumns(recyclerView.getWidth() /
                    recyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size));
        }
    };

    public void setupStickerPackView(StickerPack stickerPack) {
        layoutManager = new GridLayoutManager(this, 1);

        ImageView expandedStickerView = findViewById(R.id.sticker_details_expanded_sticker);
        expandedStickerView.setVisibility(View.GONE);

        recyclerView = findViewById(R.id.sticker_list_to_package);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(pageLayoutListener);
        recyclerView.addOnScrollListener(dividerScrollListener);

        divider = findViewById(R.id.divider);

        if (stickerPreviewAdapter == null) {
            stickerPreviewAdapter = new StickerPreviewAdapter(
                    getLayoutInflater(), R.drawable.sticker_error, getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size),
                    getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_padding), stickerPack, expandedStickerView);

            recyclerView.setAdapter(stickerPreviewAdapter);
        }

        FloatingActionButton floatingActionButton = findViewById(R.id.button_select_media);
        floatingActionButton.setVisibility(View.GONE);
    }

    public void goToEntryActivity() {
        Intent intent = new Intent(context, EntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onSupportNavigateUp() {
        goToEntryActivity();
        return true;
    }

    public final RecyclerView.OnScrollListener dividerScrollListener = new RecyclerView.OnScrollListener() {
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

        public void updateDivider(RecyclerView recyclerView) {
            boolean showDivider = recyclerView.computeVerticalScrollOffset() > 0;
            if (divider != null) {
                divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
            }
        }
    };

    @SuppressLint("NotifyDataSetChanged")
    public void setNumColumns(int numColumns) {
        if (this.numColumns != numColumns) {
            layoutManager.setSpanCount(numColumns);

            this.numColumns = numColumns;
            if (stickerPreviewAdapter != null) {
                stickerPreviewAdapter.notifyDataSetChanged();
            }
        }
    }
}
