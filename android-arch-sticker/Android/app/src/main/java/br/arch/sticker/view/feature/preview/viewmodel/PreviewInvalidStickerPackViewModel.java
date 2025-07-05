/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.preview.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.arch.sticker.core.error.ErrorCodeProvider;
import br.arch.sticker.core.error.code.FetchErrorCode;
import br.arch.sticker.core.error.code.InvalidUrlErrorCode;
import br.arch.sticker.core.error.code.StickerPackErrorCode;
import br.arch.sticker.domain.data.model.StickerPack;

public class PreviewInvalidStickerPackViewModel extends AndroidViewModel {
    private static final String TAG_LOG = PreviewInvalidStickerPackViewModel.class.getSimpleName();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<FixActionStickerPack> stickerPackMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> progressLiveData = new MutableLiveData<>();

    public LiveData<Boolean> getProgressLiveData() {
        return progressLiveData;
    }

    public PreviewInvalidStickerPackViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<FixActionStickerPack> getStickerPackMutableLiveData() {
        return stickerPackMutableLiveData;
    }

    public void handleFixStickerPackClick(StickerPack stickerPack, ErrorCodeProvider errorCode) {
        if (TextUtils.isEmpty(stickerPack.identifier)) return;

        FixActionStickerPack action = null;

        if (errorCode instanceof FetchErrorCode fetchError) {
            action = switch (fetchError) {
                case ERROR_EMPTY_STICKERPACK, ERROR_CONTENT_PROVIDER ->
                        new FixActionStickerPack.NewThumbnail(stickerPack);
            };
        }

        if (errorCode instanceof InvalidUrlErrorCode urlError) {
            action = switch (urlError) {
                case INVALID_URL -> new FixActionStickerPack.RefactorUrl(stickerPack);
            };
        }

        if (errorCode instanceof StickerPackErrorCode packError) {
            action = switch (packError) {
                case INVALID_THUMBNAIL -> new FixActionStickerPack.NewThumbnail(stickerPack);
                case INVALID_STICKERPACK_NAME ->
                        new FixActionStickerPack.RenameStickerPack(stickerPack);
                case INVALID_STICKERPACK_SIZE ->
                        new FixActionStickerPack.ResizeStickerPack(stickerPack);
                case INVALID_IDENTIFIER, DUPLICATE_IDENTIFIER ->
                        new FixActionStickerPack.Delete(stickerPack);
                case INVALID_PUBLISHER, INVALID_IOS_URL_SITE, INVALID_ANDROID_URL_SITE,
                     INVALID_WEBSITE, INVALID_EMAIL, INVALID_STICKER_ACCESSIBILITY ->
                        new FixActionStickerPack.CleanUpUrl(stickerPack);
            };
        }

        if (action != null) {
            stickerPackMutableLiveData.setValue(action);
        }
    }

    public void onFixActionConfirmed(FixActionStickerPack action) {
        progressLiveData.setValue(true);

    }

    @Override
    protected void onCleared() {
        super.onCleared(); executor.shutdownNow();
    }

    public sealed interface FixActionStickerPack permits FixActionStickerPack.Delete,
            FixActionStickerPack.NewThumbnail, FixActionStickerPack.RenameStickerPack,
            FixActionStickerPack.ResizeStickerPack, FixActionStickerPack.CleanUpUrl, FixActionStickerPack.RefactorUrl {
        record Delete(StickerPack stickerPack) implements FixActionStickerPack {}

        record NewThumbnail(StickerPack stickerPack) implements FixActionStickerPack {}

        record RenameStickerPack(StickerPack stickerPack) implements FixActionStickerPack {}

        record ResizeStickerPack(StickerPack stickerPack) implements FixActionStickerPack {}

        record CleanUpUrl(StickerPack stickerPack) implements FixActionStickerPack {}

        record RefactorUrl(StickerPack stickerPack) implements FixActionStickerPack {}
    }
}
