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

import androidx.lifecycle.ViewModel;

import br.arch.sticker.core.error.code.StickerAssetErrorCode;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;

public class PreviewInvalidStickerViewModel extends ViewModel {
    public void handleFixStickerClick(Sticker sticker) {
        if (TextUtils.equals(
                sticker.stickerIsValid,
                StickerAssetErrorCode.ERROR_FILE_SIZE.name())) {
            Log.d("ViewModel", "Botão Fix clicado para o sticker: " + sticker.imageFileName);
        }

        if (TextUtils.equals(
                sticker.stickerIsValid,
                StickerAssetErrorCode.ERROR_SIZE_STICKER.name())) {
            Log.d("ViewModel", "Botão Fix clicado para o sticker: " + sticker.imageFileName);
        }

        if (TextUtils.equals(
                sticker.stickerIsValid,
                StickerAssetErrorCode.ERROR_STICKER_TYPE.name())) {
            Log.d("ViewModel", "Botão Fix clicado para o sticker: " + sticker.imageFileName);
        }

        if (TextUtils.equals(
                sticker.stickerIsValid,
                StickerAssetErrorCode.ERROR_STICKER_DURATION.name())) {
            Log.d("ViewModel", "Botão Fix clicado para o sticker: " + sticker.imageFileName);
        }

        if (TextUtils.equals(
                sticker.stickerIsValid,
                StickerAssetErrorCode.ERROR_FILE_TYPE.name())) {
            Log.d("ViewModel", "Botão Fix clicado para o sticker: " + sticker.imageFileName);
        }
    }

    public void handleFixStickerPackClick(StickerPack stickerPack, Throwable exception) {
        Log.d("ViewModel", "Botão Fix clicado para o sticker: " + stickerPack.identifier);
    }
}
