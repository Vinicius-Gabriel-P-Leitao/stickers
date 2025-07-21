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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import br.arch.sticker.core.error.throwable.base.AppCoreStateException;
import br.arch.sticker.core.error.throwable.media.MediaConversionException;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.service.save.SaveStickerPackService;
import br.arch.sticker.view.core.usecase.definition.MimeTypesSupported;
import br.arch.sticker.view.core.util.convert.ConvertMediaToStickerFormat;

public class StickerPackCreationViewModel extends AndroidViewModel {
    public record ProgressState(int total, int completed, boolean done) {
    }

    private final Context context;
    private final SaveStickerPackService saveStickerPackService;
    private final ConvertMediaToStickerFormat convertMediaToStickerFormat;

    private int totalConversions = 0;
    private ExecutorService generateStickerPack;
    private ExecutorService conversionExecutor;
    private boolean conversionsCancelled = false;
    private final AtomicInteger completedConversions = new AtomicInteger(0);
    private final List<File> convertedFiles = Collections.synchronizedList(new ArrayList<>());
    private final List<Future<?>> conversionFutures = Collections.synchronizedList(
            new ArrayList<>());

    public final MutableLiveData<CallbackResult<StickerPack>> stickerPackResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> fragmentVisibility = new MutableLiveData<>(false);
    public final MutableLiveData<MimeTypesSupported> mimeTypesSupported = new MutableLiveData<>();
    private final MutableLiveData<List<File>> convertedFilesLiveData = new MutableLiveData<>();
    private final MutableLiveData<ProgressState> conversionProgress = new MutableLiveData<>();
    private final MutableLiveData<StickerPack> stickerPackPreview = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isAnimatedPack = new MutableLiveData<>();
    public final MutableLiveData<String> nameStickerPack = new MutableLiveData<>();

    public StickerPackCreationViewModel(@NonNull Application application) {
        super(application);
        this.context = getApplication().getApplicationContext();
        this.saveStickerPackService = new SaveStickerPackService(this.context);
        this.convertMediaToStickerFormat = new ConvertMediaToStickerFormat(this.context);

        convertedFilesLiveData.observeForever(convertedStickerPackObserver);
    }

    private final Observer<List<File>> convertedStickerPackObserver = this::onChangedConverted;

    public LiveData<CallbackResult<StickerPack>> getStickerPackResult() {
        return stickerPackResult;
    }

    public LiveData<StickerPack> getStickerPackPreview() {
        return getPackPreview();
    }

    public void setStickerPackPreview(StickerPack stickerPack) {
        stickerPackPreview.setValue(stickerPack);
    }

    private MutableLiveData<StickerPack> getPackPreview() {
        return stickerPackPreview;
    }

    public LiveData<MimeTypesSupported> getMimeTypesSupported() {
        return mimeTypesSupported;
    }

    public void setMimeTypesSupported(MimeTypesSupported mimeTypes) {
        mimeTypesSupported.setValue(mimeTypes);
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

    public void startConversions(Set<Uri> uris) {
        if (uris == null || uris.isEmpty()) {
            postFailure("A lista de URIs está vazia ou nula.");
            return;
        }

        int cores = Runtime.getRuntime().availableProcessors();
        int maxThreads = Math.min(30, cores * 2);
        conversionExecutor = new ThreadPoolExecutor(cores, maxThreads, 1L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );

        totalConversions = uris.size();
        completedConversions.set(0);
        convertedFiles.clear();
        conversionFutures.clear();
        conversionsCancelled = false;

        for (Uri uri : uris) {
            Future<?> future = conversionExecutor.submit(() -> {
                try {
                    if (conversionsCancelled) {
                        return;
                    }

                    if (uri.getPath() == null) throw new Exception("URI sem caminho válido.");
                    String fileName = new File(uri.getPath()).getName();
                    convertedFiles.add(
                            convertMediaToStickerFormat.convertMediaToWebPAsyncFuture(uri, fileName)
                                    .get());

                    int done = completedConversions.incrementAndGet();
                    conversionProgress.postValue(
                            new ProgressState(totalConversions, done, done == totalConversions));

                    if (done == totalConversions) {
                        convertedFilesLiveData.postValue(new ArrayList<>(convertedFiles));
                    }
                } catch (MediaConversionException mediaConversionException) {
                    postFailure(mediaConversionException.getMessage());
                } catch (InterruptedException interruptedException) {
                    Log.e(getClass().getSimpleName(), "Erro de interrupção do processo: " + uri,
                            interruptedException
                    );
                } catch (Exception exception) {
                    Log.e(getClass().getSimpleName(), "Erro ao converter URI: " + uri, exception);
                    postFailure("Erro na conversão: " + exception.getMessage());
                }
            });

            conversionFutures.add(future);
        }
    }

    public void setCancelConversions() {
        conversionsCancelled = true;
        synchronized (conversionFutures) {
            for (Future<?> future : conversionFutures) {
                future.cancel(true);
            }
            conversionFutures.clear();
        }

        if (conversionExecutor != null) {
            conversionExecutor.shutdownNow();
            conversionExecutor = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        convertedFilesLiveData.removeObserver(convertedStickerPackObserver);

        if (generateStickerPack != null && !generateStickerPack.isShutdown()) {
            generateStickerPack.shutdownNow();
        }

        if (conversionExecutor != null && !conversionExecutor.isShutdown()) {
            conversionExecutor.shutdownNow();
        }
    }

    private void generateStickerPack(List<File> files) {
        generateStickerPack = Executors.newSingleThreadExecutor();

        try {
            Boolean animated = isAnimatedPack.getValue();
            String name = nameStickerPack.getValue();

            if (animated == null || name == null || name.isBlank()) {
                throw new IllegalArgumentException("Dados insuficientes para gerar o pacote.");
            }

            if (context == null) {
                throw new IllegalStateException("Contexto inválido para gerar o stickerPack.");
            }

            generateStickerPack.submit(() -> {
                try {
                    CallbackResult<StickerPack> result = saveStickerPackService.saveStickerPackAsync(
                            animated, files, name).get();

                    stickerPackResult.postValue(result);
                } catch (AppCoreStateException | ExecutionException | InterruptedException exception) {
                    postFailure(exception.getMessage());
                }
            });
        } catch (StickerPackSaveException exception) {
            Log.d(StickerPackCreationViewModel.class.getSimpleName(), "Erro na conversão",
                    exception
            );
            postFailure("Erro na conversão: " + exception.getMessage());
        }
    }

    private void postFailure(String message) {
        stickerPackResult.postValue(CallbackResult.failure(new Exception(message)));
    }

    private void onChangedConverted(List<File> files) {
        if (files != null) {
            generateStickerPack(files);
        }
    }
}

