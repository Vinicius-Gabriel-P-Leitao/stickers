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

package br.arch.sticker.view.feature.stickerpack.list.activity;

import static br.arch.sticker.view.feature.preview.activity.PreviewInvalidStickerActivity.EXTRA_INVALID_STICKER_LIST;
import static br.arch.sticker.view.feature.preview.activity.PreviewInvalidStickerActivity.EXTRA_INVALID_STICKER_PACK;
import static br.arch.sticker.view.feature.stickerpack.creation.activity.StickerPackCreationActivity.ANIMATED_STICKER;
import static br.arch.sticker.view.feature.stickerpack.creation.activity.StickerPackCreationActivity.STATIC_STICKER;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.arch.sticker.R;
import br.arch.sticker.core.validation.WhatsappWhitelistValidator;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.dto.StickerPackWithInvalidStickers;
import br.arch.sticker.view.core.usecase.activity.StickerPackAddActivity;
import br.arch.sticker.view.core.usecase.component.FormatStickerPopupWindow;
import br.arch.sticker.view.core.usecase.component.InvalidStickersDialog;
import br.arch.sticker.view.feature.preview.activity.PreviewInvalidStickerActivity;
import br.arch.sticker.view.feature.preview.fragment.DialogOperationInvalidStickerPack;
import br.arch.sticker.view.feature.stickerpack.creation.activity.StickerPackCreationActivity;
import br.arch.sticker.view.feature.stickerpack.list.adapter.StickerPackListAdapter;
import br.arch.sticker.view.core.model.StickerPackListItem;
import br.arch.sticker.view.feature.stickerpack.list.viewholder.StickerPackListViewHolder;
import br.arch.sticker.view.main.EntryActivity;

public class StickerPackListActivity extends StickerPackAddActivity implements DialogOperationInvalidStickerPack.OnDialogActionListener {
    public static final String EXTRA_STICKER_PACK_LIST_DATA = "sticker_pack_list";
    public static final String EXTRA_INVALID_STICKER_PACK_LIST_DATA = "invalid_sticker_pack_list";
    public static final String EXTRA_INVALID_STICKER_MAP_DATA = "sticker_pack_with_invalid_stickers";

    private List<StickerPackListItem> unifiedList;

    private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;

    private LoadListStickerPackAsyncTask loadListStickerPackAsyncTask;
    private StickerPackListAdapter allStickerPacksListAdapter;
    private MaterialButton buttonCreateStickerPackage;
    private LinearLayoutManager packLayoutManager;
    private RecyclerView packRecyclerView;

    private final StickerPackListAdapter.OnAddButtonClickedListener onAddButtonClickedListener = (stickerPack, stickers, isValid) -> {
        if (isValid) {
            addStickerPackToWhatsApp(stickerPack.identifier, stickerPack.name);
        } else {
            InvalidStickersDialog dialog = new InvalidStickersDialog(this);
            dialog.setTitleText(this.getString(R.string.dialog_title_invalid_stickers));
            dialog.setMessageText(this.getString(R.string.dialog_message_invalid_stickers));

            dialog.setTextIgnoreButton(this.getString(R.string.dialog_button_ignore_pack));
            dialog.setOnIgnoreClick(fragment -> {
                addStickerPackToWhatsApp(stickerPack.identifier, stickerPack.name);
                dialog.dismiss();
            });

            dialog.setTextFixButton(this.getString(R.string.dialog_button_fix_stickers));
            dialog.setOnFixClick(fragment -> {
                Intent intent = new Intent(fragment.getContext(), PreviewInvalidStickerActivity.class);
                intent.putExtra(EXTRA_INVALID_STICKER_PACK, stickerPack.identifier);
                intent.putParcelableArrayListExtra(EXTRA_INVALID_STICKER_LIST, (ArrayList<? extends Parcelable>) stickers);

                fragment.getContext().startActivity(intent);
                dialog.dismiss();
            });

            dialog.show();
        }
    };

    @Override
    public void addStickerPackToWhatsApp(String stickerPackIdentifier, String stickerPackName) {
        super.addStickerPackToWhatsApp(stickerPackIdentifier, stickerPackName);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_pack_list);

        packRecyclerView = findViewById(R.id.recycler_valid_packs);

        ArrayList<StickerPack> validStickerPackList = getIntent().getParcelableArrayListExtra(EXTRA_STICKER_PACK_LIST_DATA);
        if (validStickerPackList == null) validStickerPackList = new ArrayList<>();

        ArrayList<StickerPack> invalidStickerPackList = getIntent().getParcelableArrayListExtra(EXTRA_INVALID_STICKER_PACK_LIST_DATA);
        if (invalidStickerPackList == null) invalidStickerPackList = new ArrayList<>();

        ArrayList<StickerPackWithInvalidStickers> stickerPackWithInvalidStickers = getIntent().getParcelableArrayListExtra(
                EXTRA_INVALID_STICKER_MAP_DATA);
        if (stickerPackWithInvalidStickers == null) stickerPackWithInvalidStickers = new ArrayList<>();

        unifiedList = new ArrayList<>();
        for (StickerPack stickerPack : validStickerPackList) {
            unifiedList.add(new StickerPackListItem(stickerPack, StickerPackListItem.Status.VALID));
        }

        for (StickerPack stickerPack : invalidStickerPackList) {
            unifiedList.add(new StickerPackListItem(stickerPack, StickerPackListItem.Status.INVALID));
        }

        for (StickerPackWithInvalidStickers withInvalidStickers : stickerPackWithInvalidStickers) {
            unifiedList.add(new StickerPackListItem(withInvalidStickers, StickerPackListItem.Status.WITH_INVALID_STICKER));
        }

        showStickerPack(unifiedList);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getResources().getQuantityString(R.plurals.title_activity_sticker_packs_list, unifiedList.size()));
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
        loadListStickerPackAsyncTask.execute(unifiedList.toArray(new StickerPackListItem[0]));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (loadListStickerPackAsyncTask != null) {
            loadListStickerPackAsyncTask.shutdown();
        }
    }

    @Override
    public void onReloadRequested() {
        Intent intent = new Intent(this, EntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void showStickerPack(
            List<StickerPackListItem> validPacks
    ) {
        allStickerPacksListAdapter = new StickerPackListAdapter(validPacks, onAddButtonClickedListener, getSupportFragmentManager());
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

            int minMarginBetweenImages = 0;
            if (maxNumberOfImagesInARow > 1) {
                minMarginBetweenImages = (widthOfImageRow - maxNumberOfImagesInARow * previewSize) / (maxNumberOfImagesInARow - 1);
            }
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

        public void execute(StickerPackListItem[] stickerPackArray) {
            StickerPackListActivity activity = stickerPackListActivityWeakReference.get();
            if (activity == null) return;

            executor.execute(() -> {
                StickerPackListActivity currentActivity = stickerPackListActivityWeakReference.get();
                if (currentActivity == null) return;

                for (StickerPackListItem stickerPack : stickerPackArray) {
                    if (stickerPack.status() == StickerPackListItem.Status.VALID ||
                            stickerPack.status() == StickerPackListItem.Status.INVALID) {

                        StickerPack pack = (StickerPack) stickerPack.stickerPack();
                        pack.setIsWhitelisted(WhatsappWhitelistValidator.isWhitelisted(currentActivity, pack.identifier));
                    }
                }

                List<StickerPackListItem> resultList = new ArrayList<>(Arrays.asList(stickerPackArray));
                handler.post(() -> {
                    StickerPackListActivity uiActivity = stickerPackListActivityWeakReference.get();
                    if (uiActivity != null) {
                        uiActivity.allStickerPacksListAdapter.updateStickerPackItems(resultList);
                    }
                });
            });
        }

        public void shutdown() {
            executor.shutdown();
        }
    }
}
