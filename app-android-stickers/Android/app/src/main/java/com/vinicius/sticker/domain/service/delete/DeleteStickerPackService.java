/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.service.delete;

import android.content.Context;

import androidx.annotation.NonNull;

import com.vinicius.sticker.core.exception.sticker.DeleteStickerException;
import com.vinicius.sticker.core.pattern.CallbackResult;
import com.vinicius.sticker.domain.data.database.repository.DeleteStickerPackRepo;

// @formatter:off
public class DeleteStickerPackService {
    public static CallbackResult<Boolean> deleteStickerPack(@NonNull Context context, @NonNull String stickerPackIdentifier) {
        CallbackResult<Integer> stickerPackDeletedInDb = DeleteStickerPackRepo.deleteStickerPackFromDatabase(context, stickerPackIdentifier);
        return switch (stickerPackDeletedInDb.getStatus()) {
            case SUCCESS -> {
                yield CallbackResult.debug("Pacote de figurinhas deletado com sucesso. Status: " + stickerPackDeletedInDb.getData());
            }
            case WARNING -> {
                yield CallbackResult.warning(stickerPackDeletedInDb.getWarningMessage());
            }
            case DEBUG -> {
                yield CallbackResult.debug(stickerPackDeletedInDb.getDebugMessage());
            }
            case FAILURE -> {
                yield CallbackResult.failure(new DeleteStickerException("Falha ao limpar pacote de figurinhas do banco.", stickerPackDeletedInDb.getError()));
            }
        };
    }
}
