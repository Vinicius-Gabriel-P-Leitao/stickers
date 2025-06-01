/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.vinicius.sticker.R;
import com.vinicius.sticker.core.exception.content.ContentProviderException;
import com.vinicius.sticker.core.pattern.StickerPackValidationResult;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.domain.service.fetch.FetchStickerPackService;
import com.vinicius.sticker.view.core.base.BaseActivity;
import com.vinicius.sticker.view.feature.stickerpack.creation.activity.InitialStickerPackCreationActivity;
import com.vinicius.sticker.view.feature.stickerpack.details.activity.StickerPackDetailsActivity;
import com.vinicius.sticker.view.feature.stickerpack.list.activity.StickerPackListActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntryActivity extends BaseActivity {
    private final static String TAG_LOG = EntryActivity.class.getSimpleName();

    private LoadListAsyncTask loadListAsyncTask;
    private View progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        overridePendingTransition(0, 0);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        progressBar = findViewById(R.id.entry_activity_progress);
    }

    private void showStickerPack(ArrayList<StickerPack> stickerPackList) {
        progressBar.setVisibility(View.GONE);

        if (stickerPackList == null || stickerPackList.isEmpty()) {
            showErrorMessage("Nenhum sticker pack encontrado.");
            return;
        }

        if (stickerPackList.size() > 1) {
            final Intent intent = new Intent(this, StickerPackListActivity.class);

            // TODO: Passar pacotes com sticker invalidos e pacotes invalidos
            intent.putParcelableArrayListExtra(StickerPackListActivity.EXTRA_STICKER_PACK_LIST_DATA, stickerPackList);

            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        } else {
            final Intent intent = new Intent(this, StickerPackDetailsActivity.class);

            // TODO: Passar pacote com sticker invalido e pacote invalido
            intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, false);
            intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, stickerPackList.get(0));

            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        }
    }

    private void showErrorMessage(String errorMessage) {
        progressBar.setVisibility(View.GONE);
        Log.e(TAG_LOG, "Erro ao buscar pacote de figurinhas, " + errorMessage);
        final TextView errorMessageTV = findViewById(R.id.error_message);
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

    private final ActivityResultLauncher<Intent> createPackLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadStickerPacks();
                }
            });

    private void loadStickerPacks() {
        progressBar.setVisibility(View.VISIBLE);

        loadListAsyncTask = new LoadListAsyncTask(this);
        loadListAsyncTask.execute(createPackLauncher);
    }

    static class LoadListAsyncTask {
        private final WeakReference<EntryActivity> contextWeakReference;

        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final Handler handler = new Handler(Looper.getMainLooper());

        LoadListAsyncTask(EntryActivity activity) {
            this.contextWeakReference = new WeakReference<>(activity);
        }

        public void execute(ActivityResultLauncher<Intent> createPackLauncher) {
            executor.execute(() -> {
                Pair<String, ArrayList<StickerPack>> result = new Pair<>(null, null);

                try {
                    final Context context = contextWeakReference.get();

                    if (context != null) {
                        StickerPackValidationResult.ListStickerPackResult stickerPackList = FetchStickerPackService.fetchStickerPackListFromContentProvider(context);

                        if (stickerPackList.validStickerPacks().isEmpty()) {
                            result = new Pair<>("Nenhum pacote de adesivos disponível", null);
                            return;
                        }

                        Log.d(TAG_LOG, String.valueOf(stickerPackList.invalidStickerPacks()));
                        Log.d(TAG_LOG, String.valueOf(stickerPackList.invalidStickers()));

                        result = new Pair<>(null, stickerPackList.validStickerPacks());
                    } else {
                        result = new Pair<>("Não foi possível obter os pacotes de figurinhas", null);
                    }
                } catch (ContentProviderException exception) {
                    Context context = contextWeakReference.get();

                    if (context != null) {
                        Intent intent = new Intent(context, InitialStickerPackCreationActivity.class);

                        intent.putExtra("database_empty", true);
                        intent.putExtra(InitialStickerPackCreationActivity.EXTRA_SHOW_UP_BUTTON, false);

                        createPackLauncher.launch(intent);
                        return;
                    }

                    Log.e(TAG_LOG, "Erro ao buscar pacotes de figurinhas, banco de dados vazio", exception);
                    result = new Pair<>("Erro encontrado, redirecionando...", null);

                } catch (Exception exception) {
                    Log.e(TAG_LOG, "Erro ao obter pacotes de figurinhas", exception);
                    result = new Pair<>(exception.getMessage(), null);
                }

                Pair<String, ArrayList<StickerPack>> finalResult = result;
                handler.post(() -> {
                    EntryActivity entryActivity = contextWeakReference.get();
                    if (entryActivity != null) {
                        if (finalResult.first != null) {
                            entryActivity.showErrorMessage(finalResult.first);
                        } else {
                            entryActivity.showStickerPack(finalResult.second);
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
