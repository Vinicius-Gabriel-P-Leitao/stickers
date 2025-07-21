package br.arch.sticker.domain.service.update;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.sticker.UpdateStickerException;
import br.arch.sticker.domain.data.database.StickerDatabaseHelper;
import br.arch.sticker.domain.data.database.repository.UpdateStickerPackRepo;

public class UpdateStickerPackService {
    private static final String TAG_LOG = UpdateStickerPackService.class.getSimpleName();

    private final UpdateStickerPackRepo updateStickerPackRepo;

    public UpdateStickerPackService(Context paramContext) {
        Context context = paramContext.getApplicationContext();
        SQLiteDatabase database = StickerDatabaseHelper.getInstance(context).getWritableDatabase();
        this.updateStickerPackRepo = new UpdateStickerPackRepo(database, context.getResources());
    }

    public boolean updateStickerFileName(String stickerPackIdentifier, String newName) throws UpdateStickerException {
        if (stickerPackIdentifier.isEmpty() || newName.isEmpty()) {
            Log.w(TAG_LOG, "Parâmetros inválidos para renomear pacote de figurinha. Algum campo está vazio.");
            return false;
        }

        Log.d(TAG_LOG, String.format("Atualizando nome do pacote de figurinhas: pack='%s', new='%s'", stickerPackIdentifier, newName));

        if (updateStickerPackRepo.updateStickerPackName(stickerPackIdentifier, newName)) {
            return true;
        }

        String message = "Falha ao atualizar o nome do pacote de figurinhas.";
        Log.w(TAG_LOG, message);
        throw new UpdateStickerException(message, ErrorCode.ERROR_EMPTY_STICKERPACK);
    }

    public boolean cleanStickerPackUrl(String stickerPackIdentifier) throws UpdateStickerException {
        if (stickerPackIdentifier.isEmpty()) {
            Log.w(TAG_LOG, "Parâmetros inválidos para limpar urls do pacote de figurinha. O campo de stickerPackIdentifier.");
            return false;
        }

        Log.d(TAG_LOG, String.format("Atualizando nome do pacote de figurinhas: pack='%s'", stickerPackIdentifier));

        if (updateStickerPackRepo.cleanStickerPackUrl(stickerPackIdentifier)) {
            return true;
        }

        String message = "Falha ao atualizar o nome do pacote de figurinhas.";
        Log.w(TAG_LOG, message);
        throw new UpdateStickerException(message, ErrorCode.ERROR_EMPTY_STICKERPACK);
    }
}
