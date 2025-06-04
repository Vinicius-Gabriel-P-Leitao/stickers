/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * Modifications by Vinícius, 2025
 * Licensed under the Vinícius Non-Commercial Public License (VNCL)
 */

package com.vinicius.sticker.view.feature.stickerpack.details.activity;

import static com.vinicius.sticker.view.feature.stickerpack.creation.activity.StickerPackCreationActivity.ANIMATED_STICKER;
import static com.vinicius.sticker.view.feature.stickerpack.creation.activity.StickerPackCreationActivity.STATIC_STICKER;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.vinicius.sticker.R;
import com.vinicius.sticker.core.validation.WhatsappWhitelistValidator;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.domain.service.fetch.FetchStickerAssetService;
import com.vinicius.sticker.view.core.usecase.activity.StickerPackAddActivity;
import com.vinicius.sticker.view.core.usecase.component.FormatStickerPopupWindow;
import com.vinicius.sticker.view.feature.preview.adapter.StickerPreviewAdapter;
import com.vinicius.sticker.view.feature.stickerpack.creation.activity.StickerPackCreationActivity;
import com.vinicius.sticker.view.feature.stickerpack.metadata.activity.StickerPackMetadataActivity;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StickerPackDetailsActivity extends StickerPackAddActivity {

    /**
     * Do not change below values of below 3 lines as this is also used by WhatsApp
     */
    public static final String EXTRA_STICKER_PACK_LICENSE_AGREEMENT = "sticker_pack_license_agreement";
    public static final String EXTRA_STICKER_PACK_PRIVACY_POLICY = "sticker_pack_privacy_policy";
    public static final String EXTRA_STICKER_PACK_AUTHORITY = "sticker_pack_authority";
    public static final String EXTRA_STICKER_PACK_TRAY_ICON = "sticker_pack_tray_icon";
    public static final String EXTRA_STICKER_PACK_WEBSITE = "sticker_pack_website";
    public static final String EXTRA_STICKER_PACK_EMAIL = "sticker_pack_email";
    public static final String EXTRA_STICKER_PACK_NAME = "sticker_pack_name";
    public static final String EXTRA_STICKER_PACK_DATA = "sticker_pack";
    public static final String EXTRA_STICKER_PACK_ID = "sticker_pack_id";
    public static final String EXTRA_SHOW_UP_BUTTON = "show_up_button";

    /* UI */
    private WhiteListCheckAsyncTask whiteListCheckAsyncTask;
    private StickerPreviewAdapter stickerPreviewAdapter;
    private MaterialButton buttonCreateStickerPackage;
    private GridLayoutManager layoutManager;
    private RecyclerView recyclerView;
    private StickerPack stickerPack;
    private View alreadyAddedText;
    private View addButton;
    private View divider;
    private int numColumns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_pack_details);

        boolean showUpButton = getIntent().getBooleanExtra(EXTRA_SHOW_UP_BUTTON, false);
        stickerPack = getIntent().getParcelableExtra(EXTRA_STICKER_PACK_DATA);

        TextView packNameTextView = findViewById(R.id.pack_name);
        TextView packPublisherTextView = findViewById(R.id.author);
        ImageView packTrayIcon = findViewById(R.id.tray_image);
        TextView packSizeTextView = findViewById(R.id.pack_size);
        ImageView expandedStickerView = findViewById(R.id.sticker_details_expanded_sticker);

        alreadyAddedText = findViewById(R.id.already_added_text);
        layoutManager = new GridLayoutManager(this, 1);

        recyclerView = findViewById(R.id.sticker_list);
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

        packNameTextView.setText(stickerPack.name);
        packPublisherTextView.setText(stickerPack.publisher);

        packTrayIcon.setImageURI(FetchStickerAssetService.buildStickerAssetUri(stickerPack.identifier, stickerPack.trayImageFile));
        packSizeTextView.setText(Formatter.formatShortFileSize(this, stickerPack.getTotalSize()));

        buttonCreateStickerPackage = findViewById(R.id.button_redirect_create_stickers);
        buttonCreateStickerPackage.setOnClickListener(view -> {
            FormatStickerPopupWindow.popUpButtonChooserStickerModel(
                    this, buttonCreateStickerPackage, new FormatStickerPopupWindow.OnOptionClickListener() {
                        @Override
                        public void onStaticStickerSelected() {
                            openCreateStickerPackActivity(STATIC_STICKER);
                        }

                        @Override
                        public void onAnimatedStickerSelected() {
                            openCreateStickerPackActivity(ANIMATED_STICKER);
                        }
                    });
        });

        addButton = findViewById(R.id.add_to_whatsapp_button);
        addButton.setOnClickListener(view -> addStickerPackToWhatsApp(stickerPack.identifier, stickerPack.name));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(showUpButton);
            getSupportActionBar().setTitle(showUpButton ? getResources().getString(
                    R.string.title_activity_sticker_pack_details_multiple_pack) : getResources().getQuantityString(
                    R.plurals.title_activity_sticker_packs_list, 1));
        }

        findViewById(R.id.sticker_pack_animation_indicator).setVisibility(stickerPack.animatedStickerPack ? View.VISIBLE : View.GONE);
    }

    private final ViewTreeObserver.OnGlobalLayoutListener pageLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            setNumColumns(recyclerView.getWidth() /
                    recyclerView.getContext().getResources().getDimensionPixelSize(R.dimen.sticker_pack_details_image_size));
        }
    };

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

    private void launchInfoActivity(
            String publisherWebsite, String publisherEmail, String privacyPolicyWebsite, String licenseAgreementWebsite, String trayIconUriString) {
        Intent intent = new Intent(StickerPackDetailsActivity.this, StickerPackMetadataActivity.class);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_ID, stickerPack.identifier);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_WEBSITE, publisherWebsite);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_EMAIL, publisherEmail);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_PRIVACY_POLICY, privacyPolicyWebsite);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_LICENSE_AGREEMENT, licenseAgreementWebsite);
        intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_TRAY_ICON, trayIconUriString);

        startActivity(intent);
    }

    private void openCreateStickerPackActivity(String format) {
        Intent intent = new Intent(StickerPackDetailsActivity.this, StickerPackCreationActivity.class);
        intent.putExtra(StickerPackCreationActivity.EXTRA_STICKER_FORMAT, format);

        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_info && stickerPack != null) {
            Uri trayIconUri = FetchStickerAssetService.buildStickerAssetUri(stickerPack.identifier, stickerPack.trayImageFile);
            launchInfoActivity(
                    stickerPack.publisherWebsite, stickerPack.publisherEmail, stickerPack.privacyPolicyWebsite, stickerPack.licenseAgreementWebsite,
                    trayIconUri.toString());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setNumColumns(int numColumns) {
        if (this.numColumns != numColumns) {
            layoutManager.setSpanCount(numColumns);
            this.numColumns = numColumns;
            if (stickerPreviewAdapter != null) {
                stickerPreviewAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        whiteListCheckAsyncTask = new WhiteListCheckAsyncTask(this);
        whiteListCheckAsyncTask.execute(stickerPack);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (whiteListCheckAsyncTask != null) {
            whiteListCheckAsyncTask.shutdown();
            whiteListCheckAsyncTask = null;
        }
    }

    private void updateAddUI(Boolean isWhitelisted) {
        if (isWhitelisted) {
            addButton.setVisibility(View.GONE);
            alreadyAddedText.setVisibility(View.VISIBLE);
            findViewById(R.id.sticker_pack_details_tap_to_preview).setVisibility(View.GONE);
        } else {
            addButton.setVisibility(View.VISIBLE);
            alreadyAddedText.setVisibility(View.GONE);
            findViewById(R.id.sticker_pack_details_tap_to_preview).setVisibility(View.VISIBLE);
        }
    }

    static class WhiteListCheckAsyncTask {
        private final WeakReference<StickerPackDetailsActivity> stickerPackDetailsActivityWeakReference;

        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final Handler handler = new Handler(Looper.getMainLooper());

        WhiteListCheckAsyncTask(StickerPackDetailsActivity stickerPackListActivity) {
            this.stickerPackDetailsActivityWeakReference = new WeakReference<>(stickerPackListActivity);
        }

        public void execute(StickerPack stickerPack) {
            StickerPackDetailsActivity activity = stickerPackDetailsActivityWeakReference.get();
            if (activity == null) return;

            executor.execute(() -> {
                if (Thread.currentThread().isInterrupted()) return;

                StickerPackDetailsActivity currentActivity = stickerPackDetailsActivityWeakReference.get();

                if (currentActivity == null) return;

                handler.post(() -> {
                    StickerPackDetailsActivity uiActivity = stickerPackDetailsActivityWeakReference.get();
                    if (uiActivity != null) {
                        uiActivity.updateAddUI(WhatsappWhitelistValidator.isWhitelisted(currentActivity, stickerPack.identifier));
                    }
                });
            });
        }

        public void shutdown() {
            executor.shutdown();
        }
    }
}
