/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.usecase.activity;

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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

import br.arch.sticker.R;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.orchestrator.StickerPackOrchestrator;
import br.arch.sticker.view.core.base.BaseActivity;
import br.arch.sticker.view.core.usecase.definition.DefinePermissionsToRequest;
import br.arch.sticker.view.feature.preview.adapter.StickerPreviewAdapter;
import br.arch.sticker.view.feature.stickerpack.creation.fragment.MediaPickerFragment;
import br.arch.sticker.view.feature.stickerpack.creation.viewmodel.MediaPickerViewModel;
import br.arch.sticker.view.feature.stickerpack.creation.viewmodel.NameStickerPackViewModel;
import br.arch.sticker.view.feature.stickerpack.creation.viewmodel.PermissionRequestViewModel;
import br.arch.sticker.view.main.EntryActivity;

public abstract class StickerPackCreationBaseActivity extends BaseActivity {
    private final static String TAG_LOG = StickerPackCreationBaseActivity.class.getSimpleName();

    public static final String STATIC_STICKER = "animated";
    public static final String ANIMATED_STICKER = "static";

    public MediaPickerViewModel mediaPickerViewModel;
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
        mediaPickerViewModel = new ViewModelProvider(this).get(MediaPickerViewModel.class);
        permissionRequestViewModel = new ViewModelProvider(this).get(PermissionRequestViewModel.class);
        nameStickerPackViewModel = new ViewModelProvider(this).get(NameStickerPackViewModel.class);

        mediaPickerViewModel.getStickerPackPreview().observe(this, this::setupStickerPackView);

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

    public static void launchOwnGallery(FragmentActivity activity) {
        FragmentManager supportFragmentManager = activity.getSupportFragmentManager();
        Fragment existing = supportFragmentManager.findFragmentByTag(MediaPickerFragment.class.getSimpleName());

        if (existing != null && existing.isVisible()) {
            return;
        }

        MediaPickerFragment fragment = MediaPickerFragment.newInstance(imagePath -> {
            Uri selectedImageUri = Uri.fromFile(new File(imagePath));
            Intent resultIntent = new Intent();
            resultIntent.setData(selectedImageUri);

            activity.setResult(RESULT_OK, resultIntent);
            activity.finish();
        });

        fragment.show(supportFragmentManager, MediaPickerFragment.class.getSimpleName());
    }

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
                    getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_padding), stickerPack, new ArrayList<>(),
                    expandedStickerView);

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
