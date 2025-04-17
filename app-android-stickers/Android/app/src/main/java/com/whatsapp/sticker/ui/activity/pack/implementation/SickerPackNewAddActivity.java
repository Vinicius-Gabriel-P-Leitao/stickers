package com.whatsapp.sticker.ui.activity.pack.implementation;

import static android.content.Intent.EXTRA_MIME_TYPES;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ext.SdkExtensions;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.PopupWindow;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.material.button.MaterialButton;
import com.whatsapp.sticker.R;
import com.whatsapp.sticker.core.BaseActivity;
import com.whatsapp.sticker.data.StickerPack;
import com.whatsapp.sticker.ui.adapter.StickerPreviewAdapter;

import java.util.Objects;

public class SickerPackNewAddActivity extends BaseActivity {
    public static final String EXTRA_SHOW_UP_BUTTON = "show_up_button";
    public static final String EXTRA_STICKER_PACK_DATA = "sticker_pack";
    private ActivityResultLauncher<PickVisualMediaRequest> pickGifAndVideoLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher;
    private MaterialButton buttonCreateStickerPackage;

    private RecyclerView recyclerView;
    private GridLayoutManager layoutManager;
    private StickerPreviewAdapter stickerPreviewAdapter;
    private StickerPack stickerPack;
    private int numColumns;
    View divider;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_sticker_package);

        boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, true);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(showUpButton);
            getSupportActionBar().setTitle(showUpButton ? getResources().getString(R.string.title_activity_sticker_packs_creator) : getResources().getQuantityString(R.plurals.title_activity_sticker_packs_creator_list, 1));
        }

        buttonCreateStickerPackage = findViewById(R.id.button_create_sticker_package);
        buttonCreateStickerPackage.setOnClickListener(view -> {
            popUpButtonChooserStickerModel(buttonCreateStickerPackage);
        });

        pickImageLauncher = registerForActivityResult(new PickImage(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
                if (uri != null) {
                    Log.d("MediaPicker", "Selected URI: " + uri.toString());
                } else {
                    Log.d("MediaPicker", "No media selected");
                }
            }
        });

        pickGifAndVideoLauncher = registerForActivityResult(new PickGifAndVideo(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
                if (uri != null) {
                    Log.d("MediaPicker", "Selected URI: " + uri.toString());
                } else {
                    Log.d("MediaPicker", "No media selected");
                }
            }
        });

        // Pacote tem que ser passado aqui para poder renderizar na tela
        stickerPack = getIntent().getParcelableExtra(EXTRA_STICKER_PACK_DATA);
        if (stickerPack == null) {
            Log.e("StickerPackDetailsActivity", "Erro: stickerPack é null!");
            return;
        }

        SimpleDraweeView expandedStickerView = findViewById(R.id.sticker_details_expanded_sticker);
        layoutManager = new GridLayoutManager(this, 1);
        recyclerView = findViewById(R.id.sticker_list_to_package);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(pageLayoutListener);
        recyclerView.addOnScrollListener(dividerScrollListener);
        divider = findViewById(R.id.divider);
        if (stickerPreviewAdapter == null) {
            stickerPreviewAdapter = new StickerPreviewAdapter(getLayoutInflater(), R.drawable.sticker_error, getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size), getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_padding), stickerPack, expandedStickerView);
            recyclerView.setAdapter(stickerPreviewAdapter);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(showUpButton);
            getSupportActionBar().setTitle(showUpButton ? getResources().getString(R.string.title_activity_sticker_pack_details_multiple_pack) : getResources().getQuantityString(R.plurals.title_activity_sticker_packs_list, 1));
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
                divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
            }
        }
    };

    private final ViewTreeObserver.OnGlobalLayoutListener pageLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            setNumColumns(recyclerView.getWidth() / recyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size));
        }
    };

    private void popUpButtonChooserStickerModel(@NonNull MaterialButton materialButton) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.dropdown_custom_menu, null);
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setElevation(12f);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.background_menu_dropdown));
        popupWindow.setAnimationStyle(R.style.PopupBounceAnimation);

        int[] location = new int[2];
        materialButton.getLocationOnScreen(location);

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = popupView.getMeasuredHeight();

        int margin = 16;
        int yPosition = location[1] - popupHeight - margin;

        popupWindow.showAtLocation(materialButton, Gravity.NO_GRAVITY, location[0] / 3, yPosition);

        popupView.findViewById(R.id.item_option_static).setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                openImagePicker();
            } else {
                launchLegacyGalleryForImage();
            }
        });

        popupView.findViewById(R.id.item_option_animated).setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                openGifAndVideoPicker();
            } else {
                launchLegacyGalleryForGifAndVideo();
            }
        });
    }

    private void launchLegacyGalleryForImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/* video/*");
        intent.putExtra(EXTRA_MIME_TYPES, new String[]{"image/png", "image/webp", "image/jpg"});
        startActivityForResult(intent, 1);
    }

    private void launchLegacyGalleryForGifAndVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/* video/*");
        intent.putExtra(EXTRA_MIME_TYPES, new String[]{"image/gif", "video/*"});
        startActivityForResult(intent, 1);
    }

    public void openImagePicker() {
        PickVisualMediaRequest request = new PickVisualMediaRequest.Builder().build();
        pickImageLauncher.launch(request);
    }

    public void openGifAndVideoPicker() {
        PickVisualMediaRequest request = new PickVisualMediaRequest.Builder().build();
        pickGifAndVideoLauncher.launch(request);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Uri selectedUri = data.getData();
            Log.d("MediaPicker", "Selected URI: " + selectedUri);
        }
    }

    private void setNumColumns(int columns) {
        if (this.numColumns != columns) {
            this.numColumns = columns;
            layoutManager.setSpanCount(columns);
            if (stickerPreviewAdapter != null) stickerPreviewAdapter.notifyDataSetChanged();
        }
    }

    static class PickGifAndVideo extends ActivityResultContract<PickVisualMediaRequest, Uri> {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, PickVisualMediaRequest input) {
            Intent intent = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
                intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            }
            Objects.requireNonNull(intent).setType("*/*");
            intent.putExtra(EXTRA_MIME_TYPES, new String[]{"image/gif", "video/*"});
            return intent;
        }

        @Override
        public Uri parseResult(int resultCode, @Nullable Intent intent) {
            if (resultCode == Activity.RESULT_OK && intent != null) {
                return intent.getData();
            }
            return null;
        }
    }

    static class PickImage extends ActivityResultContract<PickVisualMediaRequest, Uri> {
        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, PickVisualMediaRequest input) {
            Intent intent = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2) {
                intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
            }
            Objects.requireNonNull(intent).setType("*/*");
            intent.putExtra(EXTRA_MIME_TYPES, new String[]{"image/png", "image/webp", "image/jpg"});
            return intent;
        }

        @Override
        public Uri parseResult(int resultCode, @Nullable Intent intent) {
            if (resultCode == Activity.RESULT_OK && intent != null) {
                return intent.getData();
            }
            return null;
        }
    }
}
