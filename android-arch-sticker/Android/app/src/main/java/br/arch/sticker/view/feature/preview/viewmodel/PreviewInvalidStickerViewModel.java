/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.preview.viewmodel;

import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import br.arch.sticker.core.error.code.StickerAssetErrorCode;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.view.core.util.event.GenericEvent;
import br.arch.sticker.view.feature.preview.activity.PreviewInvalidStickerActivity;

public class PreviewInvalidStickerViewModel extends ViewModel {
    private final static String TAG_LOG = PreviewInvalidStickerActivity.class.getSimpleName();

    public sealed interface FixActionSticker permits FixActionSticker.Delete {
        record Delete(Sticker sticker, int resourceString) implements FixActionSticker {
        }
    }

    private final MutableLiveData<GenericEvent<FixActionSticker>> stickerMutableLiveData = new MutableLiveData<>();

    public LiveData<GenericEvent<FixActionSticker>> getStickerMutableLiveData()
        {
            return stickerMutableLiveData;
        }

    public void handleFixStickerClick(Sticker sticker)
        {
            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.STICKER_FILE_NOT_EXIST.name())) {
                stickerMutableLiveData.setValue(
                        new GenericEvent<>(new FixActionSticker.Delete(sticker, StickerAssetErrorCode.STICKER_FILE_NOT_EXIST.getMessageResId())));
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.INVALID_STICKER_FILENAME.name())) {
                stickerMutableLiveData.setValue(
                        new GenericEvent<>(new FixActionSticker.Delete(sticker, StickerAssetErrorCode.INVALID_STICKER_FILENAME.getMessageResId())));
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_FILE_SIZE.name())) {
                stickerMutableLiveData.setValue(
                        new GenericEvent<>(new FixActionSticker.Delete(sticker, StickerAssetErrorCode.ERROR_FILE_SIZE.getMessageResId())));
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_SIZE_STICKER.name())) {
                stickerMutableLiveData.setValue(
                        new GenericEvent<>(new FixActionSticker.Delete(sticker, StickerAssetErrorCode.ERROR_SIZE_STICKER.getMessageResId())));
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_STICKER_TYPE.name())) {
                stickerMutableLiveData.setValue(
                        new GenericEvent<>(new FixActionSticker.Delete(sticker, StickerAssetErrorCode.ERROR_STICKER_TYPE.getMessageResId())));
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_STICKER_DURATION.name())) {
                stickerMutableLiveData.setValue(
                        new GenericEvent<>(new FixActionSticker.Delete(sticker, StickerAssetErrorCode.ERROR_STICKER_DURATION.getMessageResId())));
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_FILE_TYPE.name())) {
                stickerMutableLiveData.setValue(
                        new GenericEvent<>(new FixActionSticker.Delete(sticker, StickerAssetErrorCode.ERROR_FILE_TYPE.getMessageResId())));
            }
        }

    public void onFixActionConfirmed(FixActionSticker action, Sticker sticker)
        {
            if (action instanceof FixActionSticker.Delete delete) {
                Log.w(TAG_LOG, sticker.imageFileName);
            }
        }
}
