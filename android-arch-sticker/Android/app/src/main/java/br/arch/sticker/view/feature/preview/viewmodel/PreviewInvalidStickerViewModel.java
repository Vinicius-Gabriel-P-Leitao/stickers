/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */
package br.arch.sticker.view.feature.preview.viewmodel;

import static br.arch.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;

import br.arch.sticker.core.error.ErrorCodeProvider;
import br.arch.sticker.core.error.code.StickerAssetErrorCode;
import br.arch.sticker.core.lib.NativeProcessWebp;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.service.delete.DeleteStickerAssetService;
import br.arch.sticker.domain.service.delete.DeleteStickerService;
import br.arch.sticker.domain.service.update.UpdateStickerService;
import br.arch.sticker.view.core.util.convert.ConvertMediaToStickerFormat;
import br.arch.sticker.view.core.util.event.GenericEvent;

public class PreviewInvalidStickerViewModel extends AndroidViewModel {
    private final static String TAG_LOG = PreviewInvalidStickerViewModel.class.getSimpleName();

    public sealed interface FixActionSticker permits FixActionSticker.Delete, FixActionSticker.ResizeFile {
        record Delete(Sticker sticker, String stickerPackIdentifier, ErrorCodeProvider codeProvider) implements FixActionSticker {
        }

        record ResizeFile(Sticker sticker, String stickerPackIdentifier, ErrorCodeProvider codeProvider) implements FixActionSticker {
        }
    }

    private final DeleteStickerAssetService deleteStickerAssetService;
    private final DeleteStickerService deleteStickerService;
    private final UpdateStickerService updateStickerService;
    private final Context context;

    private final MutableLiveData<GenericEvent<FixActionSticker>> stickerMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<FixActionSticker> fixCompletedLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public PreviewInvalidStickerViewModel(@NonNull Application application)
        {
            super(application);
            this.context = application.getApplicationContext();
            this.deleteStickerService = new DeleteStickerService(this.context);
            this.updateStickerService = new UpdateStickerService(this.context);
            this.deleteStickerAssetService = new DeleteStickerAssetService(this.context);
        }

    public LiveData<GenericEvent<FixActionSticker>> getStickerMutableLiveData()
        {
            return stickerMutableLiveData;
        }

    public LiveData<FixActionSticker> getFixCompletedLiveData()
        {
            return fixCompletedLiveData;
        }

    public LiveData<String> getErrorMessageLiveData()
        {
            return errorMessageLiveData;
        }

    public void handleFixStickerClick(Sticker sticker, String stickerPackIdentifier)
        {
            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.STICKER_FILE_NOT_EXIST.name())) {
                stickerMutableLiveData.setValue(new GenericEvent<>(
                        new FixActionSticker.Delete(sticker, stickerPackIdentifier, StickerAssetErrorCode.STICKER_FILE_NOT_EXIST)));
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.INVALID_STICKER_PATH.name())) {
                stickerMutableLiveData.setValue(new GenericEvent<>(
                        new FixActionSticker.Delete(sticker, stickerPackIdentifier, StickerAssetErrorCode.INVALID_STICKER_PATH)));
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_FILE_SIZE.name())) {
                stickerMutableLiveData.setValue(
                        new GenericEvent<>(new FixActionSticker.ResizeFile(sticker, stickerPackIdentifier, StickerAssetErrorCode.ERROR_FILE_SIZE)));
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_SIZE_STICKER.name())) {
                stickerMutableLiveData.setValue(
                        new GenericEvent<>(new FixActionSticker.ResizeFile(sticker, stickerPackIdentifier, StickerAssetErrorCode.ERROR_FILE_SIZE)));
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_STICKER_TYPE.name())) {
                stickerMutableLiveData.setValue(
                        new GenericEvent<>(new FixActionSticker.Delete(sticker, stickerPackIdentifier, StickerAssetErrorCode.ERROR_STICKER_TYPE)));
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_STICKER_DURATION.name())) {
                stickerMutableLiveData.setValue(new GenericEvent<>(
                        new FixActionSticker.Delete(sticker, stickerPackIdentifier, StickerAssetErrorCode.ERROR_STICKER_DURATION)));
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_FILE_TYPE.name())) {
                stickerMutableLiveData.setValue(
                        new GenericEvent<>(new FixActionSticker.Delete(sticker, stickerPackIdentifier, StickerAssetErrorCode.ERROR_FILE_TYPE)));
            }
        }

    public void onFixActionConfirmed(FixActionSticker action)
        {
            if (action instanceof FixActionSticker.Delete delete) {
                Sticker sticker = delete.sticker();
                String stickerPackIdentifier = delete.stickerPackIdentifier();

                new Thread(() -> {
                    try {
                        if (delete.codeProvider() != StickerAssetErrorCode.STICKER_FILE_NOT_EXIST) {
                            CallbackResult<Boolean> resultAsset = deleteStickerAssetService.deleteStickerAsset(stickerPackIdentifier,
                                    sticker.imageFileName);
                            if (resultAsset.isFailure()) {
                                errorMessageLiveData.postValue(resultAsset.getError().getMessage());
                                return;
                            } else if (resultAsset.isWarning()) {
                                errorMessageLiveData.postValue(resultAsset.getWarningMessage());
                            }
                        }

                        CallbackResult<Boolean> resultDB = deleteStickerService.deleteStickerByPack(stickerPackIdentifier, sticker.imageFileName);
                        if (resultDB.isFailure()) {
                            errorMessageLiveData.postValue(resultDB.getError().getMessage());
                            return;
                        } else if (resultDB.isWarning()) {
                            errorMessageLiveData.postValue(resultDB.getWarningMessage());
                        }

                        fixCompletedLiveData.postValue(action);
                    } catch (Exception exception) {
                        Log.e(TAG_LOG, "Exception: " + exception);
                        errorMessageLiveData.postValue("Erro inesperado: " + exception.getMessage());
                    }
                }).start();
            }

            if (action instanceof FixActionSticker.ResizeFile resizeFile) {
                Sticker sticker = resizeFile.sticker();
                String stickerPackIdentifier = resizeFile.stickerPackIdentifier();

                new Thread(() -> {
                    File filesDir = new File(new File(context.getFilesDir(), STICKERS_ASSET), stickerPackIdentifier);

                    String inputFile = new File(filesDir, sticker.imageFileName).getAbsolutePath();
                    String outputFile = new File(filesDir, sticker.imageFileName + "-resize").getAbsolutePath();

                    String finalOutputFileName = ConvertMediaToStickerFormat.ensureWebpExtension(outputFile);

                    NativeProcessWebp nativeProcessWebp = new NativeProcessWebp();
                    nativeProcessWebp.processWebpAsync(inputFile, finalOutputFileName,

                            new NativeProcessWebp.ConversionCallback() {
                                @Override
                                public void onSuccess(File file)
                                    {
                                        updateStickerService.updateStickerFileName(stickerPackIdentifier, file.getName(), sticker.imageFileName);
                                        fixCompletedLiveData.postValue(resizeFile);
                                    }

                                @Override
                                public void onError(Exception exception)
                                    {
                                        errorMessageLiveData.postValue(exception.getMessage());
                                    }
                            });
                }).start();
            }
        }
}
