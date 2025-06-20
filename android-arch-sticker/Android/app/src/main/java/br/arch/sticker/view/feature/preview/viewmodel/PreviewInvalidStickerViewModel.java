/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.preview.viewmodel;

import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import br.arch.sticker.core.error.code.StickerAssetErrorCode;
import br.arch.sticker.domain.data.model.Sticker;

public class PreviewInvalidStickerViewModel extends ViewModel {
    private final MutableLiveData<FixActionSticker> stickerMutableLiveData = new MutableLiveData<>();
    public LiveData<FixActionSticker> getStickerMutableLiveData()
        {
            return stickerMutableLiveData;
        }


    public void handleFixStickerClick(Sticker sticker)
        {
            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.STICKER_FILE_NOT_EXIST.name())) {
                // deletar sticker do banco
                stickerMutableLiveData.setValue(new FixActionSticker.Delete(sticker));
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.INVALID_STICKER_FILENAME.name())) {
                // Caso o nome do arquivo seja invalido, deletar
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_FILE_SIZE.name())) {
                // Tamanho em kv passou do permitido tentar rodar um script que vai tentar diminuir arquivo
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_SIZE_STICKER.name())) {
                // chamar método a ser feito que vai tentar fazer um resize forçado para 512x512
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_STICKER_TYPE.name())) {
                // deletar sticker provavelmente é um statico em pacote animado
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_STICKER_DURATION.name())) {
                // Tempo da animção muito grande, quase impossivel de acontecer porem pode ocorrer
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_FILE_TYPE.name())) {
                // O arquivo é diferente de .webp
            }
        }

    public sealed interface FixActionSticker permits FixActionSticker.Delete {
        record Delete(Sticker sticker) implements FixActionSticker {
        }
    }
}
