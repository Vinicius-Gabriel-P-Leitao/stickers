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
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import br.arch.sticker.R;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.view.core.base.BaseActivity;
import br.arch.sticker.view.core.usecase.definition.DefinePermissionsToRequest;
import br.arch.sticker.view.feature.preview.adapter.StickerPreviewAdapter;
import br.arch.sticker.view.feature.stickerpack.creation.dialog.PermissionRequestDialog;
import br.arch.sticker.view.feature.stickerpack.creation.dialog.PermissionSettingsDialog;
import br.arch.sticker.view.feature.stickerpack.creation.fragment.MediaPickerFragment;
import br.arch.sticker.view.feature.stickerpack.creation.viewmodel.NameStickerPackViewModel;
import br.arch.sticker.view.feature.stickerpack.creation.viewmodel.PermissionRequestViewModel;
import br.arch.sticker.view.feature.stickerpack.creation.viewmodel.PermissionSettingsViewModel;
import br.arch.sticker.view.feature.stickerpack.creation.viewmodel.StickerPackCreationViewModel;
import br.arch.sticker.view.main.EntryActivity;

public abstract class StickerPackCreationBaseActivity extends BaseActivity {
    private final static String TAG_LOG = StickerPackCreationBaseActivity.class.getSimpleName();

    public static final String STATIC_STICKER = "animated";
    public static final String ANIMATED_STICKER = "static";

    private ActivityResultLauncher<String[]> permissionLauncher;

    public StickerPackCreationViewModel stickerPackCreationViewModel;
    public PermissionSettingsViewModel permissionSettingsViewModel;
    public PermissionRequestViewModel permissionRequestViewModel;
    public NameStickerPackViewModel nameStickerPackViewModel;

    private PermissionSettingsDialog permissionSettingsDialog;
    private PermissionRequestDialog permissionRequestDialog;
    public StickerPreviewAdapter stickerPreviewAdapter;

    public GridLayoutManager layoutManager;
    public RecyclerView recyclerView;
    public String namePack;
    public int numColumns;
    public View divider;

    public Context context;

    public final RecyclerView.OnScrollListener dividerScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int newState)
            {
                super.onScrollStateChanged(recyclerView, newState);
                updateDivider(recyclerView);
            }

        @Override
        public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy)
            {
                super.onScrolled(recyclerView, dx, dy);
                updateDivider(recyclerView);
            }

        public void updateDivider(RecyclerView recyclerView)
            {
                boolean showDivider = recyclerView.computeVerticalScrollOffset() > 0;
                if (divider != null) {
                    divider.setVisibility(showDivider
                                          ? View.VISIBLE
                                          : View.INVISIBLE);
                }
            }
    };

    public void setNamePack(String namePack)
        {
            this.namePack = namePack;
        }

    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
            this.context = this;
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_create_sticker_pack);

            getViewModelStore().clear();
            permissionSettingsViewModel = new ViewModelProvider(this).get(PermissionSettingsViewModel.class);
            stickerPackCreationViewModel = new ViewModelProvider(this).get(StickerPackCreationViewModel.class);
            permissionRequestViewModel = new ViewModelProvider(this).get(PermissionRequestViewModel.class);
            nameStickerPackViewModel = new ViewModelProvider(this).get(NameStickerPackViewModel.class);

            stickerPackCreationViewModel.getStickerPackPreview().observe(this, this::setupStickerPackView);
            permissionSettingsViewModel.getOpenSettingsRequested().observe(this, requested -> {
                if (Boolean.TRUE.equals(requested)) {
                    permissionSettingsDialog.dismiss();

                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);

                    permissionSettingsViewModel.resetOpenSettingsRequested();
                }
            });

            permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                List<String> deniedPermissions = new ArrayList<>();

                for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                    String permission = entry.getKey();
                    boolean isGranted = entry.getValue();
                    Log.i(TAG_LOG, permission + ": " + isGranted);

                    if (!isGranted) {
                        allGranted = false;
                        deniedPermissions.add(permission);
                    }
                }

                if (allGranted) {
                    permissionRequestViewModel.setPermissionGranted();
                } else {
                    boolean permanentlyDenied = false;
                    for (String permission : deniedPermissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                            permanentlyDenied = true;
                            break;
                        }
                    }

                    if (permanentlyDenied) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            permissionSettingsViewModel.getOpenSettingsRequested().observe(this, granted -> {
                                if (Boolean.TRUE.equals(granted)) {
                                    permissionRequestViewModel.setPermissionGranted();
                                    permissionSettingsViewModel.getOpenSettingsRequested().removeObservers(this);
                                }
                            });

                            permissionSettingsViewModel.getPermissionDenied().observe(this, denied -> {
                                permissionRequestViewModel.setPermissionDenied();
                                permissionSettingsViewModel.getPermissionDenied().removeObservers(this);
                            });

                            permissionSettingsDialog = new PermissionSettingsDialog(this);
                            permissionSettingsDialog.showSettingsDialog();
                            if (permissionRequestDialog != null) {
                                permissionRequestDialog.dismiss();
                            }
                        }, 250);
                    } else {
                        permissionRequestViewModel.setPermissionDenied();
                    }
                }
            });

            setupUI(savedInstanceState);
        }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
        {
            super.onSaveInstanceState(outState);
            outState.putString("namePack", namePack);
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 1 && resultCode == RESULT_OK) {
                Uri selectedUri = data.getData();
                Log.d(TAG_LOG, "URI selecionada: " + selectedUri);
            }
        }

    public final ViewTreeObserver.OnGlobalLayoutListener pageLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout()
            {
                setNumColumns(recyclerView.getWidth() / recyclerView.getContext().getResources().getDimensionPixelSize(
                        R.dimen.sticker_pack_details_image_size));
            }
    };

    @Override
    public boolean onSupportNavigateUp()
        {
            goToEntryActivity();
            return true;
        }

    public abstract void setupUI(Bundle savedInstanceState);

    public abstract void openGallery(String namePack);

    public static void launchOwnGallery(FragmentActivity activity)
        {
            FragmentManager supportFragmentManager = activity.getSupportFragmentManager();
            Fragment existing = supportFragmentManager.findFragmentByTag(MediaPickerFragment.class.getSimpleName());

            if (existing != null && existing.isVisible()) {
                return;
            }

            MediaPickerFragment fragment = new MediaPickerFragment();
            fragment.setOnItemClickListener(imagePath -> {
                Uri selectedImageUri = Uri.fromFile(new File(imagePath));
                Intent resultIntent = new Intent();
                resultIntent.setData(selectedImageUri);

                activity.setResult(RESULT_OK, resultIntent);
                activity.finish();
            });

            fragment.show(supportFragmentManager, MediaPickerFragment.class.getSimpleName());
        }

    public void openMetadataGetter()
        {
            nameStickerPackViewModel.getNameStickerPack().observe(this, name -> {
                setNamePack(name);
                openGallery(name);
            });

            NameStickerPackViewModel.launchNameStickerPack(this);
        }

    public void createStickerPackFlow()
        {
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
            permissionRequestViewModel.getPermissionGranted().observe(this, granted -> {
                if (granted != null && granted) {
                    if (namePack == null || namePack.isEmpty()) {
                        openMetadataGetter();
                        permissionRequestDialog.dismiss();
                    } else {
                        openGallery(namePack);
                        permissionRequestDialog.dismiss();

                        Log.e(TAG_LOG, namePack);
                    }
                }
            });

            permissionRequestViewModel.getPermissionDenied().observe(this, denied -> {
                if (denied != null && denied) {
                    Toast.makeText(this, "Galeria não foi liberada.", Toast.LENGTH_SHORT).show();
                    permissionRequestDialog.dismiss();
                }
            });

            permissionRequestDialog = new PermissionRequestDialog(this, permissionLauncher);
            permissionRequestDialog.showPermissionDialog();
        }

    public void goToEntryActivity()
        {
            Intent intent = new Intent(context, EntryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }

    public void setupStickerPackView(StickerPack stickerPack)
        {
            layoutManager = new GridLayoutManager(this, 1);

            ImageView expandedStickerView = findViewById(R.id.sticker_details_expanded_sticker);
            expandedStickerView.setVisibility(View.GONE);

            recyclerView = findViewById(R.id.sticker_list_to_package);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(pageLayoutListener);
            recyclerView.addOnScrollListener(dividerScrollListener);

            divider = findViewById(R.id.divider);

            if (stickerPreviewAdapter == null) {
                stickerPreviewAdapter = new StickerPreviewAdapter(getLayoutInflater(), R.drawable.sticker_error,
                        getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size),
                        getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_padding), stickerPack, new ArrayList<>(),
                        expandedStickerView);

                recyclerView.setAdapter(stickerPreviewAdapter);
            }

            FloatingActionButton floatingActionButton = findViewById(R.id.button_select_media);
            floatingActionButton.setVisibility(View.GONE);
        }

    @SuppressLint("NotifyDataSetChanged")
    public void setNumColumns(int numColumns)
        {
            if (this.numColumns != numColumns) {
                layoutManager.setSpanCount(numColumns);

                this.numColumns = numColumns;
                if (stickerPreviewAdapter != null) {
                    stickerPreviewAdapter.notifyDataSetChanged();
                }
            }
        }
}
