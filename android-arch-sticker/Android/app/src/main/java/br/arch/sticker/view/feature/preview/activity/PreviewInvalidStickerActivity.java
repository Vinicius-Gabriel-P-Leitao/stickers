/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */
package br.arch.sticker.view.feature.preview.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
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
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerPackException;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.dto.StickerPackValidationResult;
import br.arch.sticker.domain.service.fetch.FetchStickerPackService;
import br.arch.sticker.view.core.base.BaseActivity;
import br.arch.sticker.view.feature.preview.adapter.PreviewInvalidStickerAdapter;
import br.arch.sticker.view.feature.preview.dialog.InvalidStickerDialogController;
import br.arch.sticker.view.feature.preview.dialog.InvalidStickerPackDialogController;
import br.arch.sticker.view.feature.preview.viewholder.InvalidStickerListViewHolder;
import br.arch.sticker.view.feature.preview.viewmodel.PreviewInvalidStickerPackViewModel;
import br.arch.sticker.view.feature.preview.viewmodel.PreviewInvalidStickerViewModel;
import br.arch.sticker.view.main.EntryActivity;

public class PreviewInvalidStickerActivity extends BaseActivity implements PreviewInvalidStickerAdapter.OnFixClickListener {
    private final static String TAG_LOG = PreviewInvalidStickerActivity.class.getSimpleName();

    public static final String EXTRA_INVALID_STICKER_PACK = "invalid_sticker_pack";
    public static final String EXTRA_INVALID_STICKER_LIST = "invalid_sticker_list";
    private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;

    private PreviewInvalidStickerPackViewModel invalidStickerPackViewModel;
    private PreviewInvalidStickerViewModel invalidStickerViewModel;

    private LoadListInvalidStickersAsyncTask loadListInvalidStickersAsyncTask;
    private PreviewInvalidStickerAdapter previewInvalidStickerAdapter;
    private RecyclerView recyclerViewInvalidStickers;
    private LinearLayoutManager linearLayoutManager;
    private ProgressBar progressBar;

    private ArrayList<Sticker> stickerArrayList;
    private String stickerPackIdentifier;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_invalid_sticker);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_preview_invalid_sticker);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        goToEntryActivity();
                    }
                }
        );

        invalidStickerPackViewModel = new ViewModelProvider(this).get(
                PreviewInvalidStickerPackViewModel.class);
        invalidStickerViewModel = new ViewModelProvider(this).get(
                PreviewInvalidStickerViewModel.class);

        observeInvalidStickerPackViewModel();
        observeInvalidStickerViewModel();

        recyclerViewInvalidStickers = findViewById(R.id.recycler_invalid_stickers);
        progressBar = findViewById(R.id.progress_bar_invalid);
        TextView textInvalidTitle = findViewById(R.id.text_invalid_title);
        MaterialButton buttonFixInvalid = findViewById(R.id.button_fix_invalid);
        CardView cardViewInvalidPack = findViewById(R.id.header_container);

        stickerPackIdentifier = getIntent().getStringExtra(EXTRA_INVALID_STICKER_PACK);
        stickerArrayList = getIntent().getParcelableArrayListExtra(EXTRA_INVALID_STICKER_LIST);

        if (stickerArrayList != null && !stickerArrayList.isEmpty()) {
            showInvalidStickerList(stickerArrayList);
            return;
        }

        try {
            FetchStickerPackService fetchStickerPackService = new FetchStickerPackService(this);
            StickerPackValidationResult result = fetchStickerPackService.fetchStickerPackFromContentProvider(
                    stickerPackIdentifier);
            List<Sticker> invalidStickers = result.invalidSticker();

            if (invalidStickers.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_sticker_pack_not_found),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            stickerArrayList = new ArrayList<>(invalidStickers);
            showInvalidStickerList(stickerArrayList);
        } catch (FetchStickerPackException exception) {
            Object[] details = exception.getDetails();
            if (details != null && details.length > 0 &&
                    details[0] instanceof StickerPack recoveredPack) {
                cardViewInvalidPack.setVisibility(View.VISIBLE);

                ErrorCode errorCode = exception.getErrorCode();
                int resId = (errorCode !=
                        null) ? errorCode.getMessageResId() : R.string.error_unknown;
                textInvalidTitle.setText(getString(resId));

                buttonFixInvalid.setOnClickListener(new View.OnClickListener() {
                    private long lastClickTime = 0;

                    @Override
                    public void onClick(View view) {
                        long now = SystemClock.elapsedRealtime();
                        if (now - lastClickTime < 1000) return;
                        lastClickTime = now;

                        invalidStickerPackViewModel.handleFixStickerPackClick(recoveredPack,
                                exception.getErrorCode()
                        );
                    }
                });

                showStickerPackInvalid(recoveredPack);
                return;
            }

            Toast.makeText(this, getString(R.string.error_loading_stickerpack), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onStickerFixClick(Sticker sticker, String stickerPackIdentifier) {
        invalidStickerViewModel.handleFixStickerClick(sticker, stickerPackIdentifier);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stickerArrayList != null && !stickerArrayList.isEmpty()) {
            loadListInvalidStickersAsyncTask = new PreviewInvalidStickerActivity.LoadListInvalidStickersAsyncTask(
                    this);
            loadListInvalidStickersAsyncTask.execute(stickerArrayList.toArray(new Sticker[0]));
        } else {
            Log.w(TAG_LOG, getString(R.string.error_empty_sticker_list));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (loadListInvalidStickersAsyncTask != null) {
            loadListInvalidStickersAsyncTask.shutdown();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        goToEntryActivity();
        return true;
    }

    public void goToEntryActivity() {
        Intent intent = new Intent(this, EntryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void showInvalidStickerList(List<Sticker> stickerList) {
        if (stickerList == null || stickerList.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_sticker_list),
                    Toast.LENGTH_LONG
            ).show();
            Log.w(TAG_LOG, getString(R.string.error_empty_sticker_list));
            return;
        }

        previewInvalidStickerAdapter = new PreviewInvalidStickerAdapter(stickerPackIdentifier,
                stickerList, this
        );
        recyclerViewInvalidStickers.setAdapter(previewInvalidStickerAdapter);
        decorateRecyclerView();
    }

    private void showStickerPackInvalid(StickerPack stickerPack) {
        previewInvalidStickerAdapter = new PreviewInvalidStickerAdapter(stickerPack, this);
        recyclerViewInvalidStickers.setAdapter(previewInvalidStickerAdapter);
        decorateRecyclerView();
    }

    private void observeInvalidStickerPackViewModel() {
        observeProgressBar(invalidStickerPackViewModel.getProgressLiveData());
        observeErrorMessage(invalidStickerPackViewModel.getErrorMessageLiveData());

        invalidStickerPackViewModel.getStickerMutableLiveData().observe(this, fixAction -> {
                    PreviewInvalidStickerPackViewModel.FixActionStickerPack action = fixAction.getContentIfNotHandled();
                    if (action != null) {
                        InvalidStickerPackDialogController controller = new InvalidStickerPackDialogController(
                                this, invalidStickerPackViewModel);
                        controller.showFixAction(action);
                    }
                }
        );

        invalidStickerPackViewModel.getFixCompletedLiveData().observe(this, fixAction -> {
                    // NOTE: Caso queira tratamento especial para um erro é só fazer instanceof
                    goToEntryActivity();
                }
        );
    }

    private void observeInvalidStickerViewModel() {
        observeProgressBar(invalidStickerViewModel.getProgressLiveData());
        observeErrorMessage(invalidStickerViewModel.getErrorMessageLiveData());

        invalidStickerViewModel.getStickerMutableLiveData().observe(this, fixAction -> {
                    PreviewInvalidStickerViewModel.FixActionSticker action = fixAction.getContentIfNotHandled();
                    if (action != null) {
                        InvalidStickerDialogController controller = new InvalidStickerDialogController(this,
                                invalidStickerViewModel
                        );
                        controller.showFixAction(action);
                    }
                }
        );

        invalidStickerViewModel.getFixCompletedLiveData().observe(this, fixAction -> {
                    if (fixAction instanceof PreviewInvalidStickerViewModel.FixActionSticker.Delete deleteAction) {
                        Sticker stickerDeleted = deleteAction.sticker();
                        previewInvalidStickerAdapter.removeSticker(stickerDeleted);
                        stickerArrayList.remove(stickerDeleted);
                    }

                    if (fixAction instanceof PreviewInvalidStickerViewModel.FixActionSticker.ResizeFile resizeFileAction) {
                        Sticker stickerResized = resizeFileAction.sticker();
                        previewInvalidStickerAdapter.removeSticker(stickerResized);
                        stickerArrayList.remove(stickerResized);
                    }
                }
        );
    }

    private void observeProgressBar(LiveData<Boolean> liveData) {
        liveData.observe(this, isLoading -> {
                    progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                }
        );
    }

    private void observeErrorMessage(LiveData<String> liveData) {
        liveData.observe(this, message -> {
                    if (message != null) {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void decorateRecyclerView() {
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerViewInvalidStickers.getContext(), linearLayoutManager.getOrientation());

        recyclerViewInvalidStickers.addItemDecoration(dividerItemDecoration);
        recyclerViewInvalidStickers.setLayoutManager(linearLayoutManager);
        recyclerViewInvalidStickers.getViewTreeObserver()
                .addOnGlobalLayoutListener(this::recalculateColumnCount);
    }

    private void recalculateColumnCount() {
        final int previewSize = getResources().getDimensionPixelSize(
                R.dimen.sticker_pack_list_item_preview_image_size);
        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

        InvalidStickerListViewHolder viewHolder = (InvalidStickerListViewHolder) recyclerViewInvalidStickers.findViewHolderForAdapterPosition(
                firstVisibleItemPosition);

        if (viewHolder != null) {
            final int widthOfImageRow = viewHolder.stickerPreview.getMeasuredWidth();
            final int max = Math.max(widthOfImageRow / previewSize, 1);

            int maxNumberOfImagesInARow = Math.min(STICKER_PREVIEW_DISPLAY_LIMIT, max);

            int minMarginBetweenImages = 0;
            if (maxNumberOfImagesInARow > 1) {
                minMarginBetweenImages = (widthOfImageRow - maxNumberOfImagesInARow * previewSize) /
                        (maxNumberOfImagesInARow - 1);
            }
            previewInvalidStickerAdapter.setImageRowSpec(maxNumberOfImagesInARow,
                    minMarginBetweenImages
            );
        }
    }

    static class LoadListInvalidStickersAsyncTask {
        private final WeakReference<PreviewInvalidStickerActivity> stickerPackListActivityWeakReference;

        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final Handler handler = new Handler(Looper.getMainLooper());

        LoadListInvalidStickersAsyncTask(PreviewInvalidStickerActivity stickerPackLibraryActivity) {
            this.stickerPackListActivityWeakReference = new WeakReference<>(
                    stickerPackLibraryActivity);
        }

        public void execute(Sticker[] stickers) {
            PreviewInvalidStickerActivity activity = stickerPackListActivityWeakReference.get();
            if (activity == null) return;

            executor.execute(() -> {
                PreviewInvalidStickerActivity currentActivity = stickerPackListActivityWeakReference.get();
                if (currentActivity == null) return;

                List<Sticker> resultList = new ArrayList<>(Arrays.asList(stickers));
                handler.post(() -> {
                    PreviewInvalidStickerActivity uiActivity = stickerPackListActivityWeakReference.get();
                    if (uiActivity != null) {
                        uiActivity.previewInvalidStickerAdapter.updateStickerPackItems(resultList);
                    }
                });
            });
        }

        public void shutdown() {
            executor.shutdown();
        }
    }
}
