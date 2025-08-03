/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.main;

import static br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.*;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.arch.sticker.R;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerException;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerPackException;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.dto.ListStickerPackValidationResult;
import br.arch.sticker.domain.dto.StickerPackWithInvalidStickers;
import br.arch.sticker.domain.service.fetch.FetchStickerPackService;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.view.core.base.BaseActivity;
import br.arch.sticker.view.feature.stickerpack.creation.activity.InitialStickerPackCreationActivity;
import br.arch.sticker.view.feature.preview.activity.StickerPackDetailsActivity;
import br.arch.sticker.view.feature.stickerpack.list.activity.StickerPackListActivity;

public class EntryActivity extends BaseActivity {
    private final static String TAG_LOG = EntryActivity.class.getSimpleName();

    private ApplicationTranslate applicationTranslate;
    private LoadListAsyncTask loadListAsyncTask;
    private View progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        applicationTranslate = new ApplicationTranslate(getResources());

        overridePendingTransition(0, 0);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        progressBar = findViewById(R.id.entry_activity_progress);
    }

    private final ActivityResultLauncher<Intent> createPackLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadStickerPacks();
                }
            });

    private void showStickerPack(ArrayList<StickerPack> validPacks, ArrayList<StickerPack> invalidPacks, HashMap<StickerPack, List<Sticker>> validPacksWithInvalidStickers) {
        progressBar.setVisibility(View.GONE);

        boolean hasValid = validPacks != null && !validPacks.isEmpty();
        boolean hasInvalid = invalidPacks != null && !invalidPacks.isEmpty();
        boolean hasValidWithInvalidStickers =
                validPacksWithInvalidStickers != null && !validPacksWithInvalidStickers.isEmpty();

        if (!hasValid && !hasInvalid && !hasValidWithInvalidStickers) {
            showErrorMessage(
                    applicationTranslate.translate(R.string.error_empty_sticker_pack).log(TAG_LOG, Level.ERROR).get());
            return;
        }

        if (hasValid && validPacks.size() == 1 && !hasInvalid && !hasValidWithInvalidStickers) {
            Intent intent = new Intent(this, StickerPackDetailsActivity.class);
            intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, false);
            intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, validPacks.get(0));

            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

            return;
        }

        final Intent intent = new Intent(this, StickerPackListActivity.class);

        Map<StickerPack, List<Sticker>> safeMap = validPacksWithInvalidStickers !=
                null ? validPacksWithInvalidStickers : Collections.emptyMap();

        ArrayList<StickerPackWithInvalidStickers> stickerPackWithInvalidStickers = new ArrayList<>();
        if (validPacksWithInvalidStickers != null) {
            for (Map.Entry<StickerPack, List<Sticker>> entry : validPacksWithInvalidStickers.entrySet()) {
                stickerPackWithInvalidStickers.add(
                        new StickerPackWithInvalidStickers(entry.getKey(), new ArrayList<>(entry.getValue())));
            }
        }

        intent.putParcelableArrayListExtra(StickerPackListActivity.EXTRA_STICKER_PACK_LIST_DATA, validPacks);
        intent.putParcelableArrayListExtra(StickerPackListActivity.EXTRA_INVALID_STICKER_PACK_LIST_DATA, invalidPacks);
        intent.putParcelableArrayListExtra(StickerPackListActivity.EXTRA_INVALID_STICKER_MAP_DATA,
                stickerPackWithInvalidStickers);

        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void showErrorMessage(String errorMessage) {
        progressBar.setVisibility(View.GONE);
        final TextView errorMessageTV = findViewById(R.id.error_message);
        errorMessageTV.setVisibility(View.VISIBLE);
        errorMessageTV.setText(getString(R.string.error_message, errorMessage));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loadListAsyncTask != null) {
            loadListAsyncTask.shutdown();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStickerPacks();
    }

    private void loadStickerPacks() {
        progressBar.setVisibility(View.VISIBLE);

        loadListAsyncTask = new LoadListAsyncTask(this);
        loadListAsyncTask.execute(createPackLauncher);
    }

    static class LoadListAsyncTask {
        private final WeakReference<EntryActivity> contextWeakReference;

        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final Handler handler = new Handler(Looper.getMainLooper());

        private final FetchStickerPackService fetchStickerPackService;

        private LoadListAsyncTask(EntryActivity activity) {
            this.contextWeakReference = new WeakReference<>(activity);
            this.fetchStickerPackService = new FetchStickerPackService(activity);
        }

        public void execute(ActivityResultLauncher<Intent> createPackLauncher) {
            executor.execute(() -> {
                new Pair<>(null, null);
                Pair<String, ListStickerPackValidationResult> result;
                final Context context = contextWeakReference.get();

                if (context != null) {
                    try {
                        result = new Pair<>(null, fetchStickerPackService.fetchStickerPackListFromContentProvider());
                    } catch (FetchStickerPackException | FetchStickerException exception) {
                        Intent intent = new Intent(context, InitialStickerPackCreationActivity.class);
                        intent.putExtra("database_empty", true);
                        intent.putExtra(InitialStickerPackCreationActivity.EXTRA_SHOW_UP_BUTTON, false);

                        createPackLauncher.launch(intent);
                        return;
                    } catch (Exception exception) {
                        result = new Pair<>(exception.getMessage(), null);
                    }
                } else {
                    result = new Pair<>("Invalid context application.", null);
                }

                Pair<String, ListStickerPackValidationResult> finalResult = result;
                handler.post(() -> {
                    EntryActivity entryActivity = contextWeakReference.get();
                    if (entryActivity != null) {
                        if (finalResult.first != null) {
                            entryActivity.showErrorMessage(finalResult.first);
                        } else {
                            entryActivity.showStickerPack(finalResult.second.validPacks(),
                                    finalResult.second.invalidPacks(),
                                    finalResult.second.validPacksWithInvalidStickers());
                        }
                    }
                });
            });
        }

        public void shutdown() {
            executor.shutdown();
        }
    }

}
