/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.save;

import static br.arch.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.Objects;

import br.arch.sticker.core.exception.throwable.base.InternalAppException;
import br.arch.sticker.core.exception.throwable.content.InvalidWebsiteUrlException;
import br.arch.sticker.core.exception.throwable.sticker.PackValidatorException;
import br.arch.sticker.core.exception.throwable.sticker.StickerFileException;
import br.arch.sticker.core.exception.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.exception.throwable.sticker.StickerValidatorException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.core.validation.StickerPackValidator;
import br.arch.sticker.core.validation.StickerValidator;
import br.arch.sticker.domain.data.database.StickerDatabase;
import br.arch.sticker.domain.data.database.repository.InsertStickerPackRepo;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.util.StickerPackDirectory;

// @formatter:off
public class SaveStickerPackService {
    private final static String TAG_LOG = SaveStickerPackService.class.getSimpleName();

    @FunctionalInterface
    public interface SaveStickerPackCallback {
        void onStickerPackSaveResult(CallbackResult<StickerPack> callbackResult);
    }

    public static void saveStickerPack(
            Context context, StickerPack stickerPack,
            SaveStickerPackCallback callback
    ) {
        File mainDirectory = new File(context.getFilesDir(), STICKERS_ASSET);
        if (!StickerPackDirectory.createMainDirectory(mainDirectory, callback)) {
            callback.onStickerPackSaveResult(CallbackResult.failure(new Exception("Erro ao criar pacote principal de figurinhas")));
            return;
        }

        File stickerPackDirectory = StickerPackDirectory.createStickerPackDirectory(mainDirectory, stickerPack.identifier, callback);
        if (stickerPackDirectory == null) {
            return;
        }

        if (!SaveStickerAssetService.copyStickerFromCache(context, stickerPack, stickerPackDirectory, callback)) {
            callback.onStickerPackSaveResult(CallbackResult.failure(new Exception("Erro ao copiar os figurinhas")));
            return;
        }

        try {
            StickerPackValidator.verifyStickerPackValidity(context, stickerPack);
        }  catch (PackValidatorException | StickerValidatorException | InvalidWebsiteUrlException appCoreStateException) {
            callback.onStickerPackSaveResult(CallbackResult.failure(appCoreStateException));
        }

        for (Sticker sticker : stickerPack.getStickers()) {
            try{
                StickerValidator.verifyStickerValidity(context, stickerPack.identifier, sticker, stickerPack.animatedStickerPack);
            } catch (StickerFileException stickerFileException) {
                if (Objects.equals(sticker.imageFileName, stickerFileException.getFileName())) {
                    sticker.setStickerIsInvalid(stickerFileException.getErrorCode());
                    callback.onStickerPackSaveResult(
                            CallbackResult.warning("Alguns stickers foram marcados como inválidos: " + stickerFileException.getFileName()));
                }
            } catch (StickerValidatorException stickerValidatorException) {
                callback.onStickerPackSaveResult(CallbackResult.failure(stickerValidatorException));
            }
        }

        insertStickerPack(context, stickerPack, callback);
    }

    private static void insertStickerPack(Context context, StickerPack stickerPack, SaveStickerPackCallback callback) {
        StickerDatabase instance = StickerDatabase.getInstance(context);
        SQLiteDatabase writableDatabase = instance.getWritableDatabase();

        new InsertStickerPackRepo().insertStickerPack(
                writableDatabase, stickerPack, callbackResult -> {
                    switch (callbackResult.getStatus()) {
                        case SUCCESS:
                            callback.onStickerPackSaveResult(CallbackResult.success(callbackResult.getData())); // NOTE: Somente esse deve retornar os dados.
                            break;
                        case WARNING:
                            Log.w(TAG_LOG, callbackResult.getWarningMessage());
                            break;
                        case FAILURE:
                            if (callbackResult.getError() instanceof StickerPackSaveException exception) {
                                callback.onStickerPackSaveResult(CallbackResult.failure(exception));
                            } else {
                                callback.onStickerPackSaveResult(CallbackResult.failure(new InternalAppException("Erro interno desconhecido!")));
                            }
                            break;
                    }
                });
    }
}
