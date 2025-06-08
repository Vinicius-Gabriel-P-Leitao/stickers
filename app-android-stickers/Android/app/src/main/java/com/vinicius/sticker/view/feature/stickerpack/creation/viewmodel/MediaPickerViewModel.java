/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.feature.stickerpack.creation.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.vinicius.sticker.core.exception.base.InternalAppException;
import com.vinicius.sticker.core.exception.sticker.StickerPackSaveException;
import com.vinicius.sticker.core.pattern.CallbackResult;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.domain.orchestrator.StickerPackOrchestrator;
import com.vinicius.sticker.view.core.usecase.definition.MimeTypesSupported;
import com.vinicius.sticker.view.core.util.ConvertMediaToStickerFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MediaPickerViewModel extends ViewModel {
    private final List<File> convertedFiles = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger completedConversions = new AtomicInteger(0);
    private int totalConversions = 0;

    public final MutableLiveData<CallbackResult<StickerPack>> stickerPackResult = new MutableLiveData<>();
    private final MutableLiveData<StickerPack> stickerPackPreview = new MutableLiveData<>();
    private final MutableLiveData<Boolean> fragmentVisibility = new MutableLiveData<>(false);
    private final MutableLiveData<ProgressState> conversionProgress = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isAnimatedPack = new MutableLiveData<>();
    public final MutableLiveData<String> nameStickerPack = new MutableLiveData<>();
    public final MutableLiveData<MimeTypesSupported> mimeTypesSupported = new MutableLiveData<>();

    public MutableLiveData<CallbackResult<StickerPack>> getStickerPackResult() {
        return stickerPackResult;
    }

    public LiveData<StickerPack> getStickerPackPreview() {
        return stickerPackPreview;
    }

    public MutableLiveData<MimeTypesSupported> getMimeTypesSupported() {
        return mimeTypesSupported;
    }

    public void setStickerPackResult(CallbackResult<StickerPack> stickerPack) {
        stickerPackResult.setValue(stickerPack);
    }

    public void setStickerPackPreview(StickerPack stickerPack) {
        stickerPackPreview.setValue(stickerPack);
    }

    public void setFragmentVisibility(Boolean visibility) {
        fragmentVisibility.setValue(visibility);
    }

    public void setIsAnimatedPack(Boolean animated) {
        isAnimatedPack.setValue(animated);
    }

    public void setNameStickerPack(String name) {
        nameStickerPack.setValue(name);
    }

    public void setMimeTypesSupported(MimeTypesSupported mimeTypes) {
        mimeTypesSupported.setValue(mimeTypes);
    }

    public void startConversions(Set<Uri> uris, Context context) {
        int cores = Runtime.getRuntime().availableProcessors();
        int maxThreads = Math.min(30, cores * 2);
        ExecutorService executor = new ThreadPoolExecutor(cores, maxThreads, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        if (uris == null) {
            postFailure("A lista de URIS para buscar as midias são nulas!");
            return;
        }

        totalConversions = uris.size();
        completedConversions.set(0);
        convertedFiles.clear();

        for (Uri uri : uris) {
            executor.submit(() -> {
                if (uri.getPath() == null) {
                    postFailure("Caminho nulo para mídia.");
                    return;
                }

                ConvertMediaToStickerFormat.convertMediaToWebP(
                        context, uri, new File(uri.getPath()).getName(), new ConvertMediaToStickerFormat.MediaConversionCallback() {
                            @Override
                            public void onSuccess(File outputFile) {
                                convertedFiles.add(outputFile);
                                int done = completedConversions.incrementAndGet();
                                conversionProgress.postValue(new ProgressState(totalConversions, done, done == totalConversions));

                                if (done == totalConversions) {
                                    generateStickerPack(context, isAnimatedPack.getValue(), nameStickerPack.getValue());
                                }
                            }

                            @Override
                            public void onError(Exception exception) {
                                postFailure(exception.getMessage());
                            }
                        });
            });
        }
    }

    private void generateStickerPack(Context context, Boolean isAnimatedPack, String nameStickerPack) {
        StickerPackOrchestrator.generateObjectToSave(
                context, isAnimatedPack, convertedFiles, nameStickerPack, callbackResult -> {
                    if (context != null) {
                        switch (callbackResult.getStatus()) {
                            case SUCCESS:
                                setStickerPackResult(CallbackResult.success(callbackResult.getData()));
                                break;
                            case WARNING:
                                Toast.makeText(context, "Aviso: " + callbackResult.getWarningMessage(), Toast.LENGTH_SHORT).show();
                                setStickerPackResult(CallbackResult.warning(callbackResult.getWarningMessage()));
                                break;
                            case FAILURE:
                                if (callbackResult.getError() instanceof InternalAppException exception) {
                                    Toast.makeText(context, exception.getMessage(), Toast.LENGTH_SHORT).show();
                                    setStickerPackResult(CallbackResult.failure(callbackResult.getError()));
                                    break;
                                }

                                Toast.makeText(context, callbackResult.getError().getMessage(), Toast.LENGTH_SHORT).show();
                                setStickerPackResult(CallbackResult.failure(callbackResult.getError()));
                                break;
                        }
                    } else {
                        throw new StickerPackSaveException("Fragment ou Contexto não estão mais válidos.");
                    }
                });
    }

    private void postFailure(String message) {
        stickerPackResult.postValue(CallbackResult.failure(new Exception(message)));
    }

    private record ProgressState(int total, int completed, boolean done) {
    }
}
