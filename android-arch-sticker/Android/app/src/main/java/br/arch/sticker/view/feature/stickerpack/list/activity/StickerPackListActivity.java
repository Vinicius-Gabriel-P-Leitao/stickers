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

import static br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.*;
import static br.arch.sticker.view.feature.preview.activity.PreviewInvalidStickerActivity.EXTRA_INVALID_STICKER_LIST;
import static br.arch.sticker.view.feature.preview.activity.PreviewInvalidStickerActivity.EXTRA_INVALID_STICKER_PACK;
import static br.arch.sticker.view.feature.stickerpack.creation.activity.StickerPackCreationActivity.ANIMATED_STICKER;
import static br.arch.sticker.view.feature.stickerpack.creation.activity.StickerPackCreationActivity.STATIC_STICKER;
import static br.arch.sticker.view.feature.preview.activity.StickerPackDetailsActivity.EXTRA_INVALID_STICKERS;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.text.InputType;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.arch.sticker.R;
import br.arch.sticker.core.validation.WhatsappWhitelistValidator;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.dto.StickerPackWithInvalidStickers;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.view.core.model.StickerPackListItem;
import br.arch.sticker.view.core.usecase.activity.StickerPackAddActivity;
import br.arch.sticker.view.core.usecase.component.FormatStickerPopupWindow;
import br.arch.sticker.view.core.usecase.component.AlertStickerDialog;
import br.arch.sticker.view.core.usecase.component.InputAlertStickerDialog;
import br.arch.sticker.view.feature.preview.activity.PreviewInvalidStickerActivity;
import br.arch.sticker.view.feature.stickerpack.creation.activity.StickerPackCreationActivity;
import br.arch.sticker.view.feature.preview.activity.StickerPackDetailsActivity;
import br.arch.sticker.view.feature.stickerpack.list.adapter.StickerPackListAdapter;
import br.arch.sticker.view.feature.stickerpack.list.viewholder.StickerPackListViewHolder;
import br.arch.sticker.view.feature.stickerpack.list.viewmodel.StickerPackListViewModel;
import br.arch.sticker.view.main.EntryActivity;

public class StickerPackListActivity extends StickerPackAddActivity {
    private enum Mode {
        NORMAL, RENAME, DELETE
    }

    private static final String TAG_LOG = StickerPackListActivity.class.getSimpleName();

    public static final String EXTRA_STICKER_PACK_LIST_DATA = "sticker_pack_list";
    public static final String EXTRA_INVALID_STICKER_PACK_LIST_DATA = "invalid_sticker_pack_list";
    public static final String EXTRA_INVALID_STICKER_MAP_DATA = "sticker_pack_with_invalid_stickers";
    private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;

    private StickerPackListViewModel stickerPackListViewmodel;
    private final Set<String> selectedForDeletion = new HashSet<>();
    private ApplicationTranslate applicationTranslate;
    private Mode currentMode = Mode.NORMAL;

    private LoadListStickerPackAsyncTask loadListStickerPackAsyncTask;
    private StickerPackListAdapter stickerPackListAdapter;
    private ImageButton buttonCreateStickerPackage;
    private LinearLayoutManager packLayoutManager;
    private RecyclerView packRecyclerView;

    private List<StickerPackListItem> unifiedList;

    public interface OnEventClickedListener {
        void onAddButtonClicked(StickerPack stickerPack, List<Sticker> stickers, StickerPackListItem.Status status);

        void onStickerPackClicked(StickerPack stickerPack, List<Sticker> stickers, StickerPackListItem.Status status);
    }

    private final OnEventClickedListener onEventClickedListener = new OnEventClickedListener() {
        @Override
        public void onAddButtonClicked(StickerPack stickerPack, List<Sticker> stickers, StickerPackListItem.Status status) {
            if (status == StickerPackListItem.Status.VALID) {
                addStickerPackToWhatsApp(stickerPack.identifier, stickerPack.name);
                return;
            }

            AlertStickerDialog dialog = new AlertStickerDialog(StickerPackListActivity.this);
            dialog.setTitleText(getString(R.string.dialog_invalid_stickers_title));
            dialog.setMessageText(getString(R.string.dialog_invalid_stickers_message));

            dialog.setTextIgnoreButton(getString(R.string.dialog_cancel));
            dialog.setOnIgnoreClick(fragment -> {
                addStickerPackToWhatsApp(stickerPack.identifier, stickerPack.name);
                dialog.dismiss();
            });

            dialog.setTextFixButton(getString(R.string.dialog_fix_stickers));
            dialog.setOnFixClick(fragment -> {
                Intent intent = new Intent(fragment.getContext(), PreviewInvalidStickerActivity.class);
                intent.putExtra(EXTRA_INVALID_STICKER_PACK, stickerPack.identifier);
                intent.putParcelableArrayListExtra(EXTRA_INVALID_STICKER_LIST,
                        (ArrayList<? extends Parcelable>) stickers);

                fragment.getContext().startActivity(intent);
                dialog.dismiss();
            });

            dialog.show();
        }

        @Override
        public void onStickerPackClicked(StickerPack stickerPack, List<Sticker> stickers, StickerPackListItem.Status status) {
            if (Mode.RENAME == currentMode) {
                currentMode = Mode.NORMAL;
                final InputAlertStickerDialog inputAlertStickerDialog = new InputAlertStickerDialog(
                        StickerPackListActivity.this);

                inputAlertStickerDialog.setIconImageView(R.drawable.ic_edit_24px);
                inputAlertStickerDialog.setTitleText(
                        applicationTranslate.translate(R.string.dialog_title_new_name_package).get());
                inputAlertStickerDialog.setMessageText(
                        applicationTranslate.translate(R.string.dialog_insert_new_name_message).get());
                inputAlertStickerDialog.setVisibilityFixButton(View.VISIBLE);
                inputAlertStickerDialog.setVisibilityIgnoreButton(View.VISIBLE);

                inputAlertStickerDialog.setInputType(InputType.TYPE_CLASS_TEXT);
                inputAlertStickerDialog.setTextInput(applicationTranslate.translate(R.string.dialog_rename).get());
                inputAlertStickerDialog.setTextFixButton(applicationTranslate.translate(R.string.dialog_rename).get());
                inputAlertStickerDialog.setOnFixClick(view -> {
                    String input = inputAlertStickerDialog.getUserInput();

                    if (input.isEmpty()) {
                        Toast.makeText(StickerPackListActivity.this,
                                applicationTranslate.translate(R.string.error_empty_pack_name).log(TAG_LOG, Level.ERROR)
                                        .get(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        stickerPackListViewmodel.startRename(stickerPack.identifier, input);
                    } catch (NumberFormatException numberFormatException) {
                        Toast.makeText(StickerPackListActivity.this,
                                applicationTranslate.translate(R.string.error_invalid_pack_name)
                                        .log(TAG_LOG, Level.ERROR).get(), Toast.LENGTH_SHORT).show();
                    } finally {
                        inputAlertStickerDialog.dismiss();
                    }
                });

                inputAlertStickerDialog.show();
                return;
            }

            if (Mode.DELETE == currentMode) {
                if (selectedForDeletion.contains(stickerPack.identifier)) {
                    selectedForDeletion.remove(stickerPack.identifier);
                } else {
                    selectedForDeletion.add(stickerPack.identifier);
                }

                stickerPackListAdapter.setSelectedIds(selectedForDeletion);
                return;
            }

            if (status == StickerPackListItem.Status.VALID || status == StickerPackListItem.Status.WITH_INVALID_STICKER) {
                Intent intent = new Intent(StickerPackListActivity.this, StickerPackDetailsActivity.class);

                intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, true);
                intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, stickerPack);

                if (status == StickerPackListItem.Status.WITH_INVALID_STICKER) {
                    intent.putParcelableArrayListExtra(EXTRA_INVALID_STICKERS, new ArrayList<>(stickers));
                }

                StickerPackListActivity.this.startActivity(intent);
                return;
            }

            final String stickerPackIdentifier = stickerPack.identifier;

            final AlertStickerDialog dialog = new AlertStickerDialog(StickerPackListActivity.this);
            dialog.setTitleText(getString(R.string.dialog_invalid_sticker_pack_title));
            dialog.setMessageText(getString(R.string.dialog_invalid_sticker_pack_message));

            dialog.setTextFixButton(getString(R.string.dialog_fix_sticker_pack));
            dialog.setOnFixClick(fragment -> {
                Intent intent = new Intent(fragment.getContext(), PreviewInvalidStickerActivity.class);
                intent.putExtra(EXTRA_INVALID_STICKER_PACK, stickerPackIdentifier);

                fragment.getContext().startActivity(intent);
                dialog.dismiss();
            });

            dialog.setTextIgnoreButton(getString(R.string.dialog_delete));
            dialog.setOnIgnoreClick(fragment -> {
                if (stickerPackIdentifier != null) {
                    stickerPackListViewmodel.startDeleted(stickerPackIdentifier);
                    onReloadRequested();
                    dialog.dismiss();
                }
            });

            dialog.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stickerpack_list);

        packRecyclerView = findViewById(R.id.recycler_valid_packs);

        stickerPackListViewmodel = new ViewModelProvider(this).get(StickerPackListViewModel.class);
        applicationTranslate = new ApplicationTranslate(getResources());

        ArrayList<StickerPack> validStickerPackList = getIntent().getParcelableArrayListExtra(
                EXTRA_STICKER_PACK_LIST_DATA);
        if (validStickerPackList == null) validStickerPackList = new ArrayList<>();

        ArrayList<StickerPack> invalidStickerPackList = getIntent().getParcelableArrayListExtra(
                EXTRA_INVALID_STICKER_PACK_LIST_DATA);
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
            unifiedList.add(
                    new StickerPackListItem(withInvalidStickers, StickerPackListItem.Status.WITH_INVALID_STICKER));
        }

        showStickerPack(unifiedList);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(
                    getResources().getQuantityString(R.plurals.title_activity_sticker_packs_list, unifiedList.size()));
        }

        buttonCreateStickerPackage = findViewById(R.id.action_create_sticker_pack);
        buttonCreateStickerPackage.setOnClickListener(view -> {
            final ObjectAnimator rotation = ObjectAnimator.ofFloat(buttonCreateStickerPackage, "rotation", 0f, 360f);
            rotation.setDuration(500);
            rotation.start();

            FormatStickerPopupWindow.popUpButtonChooserStickerModel(this, buttonCreateStickerPackage,
                    new FormatStickerPopupWindow.OnOptionClickListener() {
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

        ImageButton buttonRenameStickerPackage = findViewById(R.id.action_rename_sticker_pack);
        buttonRenameStickerPackage.setOnClickListener(view -> {
            currentMode = (currentMode == Mode.RENAME) ? Mode.NORMAL : Mode.RENAME;
            Toast.makeText(this, currentMode == Mode.RENAME ? applicationTranslate.translate(
                            R.string.description_select_sticker_pack_rename).log(TAG_LOG, Level.INFO)
                    .get() : applicationTranslate.translate(R.string.description_rename_mode_canceled)
                    .log(TAG_LOG, Level.INFO).get(), Toast.LENGTH_SHORT).show();
            selectedForDeletion.clear();
        });

        ImageButton buttonDeleteStickerPackage = findViewById(R.id.action_delete_sticker_pack);
        buttonDeleteStickerPackage.setOnClickListener(view -> {
            if (currentMode == Mode.DELETE) {
                if (!selectedForDeletion.isEmpty()) {
                    for (String stickerPackIdentifier : selectedForDeletion) {
                        stickerPackListViewmodel.startDeleted(stickerPackIdentifier);
                    }

                    onReloadRequested();
                }

                selectedForDeletion.clear();
                currentMode = Mode.NORMAL;
            } else {
                currentMode = Mode.DELETE;
                selectedForDeletion.clear();
                Toast.makeText(this, applicationTranslate.translate(R.string.description_select_sticker_pack_delete)
                        .log(TAG_LOG, Level.INFO).get(), Toast.LENGTH_SHORT).show();
            }

            stickerPackListAdapter.setSelectedIds(selectedForDeletion);
        });

        stickerPackListViewmodel.getDeletedStickerPack().observe(this, wasDeleted -> {
            if (Boolean.TRUE.equals(wasDeleted.first)) {
                stickerPackListAdapter.removeStickerPackByIdentifier(wasDeleted.second);
            }
        });

        stickerPackListViewmodel.getUpdatedStickerPackName().observe(this, renamed -> {
            if (renamed != null) {
                String renamedId = renamed.first;
                String newName = renamed.second;

                for (int counter = 0; counter < unifiedList.size(); counter++) {
                    StickerPackListItem item = unifiedList.get(counter);

                    if (item.stickerPack() instanceof StickerPack stickerPack) {
                        if (stickerPack.identifier.equals(renamedId)) {
                            stickerPack.name = newName;

                            stickerPackListAdapter.notifyItemChanged(counter);
                            break;
                        }
                    }

                    if (item.stickerPack() instanceof StickerPackWithInvalidStickers withInvalidStickers) {
                        if (withInvalidStickers.stickerPack.identifier.equals(renamedId)) {
                            withInvalidStickers.stickerPack.name = newName;

                            stickerPackListAdapter.notifyItemChanged(counter);
                            break;
                        }
                    }
                }
            }
        });

        stickerPackListViewmodel.getErrorMessageLiveData().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, applicationTranslate.translate(R.string.error_delete_sticker_pack)
                        .log(TAG_LOG, Level.ERROR, errorMessage).get(), Toast.LENGTH_LONG).show();
            }
        });
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

    private void onReloadRequested() {
        final Intent intent = new Intent(this, EntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void showStickerPack(List<StickerPackListItem> stickerPackListItems) {
        stickerPackListAdapter = new StickerPackListAdapter(this, stickerPackListItems, onEventClickedListener);
        packRecyclerView.setAdapter(stickerPackListAdapter);

        packLayoutManager = new LinearLayoutManager(this);
        packLayoutManager.setOrientation(RecyclerView.VERTICAL);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(packRecyclerView.getContext(),
                packLayoutManager.getOrientation());

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

            stickerPackListAdapter.setImageRowSpec(maxNumberOfImagesInARow, minMarginBetweenImages);
        }
    }

    private void openCreateStickerPackActivity(String format) {
        final Intent intent = new Intent(StickerPackListActivity.this, StickerPackCreationActivity.class);
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

        private final WhatsappWhitelistValidator whatsappWhitelistValidator;

        LoadListStickerPackAsyncTask(StickerPackListActivity stickerPackLibraryActivity) {
            this.stickerPackListActivityWeakReference = new WeakReference<>(stickerPackLibraryActivity);
            this.whatsappWhitelistValidator = new WhatsappWhitelistValidator(stickerPackLibraryActivity);
        }

        public void execute(StickerPackListItem[] stickerPackArray) {
            StickerPackListActivity activity = stickerPackListActivityWeakReference.get();
            if (activity == null) return;

            executor.execute(() -> {
                StickerPackListActivity currentActivity = stickerPackListActivityWeakReference.get();
                if (currentActivity == null) return;

                for (StickerPackListItem stickerPackListItem : stickerPackArray) {
                    if (stickerPackListItem.status() == StickerPackListItem.Status.VALID || stickerPackListItem.status() == StickerPackListItem.Status.INVALID) {

                        StickerPack stickerPack = (StickerPack) stickerPackListItem.stickerPack();
                        stickerPack.setIsWhitelisted(whatsappWhitelistValidator.isWhitelisted(stickerPack.identifier));
                    }
                }

                List<StickerPackListItem> resultList = new ArrayList<>(Arrays.asList(stickerPackArray));
                handler.post(() -> {
                    StickerPackListActivity uiActivity = stickerPackListActivityWeakReference.get();
                    if (uiActivity != null) {
                        uiActivity.stickerPackListAdapter.updateStickerPackItems(resultList);
                    }
                });
            });
        }

        public void shutdown() {
            executor.shutdown();
        }
    }
}