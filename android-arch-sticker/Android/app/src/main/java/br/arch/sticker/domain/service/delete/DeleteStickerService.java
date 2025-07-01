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

    private final DeleteStickerPackRepo deleteStickerPackRepo;

    public DeleteStickerService(Context paramContext)
        {
            Context context = paramContext.getApplicationContext();
            this.deleteStickerPackRepo = new DeleteStickerPackRepo(context);
        }

    public CallbackResult<Boolean> deleteStickerByPack(@NonNull String stickerPackIdentifier, @NonNull String fileName)
        {
            try {
                int deletedSticker = deleteStickerPackRepo.deleteSticker(stickerPackIdentifier, fileName);

                if (deletedSticker > 0) {
                    Log.i(TAG_LOG, "Figurinha deletado com sucesso");
                    return CallbackResult.success(Boolean.TRUE);
                } else {
                    return CallbackResult.warning("Nenhuma Figurinha deletada para fileName: " + fileName);
                }
            } catch (SQLException exception) {
                return CallbackResult.failure(
                        new DeleteStickerException("Erro ao deletar métadados da figurinha no  banco de dados!", exception.getCause(),
                                DeleteErrorCode.ERROR_PACK_DELETE_SERVICE));
            }
        }

    public CallbackResult<Boolean> deleteAllStickerByPack(@NonNull String stickerPackIdentifier)
        {
            try {
                CallbackResult<Integer> stickerDeletedInDb = deleteStickerPackRepo.deleteAllStickerOfPack(stickerPackIdentifier);
                if (stickerDeletedInDb.getStatus() == CallbackResult.Status.FAILURE) {
                    return CallbackResult.failure(stickerDeletedInDb.getError());
                }

                boolean dbDeleted = stickerDeletedInDb.getStatus() == CallbackResult.Status.SUCCESS;

                if (!dbDeleted) {
                    return CallbackResult.failure(new DeleteStickerException("Nenhuma figurinha foi deletada: registros não encontrados.",
                            DeleteErrorCode.ERROR_PACK_DELETE_SERVICE));
                }

                return CallbackResult.success(true);
            } catch (Exception exception) {
                return CallbackResult.failure(
                        new DeleteStickerException(String.format("Erro ao deletar figurinhas: %s", exception.getMessage()), exception,
                                DeleteErrorCode.ERROR_PACK_DELETE_SERVICE));
            }
        }
}
