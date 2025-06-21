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
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
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
import br.arch.sticker.core.error.ErrorCodeProvider;
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

// @formatter:off
public class PreviewInvalidStickerActivity extends BaseActivity implements PreviewInvalidStickerAdapter.OnFixClickListener {
    private final static String TAG_LOG = PreviewInvalidStickerActivity.class.getSimpleName();

    public static final String EXTRA_INVALID_STICKER_PACK = "invalid_sticker_pack";
    public static final String EXTRA_INVALID_STICKER_LIST = "invalid_sticker_list";

    private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;

    private PreviewInvalidStickerPackViewModel invalidStickerPackViewModel;
    private PreviewInvalidStickerViewModel invalidStickerViewModel;

    private LoadListInvalidStickersAsyncTask loadListInvalidStickersAsyncTask;
    private PreviewInvalidStickerAdapter previewInvalidStickerAdapter;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerViewInvalidStickers;

    private ArrayList<Sticker> stickerArrayList;
    private String stickerPackIdentifier;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_preview_invalid_sticker);
            invalidStickerPackViewModel = new ViewModelProvider(this).get(PreviewInvalidStickerPackViewModel.class);
            invalidStickerViewModel = new ViewModelProvider(this).get(PreviewInvalidStickerViewModel.class);

            invalidStickerPackViewModel.getStickerPackMutableLiveData().observe(
            this, fixAction -> {
                InvalidStickerPackDialogController controller = new InvalidStickerPackDialogController(this, invalidStickerPackViewModel);
                controller.showFixAction(fixAction);
            });

            invalidStickerViewModel.getStickerMutableLiveData().observe(
            this, fixAction -> {
                PreviewInvalidStickerViewModel.FixActionSticker action = fixAction.getContentIfNotHandled();
                if (action != null) {
                    InvalidStickerDialogController controller = new InvalidStickerDialogController(this, invalidStickerViewModel);
                    controller.showFixAction(action);
                }
            });

            recyclerViewInvalidStickers = findViewById(R.id.recycler_invalid_stickers);
            TextView textInvalidTitle = findViewById(R.id.text_invalid_title);
            MaterialButton buttonFixInvalid = findViewById(R.id.button_fix_invalid);
            CardView cardViewInvalidPack = findViewById(R.id.header_container);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.title_activity_preview_invalid_sticker);
            }

            stickerPackIdentifier = getIntent().getStringExtra(EXTRA_INVALID_STICKER_PACK);
            stickerArrayList = getIntent().getParcelableArrayListExtra(EXTRA_INVALID_STICKER_LIST);

            if (stickerArrayList != null && !stickerArrayList.isEmpty()) {
                showInvalidStickerList(stickerArrayList);
                return;
            }

            try {
                StickerPackValidationResult result = FetchStickerPackService.fetchStickerPackFromContentProvider(this, stickerPackIdentifier);
                List<Sticker> invalidStickers = result.invalidSticker();

                if (invalidStickers.isEmpty()) {
                    Toast.makeText(this, "Nenhum sticker inválido encontrado.", Toast.LENGTH_SHORT).show();
                    return;
                }

                stickerArrayList = new ArrayList<>(invalidStickers);
                showInvalidStickerList(stickerArrayList);
            } catch (FetchStickerPackException exception) {
                Object[] details = exception.getDetails();
                if (details != null && details.length > 0 && details[0] instanceof StickerPack recoveredPack) {
                    cardViewInvalidPack.setVisibility(View.VISIBLE);

                    ErrorCodeProvider errorCode = exception.getErrorCode();
                    int resId = (errorCode != null) ? errorCode.getMessageResId() : R.string.throw_unknown_error;
                    textInvalidTitle.setText(getString(resId));

                    buttonFixInvalid.setOnClickListener(new View.OnClickListener() {
                        private long lastClickTime = 0;

                        @Override
                        public void onClick(View view) {
                            long now = SystemClock.elapsedRealtime();
                            if (now - lastClickTime < 1000) return;
                            lastClickTime = now;

                            invalidStickerPackViewModel.handleFixStickerPackClick(recoveredPack, exception.getErrorCode());
                        }
                    });

                    showStickerPackInvalid(recoveredPack);
                    return;
                }

                Toast.makeText(this, "Erro ao carregar pacote de figurinhas inválido!", Toast.LENGTH_SHORT).show();
            }
        }

    @Override
    public void onFixClick(Sticker sticker)
        {
            invalidStickerViewModel.handleFixStickerClick(sticker);
        }

    @Override
    protected void onResume()
        {
            super.onResume();
            if (stickerArrayList != null && !stickerArrayList.isEmpty()) {
                loadListInvalidStickersAsyncTask = new PreviewInvalidStickerActivity.LoadListInvalidStickersAsyncTask(this);
                loadListInvalidStickersAsyncTask.execute(stickerArrayList.toArray(new Sticker[0]));
            } else {
                Log.w(TAG_LOG, "stickerArrayList está nula ou vazia. Nada para validar.");
            }
        }

    @Override
    protected void onPause()
        {
            super.onPause();
            if (loadListInvalidStickersAsyncTask != null) {
                loadListInvalidStickersAsyncTask.shutdown();
            }
        }

    private void showInvalidStickerList(List<Sticker> stickerList)
        {
            if (stickerList == null || stickerList.isEmpty()) {
                Log.w(TAG_LOG, "Lista de stickers inválidos está vazia ou nula.");
                return;
            }

            stickerArrayList = new ArrayList<>(stickerList);

            previewInvalidStickerAdapter = new PreviewInvalidStickerAdapter(stickerPackIdentifier, stickerArrayList, this);
            recyclerViewInvalidStickers.setAdapter(previewInvalidStickerAdapter);
            decorateRecyclerView();
        }

    private void showStickerPackInvalid(StickerPack stickerPack)
        {
            previewInvalidStickerAdapter = new PreviewInvalidStickerAdapter(stickerPack, this);
            recyclerViewInvalidStickers.setAdapter(previewInvalidStickerAdapter);
            decorateRecyclerView();
        }

    private void decorateRecyclerView()
        {
            linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                    recyclerViewInvalidStickers.getContext(), linearLayoutManager.getOrientation());

            recyclerViewInvalidStickers.addItemDecoration(dividerItemDecoration);
            recyclerViewInvalidStickers.setLayoutManager(linearLayoutManager);
            recyclerViewInvalidStickers.getViewTreeObserver().addOnGlobalLayoutListener(this::recalculateColumnCount);
        }

    private void recalculateColumnCount()
        {
            final int previewSize = getResources().getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size);
            int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();

            InvalidStickerListViewHolder viewHolder = (InvalidStickerListViewHolder) recyclerViewInvalidStickers.findViewHolderForAdapterPosition(
                    firstVisibleItemPosition);

            if (viewHolder != null) {
                final int widthOfImageRow = viewHolder.stickerPreview.getMeasuredWidth();
                final int max = Math.max(widthOfImageRow / previewSize, 1);

                int maxNumberOfImagesInARow = Math.min(STICKER_PREVIEW_DISPLAY_LIMIT, max);

                int minMarginBetweenImages = 0;
                if (maxNumberOfImagesInARow > 1) {
                    minMarginBetweenImages = (widthOfImageRow - maxNumberOfImagesInARow * previewSize) / (maxNumberOfImagesInARow - 1);
                }
                previewInvalidStickerAdapter.setImageRowSpec(maxNumberOfImagesInARow, minMarginBetweenImages);
            }
        }

    static class LoadListInvalidStickersAsyncTask {
        private final WeakReference<PreviewInvalidStickerActivity> stickerPackListActivityWeakReference;

        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final Handler handler = new Handler(Looper.getMainLooper());

        LoadListInvalidStickersAsyncTask(PreviewInvalidStickerActivity stickerPackLibraryActivity)
            {
                this.stickerPackListActivityWeakReference = new WeakReference<>(stickerPackLibraryActivity);
            }

        public void execute(Sticker[] stickers)
            {
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

        public void shutdown()
            {
                executor.shutdown();
            }
    }
}
