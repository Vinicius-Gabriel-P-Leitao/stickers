/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.preview.viewmodel;

import static br.arch.sticker.core.validation.StickerPackValidator.STICKER_SIZE_MAX;
import static br.arch.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;
import static br.arch.sticker.domain.util.StickerPackPlaceholder.PLACEHOLDER_ANIMATED;
import static br.arch.sticker.domain.util.StickerPackPlaceholder.PLACEHOLDER_STATIC;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCodeProvider;
import br.arch.sticker.core.error.code.FetchErrorCode;
import br.arch.sticker.core.error.code.InvalidUrlErrorCode;
import br.arch.sticker.core.error.code.StickerPackErrorCode;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.service.delete.DeleteStickerAssetService;
import br.arch.sticker.domain.service.delete.DeleteStickerPackService;
import br.arch.sticker.domain.service.update.UpdateStickerPackService;
import br.arch.sticker.view.core.util.convert.ConvertThumbnail;
import br.arch.sticker.view.core.util.event.GenericEvent;

public class PreviewInvalidStickerPackViewModel extends AndroidViewModel {
    private static final String TAG_LOG = PreviewInvalidStickerPackViewModel.class.getSimpleName();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final DeleteStickerAssetService deleteStickerAssetService;
    private final UpdateStickerPackService updateStickerPackService;
    private final DeleteStickerPackService deleteStickerPackService;
    private final Context context;

    public PreviewInvalidStickerPackViewModel(@NonNull Application application) {
        super(application);
        this.context = application.getApplicationContext();
        this.deleteStickerPackService = new DeleteStickerPackService(this.context);
        this.updateStickerPackService = new UpdateStickerPackService(this.context);
        this.deleteStickerAssetService = new DeleteStickerAssetService(this.context);
    }

    private final MutableLiveData<GenericEvent<FixActionStickerPack>> stickerpackMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<FixActionStickerPack> fixCompletedLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> progressLiveData = new MutableLiveData<>();

    public LiveData<GenericEvent<FixActionStickerPack>> getStickerMutableLiveData() {
        return stickerpackMutableLiveData;
    }

    public LiveData<Boolean> getProgressLiveData() {
        return progressLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public LiveData<FixActionStickerPack> getFixCompletedLiveData() {
        return fixCompletedLiveData;
    }

    public void handleFixStickerPackClick(StickerPack stickerPack, ErrorCodeProvider errorCode) {
        if (TextUtils.isEmpty(stickerPack.identifier)) return;

        FixActionStickerPack action = null;

        if (errorCode instanceof FetchErrorCode fetchError) {
            action = switch (fetchError) {
                case ERROR_EMPTY_STICKERPACK, ERROR_CONTENT_PROVIDER ->
                        new FixActionStickerPack.Delete(stickerPack);
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
                        new FixActionStickerPack.RenameStickerPack(stickerPack, null);
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
            stickerpackMutableLiveData.setValue(new GenericEvent<>(action));
        }
    }

    public void onFixActionConfirmed(FixActionStickerPack action) {
        progressLiveData.setValue(true);

        if (action instanceof FixActionStickerPack.Delete delete) {
            executor.submit(() -> {
                String stickerPackIdentifier = delete.stickerPack.identifier;

                CallbackResult<Boolean> resultAsset = deleteStickerAssetService.deleteAllStickerAssetsByPack(stickerPackIdentifier);
                if (resultAsset.isFailure()) {
                    errorMessageLiveData.postValue(resultAsset.getError().getMessage());
                    progressLiveData.postValue(false);
                    return;
                } else if (resultAsset.isWarning()) {
                    errorMessageLiveData.postValue(resultAsset.getWarningMessage());
                }

                CallbackResult<Boolean> resultDB = deleteStickerPackService.deleteStickerPack(stickerPackIdentifier);
                if (resultDB.isFailure()) {
                    errorMessageLiveData.postValue(resultDB.getError().getMessage());
                    progressLiveData.postValue(false);
                    return;
                } else if (resultDB.isWarning()) {
                    errorMessageLiveData.postValue(resultDB.getWarningMessage());
                }

                fixCompletedLiveData.postValue(delete);
                progressLiveData.postValue(false);
            });
        }

        if (action instanceof FixActionStickerPack.NewThumbnail newThumbnail) {
            executor.submit(() -> {
                Optional<String> stickerFileName = newThumbnail.stickerPack.getStickers().stream()
                        .map(sticker -> sticker.imageFileName)
                        .filter(name -> !PLACEHOLDER_ANIMATED.equals(name) &&
                                !PLACEHOLDER_STATIC.equals(name) && !name.isBlank()).findFirst();

                stickerFileName.ifPresentOrElse(name -> {
                    File filesDir = new File(new File(context.getFilesDir(), STICKERS_ASSET), newThumbnail.stickerPack.identifier);
                    File thumbnailSticker = new File(filesDir, name);

                    CallbackResult<Boolean> thumbnail = ConvertThumbnail.createThumbnail(thumbnailSticker, filesDir);

                    if (thumbnail.isFailure()) {
                        Log.e(TAG_LOG, "Error: " + thumbnail.getError());
                        errorMessageLiveData.postValue(thumbnail.getError().getMessage());
                        progressLiveData.postValue(false);
                        return;
                    }

                    if (thumbnail.isWarning()) {
                        Log.e(TAG_LOG, "Warning: " + thumbnail.getWarningMessage());
                        errorMessageLiveData.postValue(thumbnail.getWarningMessage());
                        progressLiveData.postValue(false);
                        return;
                    }

                    Log.e(TAG_LOG, "Success: " + thumbnail.getData());
                    fixCompletedLiveData.postValue(newThumbnail);
                    progressLiveData.postValue(false);
                }, () -> {
                    errorMessageLiveData.postValue(context.getString(R.string.error_message_file_to_make_thumbnail_not_exist));
                    progressLiveData.postValue(false);
                });
            });
        }

        if (action instanceof FixActionStickerPack.RenameStickerPack renameStickerPack) {
            if (renameStickerPack.newName == null) {
                errorMessageLiveData.postValue(context.getString(R.string.error_message_stickerpack_name_must_be_entered));
                progressLiveData.postValue(false);
                return;
            }

            String newNameStickerPack = renameStickerPack.newName;

            executor.submit(() -> {
                if (updateStickerPackService.updateStickerFileName(renameStickerPack.stickerPack.identifier, newNameStickerPack)) {
                    fixCompletedLiveData.postValue(renameStickerPack);
                    progressLiveData.postValue(false);
                    return;
                }

                errorMessageLiveData.postValue(context.getString(R.string.error_message_unable_to_update_stickerpack_name));
                progressLiveData.postValue(false);
            });
        }

        if (action instanceof FixActionStickerPack.ResizeStickerPack resizeStickerPack) {
            List<Sticker> stickerList = resizeStickerPack.stickerPack.getStickers();
            if (stickerList.isEmpty() || stickerList.size() <= STICKER_SIZE_MAX) {
                errorMessageLiveData.postValue(context.getString(R.string.error_message_list_sticker_in_pack_invalid_size));
                progressLiveData.postValue(false);
                return;
            }

            executor.submit(() -> {
                try {
                    String stickerPackIdentifier = resizeStickerPack.stickerPack.identifier;
                    List<Sticker> stickersToDelete = resizeStickerPack.stickerPack.getStickers();
                    List<String> subbedList = new ArrayList<>();

                    if (STICKER_SIZE_MAX < stickersToDelete.size()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            subbedList = stickersToDelete.subList(STICKER_SIZE_MAX, stickersToDelete.size())
                                    .stream().map(Sticker::getImageFileName)
                                    .collect(Collectors.toList());
                        } else {
                            for (int counter = STICKER_SIZE_MAX;
                                 counter < stickersToDelete.size(); counter++) {
                                subbedList.add(stickersToDelete.get(counter).getImageFileName());
                            }
                        }
                    }

                    CallbackResult<Boolean> resultAsset = deleteStickerAssetService.deleteListStickerAssetsByPack(stickerPackIdentifier, subbedList);
                    if (resultAsset.isFailure()) {
                        errorMessageLiveData.postValue(resultAsset.getError().getMessage());
                        progressLiveData.postValue(false);
                        return;
                    } else if (resultAsset.isWarning()) {
                        errorMessageLiveData.postValue(resultAsset.getWarningMessage());
                    }

                    CallbackResult<Boolean> deletedSticker = deleteStickerPackService.deleteSpareStickerPack(stickerPackIdentifier, subbedList);
                    if (deletedSticker.isFailure()) {
                        errorMessageLiveData.postValue(deletedSticker.getError().getMessage());
                        return;
                    } else if (deletedSticker.isWarning()) {
                        errorMessageLiveData.postValue(deletedSticker.getWarningMessage());
                    }

                    fixCompletedLiveData.postValue(resizeStickerPack);
                } catch (Exception exception) {
                    Log.e(TAG_LOG, "Exception: " + exception);
                    errorMessageLiveData.postValue(context.getString(R.string.throw_unknown_error) +
                            exception.getMessage());
                } finally {
                    progressLiveData.postValue(false);
                }
            });
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }

    public sealed interface FixActionStickerPack permits FixActionStickerPack.Delete, FixActionStickerPack.NewThumbnail, FixActionStickerPack.RenameStickerPack, FixActionStickerPack.ResizeStickerPack, FixActionStickerPack.CleanUpUrl, FixActionStickerPack.RefactorUrl {
        record Delete(StickerPack stickerPack) implements FixActionStickerPack {
        }

        record NewThumbnail(StickerPack stickerPack) implements FixActionStickerPack {
        }

        record RenameStickerPack(StickerPack stickerPack,
                                 String newName) implements FixActionStickerPack {
            public RenameStickerPack withNewName(@Nullable String newName) {
                return new RenameStickerPack(stickerPack, newName);
            }
        }

        record ResizeStickerPack(StickerPack stickerPack) implements FixActionStickerPack {
        }

        record CleanUpUrl(StickerPack stickerPack) implements FixActionStickerPack {
        }

        record RefactorUrl(StickerPack stickerPack) implements FixActionStickerPack {
        }
    }
}
