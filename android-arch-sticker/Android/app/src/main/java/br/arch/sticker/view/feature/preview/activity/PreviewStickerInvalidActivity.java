/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.preview.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.arch.sticker.R;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.dto.StickerPackValidationResult;
import br.arch.sticker.domain.service.fetch.FetchStickerPackService;
import br.arch.sticker.view.core.base.BaseActivity;
import br.arch.sticker.view.feature.preview.adapter.InvalidStickerPreviewAdapter;
import br.arch.sticker.view.feature.stickerpack.list.viewholder.StickerPackListViewHolder;

// @formatter:off
public class PreviewStickerInvalidActivity extends BaseActivity {
    private final static String TAG_LOG = PreviewStickerInvalidActivity.class.getSimpleName();

    public static final String EXTRA_INVALID_STICKER_PACK = "invalid_sticker_pack";
    public static final String EXTRA_INVALID_STICKER_LIST = "invalid_sticker_list";

    private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;

    private PreviewStickerInvalidActivity.LoadListInvalidStickersAsyncTask loadListInvalidStickersAsyncTask;
    private InvalidStickerPreviewAdapter invalidStickerPreviewAdapter;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerViewInvalidStickers;
    private ArrayList<Sticker> stickerArrayList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_invalid_sticker);

        recyclerViewInvalidStickers = findViewById(R.id.recycler_invalid_stickers);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_preview_invalid_sticker);
        }

        String stickerPackIdentifier = getIntent().getStringExtra(EXTRA_INVALID_STICKER_PACK);
        stickerArrayList = getIntent().getParcelableArrayListExtra(EXTRA_INVALID_STICKER_LIST);

        StickerPackValidationResult stickerPackValidationResult =
                FetchStickerPackService.fetchStickerPackFromContentProvider(this, stickerPackIdentifier);

        StickerPack stickerPack = stickerPackValidationResult.stickerPack();
        List<Sticker> stickers = stickerPackValidationResult.invalidSticker();

        showInvalidStickerList(stickerArrayList);

        if (stickerPackIdentifier != null) Log.d(TAG_LOG, stickerPackIdentifier);
        if (stickerArrayList != null) Log.d(TAG_LOG, stickerArrayList.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadListInvalidStickersAsyncTask = new PreviewStickerInvalidActivity.LoadListInvalidStickersAsyncTask(this);
        loadListInvalidStickersAsyncTask.execute(stickerArrayList.toArray(new Sticker[0]));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (loadListInvalidStickersAsyncTask != null) {
            loadListInvalidStickersAsyncTask.shutdown();
        }
    }

    private void showInvalidStickerList(
            List<Sticker> stickerList
    ) {
        invalidStickerPreviewAdapter = new InvalidStickerPreviewAdapter(stickerList);
        recyclerViewInvalidStickers.setAdapter(invalidStickerPreviewAdapter);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewInvalidStickers.getContext(), linearLayoutManager.getOrientation());

        recyclerViewInvalidStickers.addItemDecoration(dividerItemDecoration);
        recyclerViewInvalidStickers.setLayoutManager(linearLayoutManager);
        recyclerViewInvalidStickers.getViewTreeObserver().addOnGlobalLayoutListener(this::recalculateColumnCount);
    }

    private void recalculateColumnCount() {
        final int previewSize = getResources().getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size);
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

        StickerPackListViewHolder viewHolder = (StickerPackListViewHolder) recyclerViewInvalidStickers.findViewHolderForAdapterPosition(
                firstVisibleItemPosition);

        if (viewHolder != null) {
            final int widthOfImageRow = viewHolder.imageRowView.getMeasuredWidth();
            final int max = Math.max(widthOfImageRow / previewSize, 1);

            int maxNumberOfImagesInARow = Math.min(STICKER_PREVIEW_DISPLAY_LIMIT, max);

            int minMarginBetweenImages = 0;
            if (maxNumberOfImagesInARow > 1) {
                minMarginBetweenImages = (widthOfImageRow - maxNumberOfImagesInARow * previewSize) / (maxNumberOfImagesInARow - 1);
            }
            invalidStickerPreviewAdapter.setImageRowSpec(maxNumberOfImagesInARow, minMarginBetweenImages);
        }
    }

    static class LoadListInvalidStickersAsyncTask {
        private final WeakReference<PreviewStickerInvalidActivity> stickerPackListActivityWeakReference;

        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final Handler handler = new Handler(Looper.getMainLooper());

        LoadListInvalidStickersAsyncTask(PreviewStickerInvalidActivity stickerPackLibraryActivity) {
            this.stickerPackListActivityWeakReference = new WeakReference<>(stickerPackLibraryActivity);
        }

        public void execute(Sticker[] stickers) {
            PreviewStickerInvalidActivity activity = stickerPackListActivityWeakReference.get();
            if (activity == null) return;

            executor.execute(() -> {
                PreviewStickerInvalidActivity currentActivity = stickerPackListActivityWeakReference.get();
                if (currentActivity == null) return;

                List<Sticker> resultList = new ArrayList<>(Arrays.asList(stickers));
                handler.post(() -> {
                    PreviewStickerInvalidActivity uiActivity = stickerPackListActivityWeakReference.get();
                    if (uiActivity != null) {
                        uiActivity.invalidStickerPreviewAdapter.updateStickerPackItems(resultList);
                    }
                });
            });
        }

        public void shutdown() {
            executor.shutdown();
        }
    }
}
