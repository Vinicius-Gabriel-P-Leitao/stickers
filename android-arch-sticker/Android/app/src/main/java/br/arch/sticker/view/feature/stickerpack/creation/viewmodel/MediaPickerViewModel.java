/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.stickerpack.creation.viewmodel;

import android.app.Application;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import br.arch.sticker.core.error.throwable.base.InternalAppException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.service.save.SaveStickerPackService;
import br.arch.sticker.view.core.usecase.definition.MimeTypesSupported;
import br.arch.sticker.view.core.util.convert.ConvertMediaToStickerFormat;

public class MediaPickerViewModel extends AndroidViewModel {

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
    private final MutableLiveData<List<File>> convertedFilesLiveData = new MutableLiveData<>();

    public MediaPickerViewModel(@NonNull Application application)
        {
            super(application);

            convertedFilesLiveData.observeForever(files -> {
                if (files != null) {
                    generateStickerPack(files);
                }
            });
        }

    private Context getAppContext()
        {
            return getApplication().getApplicationContext();
        }

    public LiveData<CallbackResult<StickerPack>> getStickerPackResult()
        {
            return stickerPackResult;
        }

    public LiveData<StickerPack> getStickerPackPreview()
        {
            return stickerPackPreview;
        }

    public LiveData<MimeTypesSupported> getMimeTypesSupported()
        {
            return mimeTypesSupported;
        }

    public void setStickerPackPreview(StickerPack stickerPack)
        {
            stickerPackPreview.setValue(stickerPack);
        }

    public void setFragmentVisibility(Boolean visibility)
        {
            fragmentVisibility.setValue(visibility);
        }

    public void setIsAnimatedPack(Boolean animated)
        {
            isAnimatedPack.setValue(animated);
        }

    public void setNameStickerPack(String name)
        {
            nameStickerPack.setValue(name);
        }

    public void setMimeTypesSupported(MimeTypesSupported mimeTypes)
        {
            mimeTypesSupported.setValue(mimeTypes);
        }

    public void startConversions(Set<Uri> uris, Context context)
        {
            int cores = Runtime.getRuntime().availableProcessors();
            int maxThreads = Math.min(30, cores * 2);
            ExecutorService executor = new ThreadPoolExecutor(cores, maxThreads, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

            if (uris == null || uris.isEmpty()) {
                postFailure("A lista de URIs está vazia ou nula.");
                return;
            }

            totalConversions = uris.size();
            completedConversions.set(0);
            convertedFiles.clear();

            for (Uri uri : uris) {
                executor.submit(() -> {
                    try {
                        if (uri.getPath() == null) throw new Exception("URI sem caminho válido.");
                        String fileName = new File(uri.getPath()).getName();
                        convertedFiles.add(ConvertMediaToStickerFormat.convertMediaToWebPAsyncFuture(context, uri, fileName).get());

                        int done = completedConversions.incrementAndGet();
                        conversionProgress.postValue(new ProgressState(totalConversions, done, done == totalConversions));

                        if (done == totalConversions) {
                            convertedFilesLiveData.postValue(new ArrayList<>(convertedFiles));
                        }
                    } catch (Exception exception) {
                        postFailure("Erro na conversão: " + exception.getMessage());
                    }
                });
            }
        }

    private void generateStickerPack(List<File> files)
        {
            try {
                Boolean animated = isAnimatedPack.getValue();
                String name = nameStickerPack.getValue();

                if (animated == null || name == null || name.isBlank()) {
                    throw new IllegalArgumentException("Dados insuficientes para gerar o pacote.");
                }

                Context context = getAppContext();
                if (context == null) {
                    throw new IllegalStateException("Contexto inválido para gerar o pack.");
                }

                Executors.newSingleThreadExecutor().submit(() -> {
                    try {
                        CallbackResult<StickerPack> result = SaveStickerPackService.saveStickerPackAsync(context, animated, files, name).get();
                        stickerPackResult.postValue(result);
                    } catch (Exception exception) {
                        postFailure(exception.getMessage());
                    }
                });
            } catch (InternalAppException internalAppException) {
                stickerPackResult.postValue(CallbackResult.failure(internalAppException));
            }
        }

    private void postFailure(String message)
        {
            stickerPackResult.postValue(CallbackResult.failure(new Exception(message)));
        }

    public record ProgressState(int total, int completed, boolean done) {
    }
}

