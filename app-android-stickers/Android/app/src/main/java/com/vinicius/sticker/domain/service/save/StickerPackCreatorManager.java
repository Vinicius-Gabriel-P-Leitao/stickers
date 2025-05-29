/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.service.save;

import android.content.Context;
import android.util.Log;

import com.vinicius.sticker.core.exception.DeleteStickerException;
import com.vinicius.sticker.core.exception.InvalidWebsiteUrlException;
import com.vinicius.sticker.core.exception.PackValidatorException;
import com.vinicius.sticker.core.exception.StickerFileException;
import com.vinicius.sticker.core.exception.StickerPackSaveException;
import com.vinicius.sticker.core.exception.base.InternalAppException;
import com.vinicius.sticker.core.pattern.CallbackResult;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

// @formatter:off
public class StickerPackCreatorManager {

    private static final List<Sticker> stickers = new ArrayList<>();
    private static final String uuidPack = UUID.randomUUID().toString();

    @FunctionalInterface
    public interface SavedStickerPackCallback {
        void onSavedStickerPack(CallbackResult<StickerPack> callbackResult);
    }

    public static void generateJsonPack(
            Context context, boolean isAnimatedPack, List<File> fileList, String namePack,
            SavedStickerPackCallback savedStickerPackCallback
    ) {
        try {
            stickers.clear();
            StickerPack stickerPack = new StickerPack(
                    uuidPack,
                    namePack.trim(),
                    "vinicius",
                    "thumbnail.jpg",
                    "",
                    "",
                    "",
                    "",
                    "1",
                    false,
                    isAnimatedPack);

            for (File file : fileList) {
                boolean exists = false;

                for (Sticker sticker : stickers) {
                    if (sticker.imageFileName.equals(file.getName())) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    stickers.add(new Sticker(file.getName().trim(), "\uD83D\uDDFF", "Sticker pack"));
                }
            }

            stickerPack.setStickers(stickers);
            try {
                if (stickerPack.identifier == null) {
                    throw new DeleteStickerException("Erro ao encontrar o id do pacote para deletar.");
                }

                StickerPackSaveService.generateStructureForSavePack(
                        context, stickerPack, stickerPack.identifier, callbackResult -> {
                            switch (callbackResult.getStatus()) {
                                case SUCCESS:
                                    savedStickerPackCallback.onSavedStickerPack(CallbackResult.success(callbackResult.getData()));
                                    break;
                                case WARNING:
                                    Log.w("SaveStickerPack", callbackResult.getWarningMessage());
                                    break;
                                case DEBUG:
                                    Log.d("SaveStickerPack", callbackResult.getDebugMessage());
                                    break;
                                case FAILURE:
                                    if (callbackResult.getError() instanceof StickerPackSaveException stickerPackSaveException) {
                                        savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(stickerPackSaveException));
                                        break;
                                    }

                                    if (callbackResult.getError() instanceof StickerFileException stickerFileException) {
                                        Log.d("StickerPackCreatorManager", Objects.requireNonNull(stickerFileException.getMessage()));
                                        savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(stickerFileException));
                                        break;
                                    }

                                    if (callbackResult.getError() instanceof PackValidatorException packValidatorException) {
                                        Log.e("StickerPackCreatorManager", Objects.requireNonNull(packValidatorException.getMessage()));
                                        savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(packValidatorException));
                                        break;
                                    }

                                    if(callbackResult.getError() instanceof  InvalidWebsiteUrlException invalidWebsiteUrlException) {
                                        Log.e("StickerPackCreatorManager", Objects.requireNonNull(invalidWebsiteUrlException.getMessage()));
                                        savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(invalidWebsiteUrlException));
                                        break;
                                    }

                                    if (callbackResult.getError() instanceof IOException ioException) {
                                        Log.d("StickerPackCreatorManager", Objects.requireNonNull(ioException.getMessage()));
                                        savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(new InternalAppException("Erro interno de IO nos arquivos!")));
                                        break;
                                    }

                                    savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(new InternalAppException("Erro interno desconhecido!")));
                                    break;
                            }
                        });
            } catch (DeleteStickerException exception) {
                throw new RuntimeException(exception);
            }
        } catch (DeleteStickerException exception) {
            throw new RuntimeException(exception);
        }
    }
}
