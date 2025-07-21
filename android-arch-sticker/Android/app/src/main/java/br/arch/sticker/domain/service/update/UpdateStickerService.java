/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */
package br.arch.sticker.domain.service.update;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.sticker.UpdateStickerException;
import br.arch.sticker.domain.data.database.StickerDatabaseHelper;
import br.arch.sticker.domain.data.database.repository.UpdateStickerRepo;

public class UpdateStickerService {
    private static final String TAG_LOG = UpdateStickerService.class.getSimpleName();

    private final UpdateStickerRepo updateStickerRepo;

    public UpdateStickerService(Context paramContext) {
        Context context = paramContext.getApplicationContext();
        SQLiteDatabase database = StickerDatabaseHelper.getInstance(context).getWritableDatabase();
        this.updateStickerRepo = new UpdateStickerRepo(database, context.getResources());
    }

    public boolean updateStickerFileName(String stickerPackIdentifier, String newFileName, String oldFileName)
            throws UpdateStickerException {
        if (stickerPackIdentifier.isEmpty() || newFileName.isEmpty() || oldFileName.isEmpty()) {
            Log.w(TAG_LOG, "Parâmetros inválidos para renomear figurinha. Algum campo está vazio.");
            return false;
        }

        Log.d(TAG_LOG,
                String.format("Atualizando nome de figurinha: pack='%s', old='%s', new='%s'", stickerPackIdentifier,
                        oldFileName, newFileName
                )
        );

        if (updateStickerRepo.updateStickerFileName(stickerPackIdentifier, newFileName, oldFileName)) {
            return true;
        }

        String message = "Atualização de nome de figurinha falhou ou não teve efeito.";
        Log.w(TAG_LOG, message);
        throw new UpdateStickerException(message, ErrorCode.ERROR_EMPTY_STICKERPACK);
    }

    public boolean updateInvalidSticker(String stickerPackIdentifier, String fileName, ErrorCode errorCode)
            throws UpdateStickerException {
        if (stickerPackIdentifier.isEmpty() || fileName.isEmpty() || errorCode == null) {
            Log.w(TAG_LOG, "Parâmetros inválidos para marcar figurinha inválida.");
            return false;
        }

        String errorName = (errorCode instanceof Enum<?>) ? ((Enum<?>) errorCode).name() : errorCode.toString();

        Log.d(TAG_LOG, String.format("Marcando figurinha como inválida: pack='%s', file='%s', erro='%s'",
                        stickerPackIdentifier, fileName, errorName
                )
        );

        boolean updated = updateStickerRepo.updateInvalidSticker(stickerPackIdentifier, fileName, errorName);

        if (!updated) {
            Log.w(TAG_LOG, "Falha ao marcar a figurinha como inválida.");
            throw new UpdateStickerException("Falha ao marcar a figurinha como inválida.",
                    ErrorCode.ERROR_EMPTY_STICKERPACK
            );
        }

        return true;
    }
}
