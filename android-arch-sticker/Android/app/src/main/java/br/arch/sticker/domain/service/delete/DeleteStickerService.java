/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.delete;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.sql.SQLException;

import br.arch.sticker.core.error.code.DeleteErrorCode;
import br.arch.sticker.core.error.throwable.sticker.DeleteStickerException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.database.repository.DeleteStickerPackRepo;

public class DeleteStickerService {
    private final static String TAG_LOG = DeleteStickerService.class.getSimpleName();

    public static CallbackResult<Boolean> deleteStickerByPack(
            @NonNull Context context, @NonNull String stickerPackIdentifier, @NonNull String fileName)
        {
            try
            {
                int deletedSticker = DeleteStickerPackRepo.deleteSticker(context, stickerPackIdentifier, fileName);
                CallbackResult<Boolean> deletedStickerFile = DeleteStickerAssetService.deleteStickerAsset(context, stickerPackIdentifier, fileName);

                if (deletedStickerFile.isSuccess())
                {
                    if (deletedSticker > 0 && deletedStickerFile.getData())
                    {
                        Log.i(TAG_LOG, "Figurinha deletado com sucesso");
                        return CallbackResult.success(Boolean.TRUE);
                    } else
                    {
                        return CallbackResult.warning("Nenhuma Figurinha deletado para fileName: " + fileName);
                    }
                }

                return CallbackResult.failure(deletedStickerFile.getError() != null
                                              ? deletedStickerFile.getError()
                                              : new DeleteStickerException("Erro ao deletar arquivo!", DeleteErrorCode.ERROR_PACK_DELETE_SERVICE));

            } catch (SQLException exception)
            {
                return CallbackResult.failure(
                        new DeleteStickerException(
                                "Erro ao deletar métadados da figurinha no  banco de dados!", exception.getCause(),
                                DeleteErrorCode.ERROR_PACK_DELETE_SERVICE));
            }
        }

    public static CallbackResult<Boolean> deleteAllStickerByPack(@NonNull Context context, @NonNull String stickerPackIdentifier)
        {
            try
            {
                CallbackResult<Boolean> stickerAssetDeleted = DeleteStickerAssetService.deleteAllStickerAssetsByPack(context, stickerPackIdentifier);
                if (stickerAssetDeleted.getStatus() == CallbackResult.Status.FAILURE)
                {
                    return CallbackResult.failure(stickerAssetDeleted.getError());
                }

                CallbackResult<Integer> stickerDeletedInDb = DeleteStickerPackRepo.deleteAllStickerOfPack(context, stickerPackIdentifier);
                if (stickerDeletedInDb.getStatus() == CallbackResult.Status.FAILURE)
                {
                    return CallbackResult.failure(stickerDeletedInDb.getError());
                }

                boolean assetsDeleted = stickerAssetDeleted.getStatus() == CallbackResult.Status.SUCCESS;
                boolean dbDeleted = stickerDeletedInDb.getStatus() == CallbackResult.Status.SUCCESS;

                if (!assetsDeleted && !dbDeleted)
                {
                    return CallbackResult.warning("Nenhuma figurinha foi deletada: diretório e registros não encontrados.");
                } else if (!assetsDeleted)
                {
                    return CallbackResult.warning("Figurinhas não encontradas no armazenamento, mas registros foram deletados.");
                } else if (!dbDeleted)
                {
                    return CallbackResult.warning("Registros não encontrados no banco de dados, mas arquivos foram deletados.");
                }

                return CallbackResult.success(true);
            } catch (Exception exception)
            {
                return CallbackResult.failure(new Exception("Erro ao deletar figurinhas: " + exception.getMessage(), exception));
            }
        }
}
