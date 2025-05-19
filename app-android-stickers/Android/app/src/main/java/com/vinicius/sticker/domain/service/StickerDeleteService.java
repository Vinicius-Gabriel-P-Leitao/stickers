package com.vinicius.sticker.domain.service;

import static com.vinicius.sticker.domain.data.content.provider.StickerContentProvider.STICKERS_ASSET;
import static com.vinicius.sticker.domain.data.database.repository.DeleteStickerPacks.deleteSticker;

import android.content.Context;
import android.util.Log;

import com.vinicius.sticker.domain.pattern.CallbackResult;

import java.io.File;
import java.sql.SQLException;

public class StickerDeleteService {
    // TODO: Fazer exceptions personalizadas.
    public static CallbackResult<Boolean> deleteStickerByIdentifier(Context context, String stickerPackIdentifier, String fileName) {
        if (stickerPackIdentifier == null || fileName == null) {
            return CallbackResult.failure(new IllegalArgumentException("stickerPackIdentifier e fileName não podem ser null"));
        }

        try {
            int deletedSticker = deleteSticker(context, stickerPackIdentifier, fileName);

            if (deletedSticker > 0 && deleteFileSticker(context, stickerPackIdentifier, fileName)) {
                Log.i("StickerDeleteService", "Sticker deletado com sucesso");
                return CallbackResult.success(Boolean.TRUE);
            } else {
                return CallbackResult.warning("Nenhum sticker deletado para fileName: " + fileName);
            }
        } catch (SQLException exception) {
            return CallbackResult.failure(exception);
        }

    }

    private static Boolean deleteFileSticker(Context context, String stickerPackIdentifier, String fileName) {
        File mainDirectory = new File(context.getFilesDir(), STICKERS_ASSET);
        File stickerDirectory = new File(mainDirectory, stickerPackIdentifier + File.separator + fileName);

        if (stickerDirectory.exists()) {
            boolean deleted = stickerDirectory.delete();

            if (deleted) {
                Log.i("StickerDelete", "Arquivo deletado: " + stickerDirectory.getAbsolutePath());
            } else {
                Log.e("StickerDelete", "Falha ao deletar arquivo: " + stickerDirectory.getAbsolutePath());
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
        } else {
            Log.w("StickerDelete", "Arquivo não encontrado para deletar: " + stickerDirectory.getAbsolutePath());
            return Boolean.FALSE;
        }
    }
}
