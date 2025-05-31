/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.orchestrator;

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
import com.vinicius.sticker.domain.service.save.SaveStickerPackService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

// @formatter:off
public class StickerPackOrchestrator {
    private final static String TAG_LOG = StickerPackOrchestrator.class.getSimpleName();

    private static final List<Sticker> stickerList = new ArrayList<>();
    private static String uuidPack = UUID.randomUUID().toString();

    @FunctionalInterface
    public interface SavedStickerPackCallback {
        void onSavedStickerPack(CallbackResult<StickerPack> callbackResult);
    }

    public static void generateObjectToSave(
            Context context, boolean isAnimatedPack, List<File> fileList, String namePack,
            SavedStickerPackCallback savedStickerPackCallback
    ) {
        String nameStickerPack = namePack.trim() + " - [" + uuidPack.substring(0, 8) + "]";
        StickerPack stickerPack = new StickerPack(
                uuidPack,
                nameStickerPack,
                "vinicius",
                "thumbnail.jpg",
                "",
                "",
                "",
                "",
                "1",
                false,
                isAnimatedPack
        );

        stickerList.clear();
        for (File file : fileList) {
            boolean exists = false;

            for (Sticker sticker : stickerList) {
                if (sticker.imageFileName.equals(file.getName())) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                stickerList.add(new Sticker(file.getName().trim(), "\uD83D\uDDFF", "","Sticker pack"));
            }
        }
        stickerPack.setStickers(stickerList);

        try {
            SaveStickerPackService.saveStickerPack(
                    context, stickerPack, callbackResult -> {
                        switch (callbackResult.getStatus()) {
                            case SUCCESS:
                                savedStickerPackCallback.onSavedStickerPack(CallbackResult.success(callbackResult.getData()));
                                break;
                            case WARNING:
                                Log.w(TAG_LOG, callbackResult.getWarningMessage());
                                break;
                            case DEBUG:
                                Log.d(TAG_LOG, callbackResult.getDebugMessage());
                                break;
                            case FAILURE:
                                if (callbackResult.getError() instanceof StickerPackSaveException stickerPackSaveException) {
                                    savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(stickerPackSaveException));
                                    break;
                                }

                                if (callbackResult.getError() instanceof StickerFileException stickerFileException) {
                                    Log.d(TAG_LOG, Objects.requireNonNull(stickerFileException.getMessage()));
                                    savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(stickerFileException));
                                    break;
                                }

                                if (callbackResult.getError() instanceof PackValidatorException packValidatorException) {
                                    Log.e(TAG_LOG, Objects.requireNonNull(packValidatorException.getMessage()));
                                    savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(packValidatorException));
                                    break;
                                }

                                if(callbackResult.getError() instanceof  InvalidWebsiteUrlException invalidWebsiteUrlException) {
                                    Log.e(TAG_LOG, Objects.requireNonNull(invalidWebsiteUrlException.getMessage()));
                                    savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(invalidWebsiteUrlException));
                                    break;
                                }

                                if (callbackResult.getError() instanceof IOException ioException) {
                                    Log.d(TAG_LOG, Objects.requireNonNull(ioException.getMessage()));
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
    }

    public static void resetData() {
        stickerList.clear();
        uuidPack = UUID.randomUUID().toString();
    }
}
