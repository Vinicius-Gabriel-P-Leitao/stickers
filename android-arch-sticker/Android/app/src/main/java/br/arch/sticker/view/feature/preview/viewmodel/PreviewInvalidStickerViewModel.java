/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */
package br.arch.sticker.view.feature.preview.viewmodel;

import static br.arch.sticker.core.error.ErrorCode.STICKER_FILE_NOT_EXIST;
import static br.arch.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;

import android.app.Application;
import android.content.Context;
import android.media.Image;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerPackException;
import br.arch.sticker.core.error.throwable.sticker.StickerFileException;
import br.arch.sticker.core.lib.NativeProcessWebp;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.core.validation.StickerValidator;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.service.delete.DeleteStickerAssetService;
import br.arch.sticker.domain.service.delete.DeleteStickerService;
import br.arch.sticker.domain.service.fetch.FetchStickerPackService;
import br.arch.sticker.domain.service.update.UpdateStickerService;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;
import br.arch.sticker.view.core.util.convert.ConvertMediaToStickerFormat;
import br.arch.sticker.view.core.util.convert.ImageConverter;
import br.arch.sticker.view.core.util.event.GenericEvent;

public class PreviewInvalidStickerViewModel extends AndroidViewModel {
    private static final String TAG_LOG = PreviewInvalidStickerViewModel.class.getSimpleName();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final DeleteStickerAssetService deleteStickerAssetService;
    private final FetchStickerPackService fetchStickerPackService;
    private final DeleteStickerService deleteStickerService;
    private final ApplicationTranslate applicationTranslate;
    private final UpdateStickerService updateStickerService;
    private final StickerValidator stickerValidator;
    private final ImageConverter imageConverter;
    private final Context context;

    private final MutableLiveData<GenericEvent<FixActionSticker>> stickerMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<FixActionSticker> fixCompletedLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> progressLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public PreviewInvalidStickerViewModel(@NonNull Application application) {
        super(application);
        this.context = application.getApplicationContext();
        this.applicationTranslate = new ApplicationTranslate(this.context.getResources());

        this.imageConverter = new ImageConverter(this.context);
        this.stickerValidator = new StickerValidator(this.context);
        this.deleteStickerService = new DeleteStickerService(this.context);
        this.updateStickerService = new UpdateStickerService(this.context);
        this.fetchStickerPackService = new FetchStickerPackService(this.context);
        this.deleteStickerAssetService = new DeleteStickerAssetService(this.context);
    }

    public LiveData<GenericEvent<FixActionSticker>> getStickerMutableLiveData() {
        return stickerMutableLiveData;
    }

    public LiveData<FixActionSticker> getFixCompletedLiveData() {
        return fixCompletedLiveData;
    }

    public LiveData<Boolean> getProgressLiveData() {
        return progressLiveData;
    }

    public LiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public void handleFixStickerClick(Sticker sticker, String stickerPackIdentifier) {
        if (TextUtils.isEmpty(sticker.stickerIsValid) || TextUtils.isEmpty(stickerPackIdentifier)) return;

        try {
            StickerPack stickerPack = fetchStickerPackService.fetchStickerPackFromContentProvider(stickerPackIdentifier)
                    .stickerPack();

            ErrorCode errorCode = ErrorCode.valueOf(sticker.stickerIsValid);

            FixActionSticker action = switch (errorCode) {
                case STICKER_FILE_NOT_EXIST, INVALID_STICKER_PATH, ERROR_STICKER_TYPE, ERROR_STICKER_DURATION,
                     ERROR_FILE_TYPE -> new FixActionSticker.Delete(sticker, stickerPackIdentifier, errorCode);

                case ERROR_FILE_SIZE, ERROR_SIZE_STICKER ->
                        new FixActionSticker.ResizeFile(sticker, stickerPackIdentifier, stickerPack.animatedStickerPack,
                                null, errorCode);

                default -> throw new IllegalStateException("Unexpected value: " + errorCode);
            };

            stickerMutableLiveData.setValue(new GenericEvent<>(action));
        } catch (IllegalArgumentException argumentException) {
            errorMessageLiveData.postValue(
                    applicationTranslate.translate(R.string.error_unknown_code, sticker.stickerIsValid)
                            .log(TAG_LOG, Level.ERROR, argumentException).get());
        } catch (FetchStickerPackException exception) {
            errorMessageLiveData.postValue(context.getString(exception.getErrorCode().getMessageResId()));
        }
    }

    public void onFixActionConfirmed(FixActionSticker action) {
        progressLiveData.setValue(true);

        if (action instanceof FixActionSticker.Delete delete) {
            Sticker sticker = delete.sticker();
            String stickerPackIdentifier = delete.stickerPackIdentifier();

            executor.submit(() -> {
                try {
                    if (delete.codeProvider() != STICKER_FILE_NOT_EXIST) {
                        CallbackResult<Boolean> resultAsset = deleteStickerAssetService.deleteStickerAsset(
                                stickerPackIdentifier, sticker.imageFileName);
                        if (resultAsset.isFailure()) {
                            errorMessageLiveData.postValue(resultAsset.getError().getMessage());
                            return;
                        } else if (resultAsset.isWarning()) {
                            errorMessageLiveData.postValue(resultAsset.getWarningMessage());
                        }
                    }

                    CallbackResult<Boolean> resultDB = deleteStickerService.deleteStickerByPack(stickerPackIdentifier,
                            sticker.imageFileName);
                    if (resultDB.isFailure()) {
                        errorMessageLiveData.postValue(resultDB.getError().getMessage());
                        return;
                    } else if (resultDB.isWarning()) {
                        errorMessageLiveData.postValue(resultDB.getWarningMessage());
                    }

                    fixCompletedLiveData.postValue(delete);
                } catch (Exception exception) {
                    errorMessageLiveData.postValue(
                            applicationTranslate.translate(R.string.error_unknown).log(TAG_LOG, Level.ERROR, exception)
                                    .get());
                } finally {
                    progressLiveData.postValue(false);
                }
            });
        }

        if (action instanceof FixActionSticker.ResizeFile resizeFile) {
            if (resizeFile.quality == null) {
                errorMessageLiveData.postValue(
                        applicationTranslate.translate(R.string.error_quality_not_entered).log(TAG_LOG, Level.ERROR)
                                .get());

                progressLiveData.postValue(false);
                return;
            }

            Sticker sticker = resizeFile.sticker();
            String stickerPackIdentifier = resizeFile.stickerPackIdentifier();
            float fileQuality = resizeFile.quality.floatValue();

            executor.submit(() -> {
                File filesDir = new File(new File(context.getFilesDir(), STICKERS_ASSET), stickerPackIdentifier);
                String inputFile = new File(filesDir, sticker.imageFileName).getAbsolutePath();

                String cleanName = sticker.imageFileName;
                if (cleanName.startsWith("resize-")) {
                    cleanName = cleanName.substring("resize-".length());
                }

                String finalOutputFileName = ConvertMediaToStickerFormat.ensureWebpExtension(
                        new File(filesDir, "resize-" + cleanName).getAbsolutePath());
                File outputFile = new File(finalOutputFileName);

                if (!resizeFile.animatedStickerPack) {
                    Log.d(TAG_LOG, "Input: " + inputFile + " Output: " + outputFile.getAbsolutePath());
                    File fileConverted = imageConverter.convertImageToWebp(inputFile, outputFile, (int) fileQuality);

                    if (fileConverted.exists()) {
                        boolean updated = updateStickerService.updateStickerFileName(stickerPackIdentifier,
                                outputFile.getName(), sticker.imageFileName);
                        if (!updated) {
                            errorMessageLiveData.postValue(
                                    applicationTranslate.translate(R.string.error_unable_update_sticker)
                                            .log(TAG_LOG, Level.ERROR).get());
                            progressLiveData.postValue(false);
                            return;
                        }

                        progressLiveData.postValue(false);
                        fixCompletedLiveData.postValue(resizeFile);
                        return;
                    }

                    errorMessageLiveData.postValue(
                            applicationTranslate.translate(R.string.error_conversion_failed).log(TAG_LOG, Level.ERROR)
                                    .get());
                    progressLiveData.postValue(false);
                    return;
                }

                NativeProcessWebp nativeProcessWebp = new NativeProcessWebp(context.getResources());
                nativeProcessWebp.processWebpAsync(inputFile, finalOutputFileName, fileQuality, false,
                        new NativeProcessWebp.ConversionCallback() {
                            @Override
                            public void onSuccess(File file) {
                                boolean updated = updateStickerService.updateStickerFileName(stickerPackIdentifier,
                                        file.getName(), sticker.imageFileName);

                                if (!updated) {
                                    errorMessageLiveData.postValue(
                                            applicationTranslate.translate(R.string.error_unable_update_sticker)
                                                    .log(TAG_LOG, Level.ERROR).get());
                                    progressLiveData.postValue(false);
                                    return;
                                }

                                try {
                                    stickerValidator.validateStickerFile(stickerPackIdentifier, file.getName(),
                                            resizeFile.animatedStickerPack);
                                } catch (StickerFileException exceptionFactory) {
                                    errorMessageLiveData.postValue(
                                            context.getString(exceptionFactory.getErrorCode().getMessageResId()));
                                    progressLiveData.postValue(false);
                                    return;
                                }

                                fixCompletedLiveData.postValue(resizeFile);
                                progressLiveData.postValue(false);
                            }

                            @Override
                            public void onError(Exception exception) {
                                errorMessageLiveData.postValue(exception.getMessage());
                                progressLiveData.postValue(false);
                            }
                        });
            });
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }

    public sealed interface FixActionSticker permits FixActionSticker.Delete, FixActionSticker.ResizeFile {
        record Delete(Sticker sticker, String stickerPackIdentifier,
                      ErrorCode codeProvider) implements FixActionSticker {
        }

        record ResizeFile(Sticker sticker, String stickerPackIdentifier, boolean animatedStickerPack, Integer quality,
                          ErrorCode codeProvider) implements FixActionSticker {
            public ResizeFile withQuality(@Nullable Integer newQuality) {
                return new ResizeFile(sticker, stickerPackIdentifier, animatedStickerPack, newQuality, codeProvider);
            }
        }
    }
}
