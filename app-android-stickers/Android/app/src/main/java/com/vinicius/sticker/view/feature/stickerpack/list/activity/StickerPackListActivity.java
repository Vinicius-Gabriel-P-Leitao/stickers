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

package com.vinicius.sticker.view.feature.stickerpack.list.activity;

import static com.vinicius.sticker.view.feature.stickerpack.creation.activity.StickerPackCreationActivity.ANIMATED_STICKER;
import static com.vinicius.sticker.view.feature.stickerpack.creation.activity.StickerPackCreationActivity.STATIC_STICKER;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.vinicius.sticker.R;
import com.vinicius.sticker.core.validation.WhatsappWhitelistValidator;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.view.core.usecase.activity.StickerPackAddActivity;
import com.vinicius.sticker.view.core.usecase.component.FormatStickerPopupWindow;
import com.vinicius.sticker.view.feature.stickerpack.creation.activity.StickerPackCreationActivity;
import com.vinicius.sticker.view.feature.stickerpack.list.adapter.StickerPackListAdapter;
import com.vinicius.sticker.view.feature.stickerpack.list.viewholder.StickerPackListViewHolder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StickerPackListActivity extends StickerPackAddActivity {
    public static final String EXTRA_STICKER_PACK_LIST_DATA = "sticker_pack_list";
    private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;
    private StickerPackListAdapter allStickerPacksListAdapter;
    private LoadListStickerPackAsyncTask loadListStickerPackAsyncTask;
    private MaterialButton buttonCreateStickerPackage;
    private ArrayList<StickerPack> stickerPackList;
    private LinearLayoutManager packLayoutManager;
    private RecyclerView packRecyclerView;

    private final StickerPackListAdapter.OnAddButtonClickedListener onAddButtonClickedListener = pack -> addStickerPackToWhatsApp(
            pack.identifier,
            pack.name);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_pack_list);

        packRecyclerView = findViewById(R.id.sticker_pack_list);

        stickerPackList = getIntent().getParcelableArrayListExtra(EXTRA_STICKER_PACK_LIST_DATA);
        showStickerPackList(stickerPackList);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getQuantityString(R.plurals.title_activity_sticker_packs_list, stickerPackList.size()));
        }

        buttonCreateStickerPackage = findViewById(R.id.button_redirect_create_stickers);
        buttonCreateStickerPackage.setOnClickListener(view -> FormatStickerPopupWindow.popUpButtonChooserStickerModel(
                this, buttonCreateStickerPackage, new FormatStickerPopupWindow.OnOptionClickListener() {
                    @Override
                    public void onStaticStickerSelected() {
                        openCreateStickerPackActivity(STATIC_STICKER);
                    }

                    @Override
                    public void onAnimatedStickerSelected() {
                        openCreateStickerPackActivity(ANIMATED_STICKER);
                    }
                }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadListStickerPackAsyncTask = new LoadListStickerPackAsyncTask(this);
        loadListStickerPackAsyncTask.execute(stickerPackList.toArray(new StickerPack[0]));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (loadListStickerPackAsyncTask != null) {
            loadListStickerPackAsyncTask.shutdown();
        }
    }

    private void showStickerPackList(List<StickerPack> stickerPackList) {
        allStickerPacksListAdapter = new StickerPackListAdapter(stickerPackList, onAddButtonClickedListener);
        packRecyclerView.setAdapter(allStickerPacksListAdapter);

        packLayoutManager = new LinearLayoutManager(this);
        packLayoutManager.setOrientation(RecyclerView.VERTICAL);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(packRecyclerView.getContext(), packLayoutManager.getOrientation());

        packRecyclerView.addItemDecoration(dividerItemDecoration);
        packRecyclerView.setLayoutManager(packLayoutManager);
        packRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(this::recalculateColumnCount);
    }

    private void recalculateColumnCount() {
        final int previewSize = getResources().getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size);
        int firstVisibleItemPosition = packLayoutManager.findFirstVisibleItemPosition();

        StickerPackListViewHolder viewHolder = (StickerPackListViewHolder) packRecyclerView.findViewHolderForAdapterPosition(
                firstVisibleItemPosition);

        if (viewHolder != null) {
            final int widthOfImageRow = viewHolder.imageRowView.getMeasuredWidth();
            final int max = Math.max(widthOfImageRow / previewSize, 1);

            int maxNumberOfImagesInARow = Math.min(STICKER_PREVIEW_DISPLAY_LIMIT, max);
            int minMarginBetweenImages = (widthOfImageRow - maxNumberOfImagesInARow * previewSize) / (maxNumberOfImagesInARow - 1);

            allStickerPacksListAdapter.setImageRowSpec(maxNumberOfImagesInARow, minMarginBetweenImages);
        }
    }

        private void openCreateStickerPackActivity(String format) {
            Intent intent = new Intent(StickerPackListActivity.this, StickerPackCreationActivity.class);
            intent.putExtra(StickerPackCreationActivity.EXTRA_STICKER_FORMAT, format);

            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }

    static class LoadListStickerPackAsyncTask {
        private final WeakReference<StickerPackListActivity> stickerPackListActivityWeakReference;

        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final Handler handler = new Handler(Looper.getMainLooper());

        LoadListStickerPackAsyncTask(StickerPackListActivity stickerPackLibraryActivity) {
            this.stickerPackListActivityWeakReference = new WeakReference<>(stickerPackLibraryActivity);
        }

        public void execute(StickerPack[] stickerPackArray) {
            StickerPackListActivity activity = stickerPackListActivityWeakReference.get();
            if (activity == null) return;

            executor.execute(() -> {
                StickerPackListActivity currentActivity = stickerPackListActivityWeakReference.get();
                if (currentActivity == null) return;

                for (StickerPack stickerPack : stickerPackArray) {
                    stickerPack.setIsWhitelisted(WhatsappWhitelistValidator.isWhitelisted(currentActivity, stickerPack.identifier));
                }

                List<StickerPack> resultList = new ArrayList<>(Arrays.asList(stickerPackArray));
                handler.post(() -> {
                    StickerPackListActivity uiActivity = stickerPackListActivityWeakReference.get();
                    if (uiActivity != null) {
                        uiActivity.allStickerPacksListAdapter.addStickerPack(resultList);
                    }
                });
            });
        }

        public void shutdown() {
            executor.shutdown();
        }
    }
}
