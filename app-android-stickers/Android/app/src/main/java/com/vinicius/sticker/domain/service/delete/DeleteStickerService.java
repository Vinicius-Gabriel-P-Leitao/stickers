/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.service.delete;

import static com.vinicius.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;
import static com.vinicius.sticker.domain.data.database.repository.DeleteStickerPackRepo.deleteSticker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vinicius.sticker.core.exception.DeleteStickerException;
import com.vinicius.sticker.core.exception.StickerPackSaveException;
import com.vinicius.sticker.core.pattern.CallbackResult;
import com.vinicius.sticker.domain.data.database.repository.DeleteStickerPackRepo;

import java.io.File;
import java.sql.SQLException;

public class DeleteStickerService {
    private final static String TAG_LOG = DeleteStickerService.class.getSimpleName();

    public static CallbackResult<Boolean> deleteStickerByPack(
            @NonNull Context context, @NonNull String stickerPackIdentifier, @NonNull String fileName) {
        try {
            int deletedSticker = DeleteStickerPackRepo.deleteSticker(context, stickerPackIdentifier, fileName);
            CallbackResult<Boolean> deletedStickerFile = DeleteStickerAssetService.deleteStickerAsset(context, stickerPackIdentifier, fileName);

            if (deletedStickerFile.isSuccess()) {
                if (deletedSticker > 0 && deletedStickerFile.getData()) {
                    Log.i(TAG_LOG, "Figurinha deletado com sucesso");
                    return CallbackResult.success(Boolean.TRUE);
                } else {
                    return CallbackResult.warning("Nenhuma Figurinha deletado para fileName: " + fileName);
                }
            }

            return CallbackResult.failure(
                    deletedStickerFile.getError() != null ? deletedStickerFile.getError() : new DeleteStickerException("Erro ao deletar arquivo!"));

        } catch (SQLException exception) {
            return CallbackResult.failure(
                    new DeleteStickerException("Erro ao deletar métadados da figurinha no  banco de dados!", exception.getCause()));
        }
    }

    public static CallbackResult<Boolean> deleteAllStickerByPack(@NonNull Context context, @NonNull String stickerPackIdentifier) {
        CallbackResult<Integer> stickersDeletedInDb = DeleteStickerPackRepo.deleteAllStickerOfPack(context, stickerPackIdentifier);
        CallbackResult<Void> stickerFilesDeleted = DeleteStickerAssetService.deleteAllStickerAssetsInPack(context, stickerPackIdentifier);

        if (stickersDeletedInDb.isFailure()) {
            return CallbackResult.failure(
                    new DeleteStickerException("Falha ao limpar figurinhas do banco de dados.", stickersDeletedInDb.getError()));

        }

        return switch (stickerFilesDeleted.getStatus()) {
            case SUCCESS -> CallbackResult.debug("Figurinhas deletadas com sucesso.");
            case WARNING -> CallbackResult.warning(stickerFilesDeleted.getWarningMessage());
            case FAILURE -> CallbackResult.failure(stickerFilesDeleted.getError());
            default -> CallbackResult.debug("Figurinhas deletadas do banco com sucesso.");
        };
    }
}
