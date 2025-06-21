/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.preview.viewmodel;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import br.arch.sticker.core.error.ErrorCodeProvider;
import br.arch.sticker.core.error.code.StickerAssetErrorCode;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.service.delete.DeleteStickerAssetService;
import br.arch.sticker.domain.service.delete.DeleteStickerService;
import br.arch.sticker.view.core.util.event.GenericEvent;

public class PreviewInvalidStickerViewModel extends ViewModel {
    public sealed interface FixActionSticker permits FixActionSticker.Delete {
        record Delete(Sticker sticker, String stickerPackIdentifier, ErrorCodeProvider codeProvider) implements FixActionSticker {
        }
    }

    private final MutableLiveData<GenericEvent<FixActionSticker>> stickerMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<FixActionSticker> fixCompletedLiveData = new MutableLiveData<>();

    public LiveData<GenericEvent<FixActionSticker>> getStickerMutableLiveData()
        {
            return stickerMutableLiveData;
        }

    public LiveData<FixActionSticker> getFixCompletedLiveData()
        {
            return fixCompletedLiveData;
        }

    public void handleFixStickerClick(Sticker sticker, String stickerPackIdentifier)
        {
            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.STICKER_FILE_NOT_EXIST.name())) {
                stickerMutableLiveData.setValue(new GenericEvent<>(
                        new FixActionSticker.Delete(sticker, stickerPackIdentifier, StickerAssetErrorCode.STICKER_FILE_NOT_EXIST)));
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.INVALID_STICKER_FILENAME.name())) {
                stickerMutableLiveData.setValue(new GenericEvent<>(
                        new FixActionSticker.Delete(sticker, stickerPackIdentifier, StickerAssetErrorCode.INVALID_STICKER_FILENAME)));
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_FILE_SIZE.name())) {
                stickerMutableLiveData.setValue(
                        new GenericEvent<>(new FixActionSticker.Delete(sticker, stickerPackIdentifier, StickerAssetErrorCode.ERROR_FILE_SIZE)));
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_SIZE_STICKER.name())) {
                stickerMutableLiveData.setValue(
                        new GenericEvent<>(new FixActionSticker.Delete(sticker, stickerPackIdentifier, StickerAssetErrorCode.ERROR_SIZE_STICKER)));
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

    public void onFixActionConfirmed(FixActionSticker action, Context context, Sticker sticker, String stickerPackIdentifier)
        {
            // FIXME: Validar do por que diabos ele deleta o arquivo quando não é STICKER_FILE_NOT_EXIST porem simplesmente não deleta no banco de
            //  dados!
            if (action instanceof FixActionSticker.Delete delete) {
                if (delete.codeProvider != StickerAssetErrorCode.STICKER_FILE_NOT_EXIST) {
                    CallbackResult<Boolean> deletedStickerAsset = DeleteStickerAssetService.deleteStickerAsset(
                            context, stickerPackIdentifier, sticker.imageFileName);

                    switch (deletedStickerAsset.getStatus()) {
                        case WARNING:
                            Toast.makeText(context, deletedStickerAsset.getWarningMessage(), Toast.LENGTH_LONG).show();
                            break;
                        case FAILURE:
                            Toast.makeText(context, deletedStickerAsset.getError().getMessage(), Toast.LENGTH_LONG).show();
                            break;
                    }
                }

                CallbackResult<Boolean> deletedSticker = DeleteStickerService.deleteStickerByPack(
                        context, stickerPackIdentifier, sticker.imageFileName);

                switch (deletedSticker.getStatus()) {
                    case WARNING:
                        Toast.makeText(context, deletedSticker.getWarningMessage(), Toast.LENGTH_LONG).show();
                        break;
                    case FAILURE:
                        Toast.makeText(context, deletedSticker.getError().getMessage(), Toast.LENGTH_LONG).show();
                        break;
                }
            }

            fixCompletedLiveData.setValue(action);
        }
}
