/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.orchestrator;

import static br.arch.sticker.view.core.util.convert.ConvertThumbnail.THUMBNAIL_FILE;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import br.arch.sticker.core.error.throwable.base.InternalAppException;
import br.arch.sticker.core.error.throwable.content.InvalidWebsiteUrlException;
import br.arch.sticker.core.error.throwable.sticker.PackValidatorException;
import br.arch.sticker.core.error.throwable.sticker.StickerFileException;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.domain.service.save.SaveStickerPackService;

public class StickerPackOrchestrator {
    private final static String TAG_LOG = StickerPackOrchestrator.class.getSimpleName();

    private static final List<Sticker> stickerList = new ArrayList<>();
    private static String uuidPack = UUID.randomUUID().toString();

    @FunctionalInterface
    public interface SavedStickerPackCallback {
        void onSavedStickerPack(CallbackResult<StickerPack> callbackResult);
    }

    private static SavedStickerPackCallback savedStickerPackCallback;

    private static final SaveStickerPackService.SaveStickerPackCallback saveStickerPackCallback = new SaveStickerPackService.SaveStickerPackCallback() {
        @Override
        public void onStickerPackSaveResult(CallbackResult<StickerPack> callbackResult) {
            switch (callbackResult.getStatus()) {
                case SUCCESS:
                    savedStickerPackCallback.onSavedStickerPack(CallbackResult.success(callbackResult.getData()));
                    break;
                case WARNING:
                    Log.w(
                            TAG_LOG,
                            callbackResult.getWarningMessage());
                    break;
                case DEBUG:
                    Log.d(
                            TAG_LOG,
                            callbackResult.getDebugMessage());
                    break;
                case FAILURE:
                    if (callbackResult.getError() instanceof StickerPackSaveException stickerPackSaveException) {
                        Log.e(
                                stickerPackSaveException.getErrorCodeName(),
                                Objects.requireNonNull(stickerPackSaveException.getMessage()));
                        savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(stickerPackSaveException));
                        break;
                    }

                    if (callbackResult.getError() instanceof StickerFileException stickerFileException) {
                        Log.d(
                                stickerFileException.getErrorCodeName(),
                                Objects.requireNonNull(stickerFileException.getMessage()));
                        savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(stickerFileException));
                        break;
                    }

                    if (callbackResult.getError() instanceof PackValidatorException packValidatorException) {
                        Log.e(
                                packValidatorException.getErrorCodeName(),
                                Objects.requireNonNull(packValidatorException.getMessage()));
                        savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(packValidatorException));
                        break;
                    }

                    if (callbackResult.getError() instanceof InvalidWebsiteUrlException invalidWebsiteUrlException) {
                        Log.e(
                                invalidWebsiteUrlException.getErrorCodeName(),
                                Objects.requireNonNull(invalidWebsiteUrlException.getMessage()));
                        savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(invalidWebsiteUrlException));
                        break;
                    }

                    if (callbackResult.getError() instanceof IOException ioException) {
                        Log.d(
                                TAG_LOG,
                                Objects.requireNonNull(ioException.getMessage()));
                        savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(new InternalAppException("Erro interno de IO nos arquivos!")));
                        break;
                    }

                    savedStickerPackCallback.onSavedStickerPack(CallbackResult.failure(new InternalAppException("Erro interno desconhecido!")));
                    break;
            }
        }
    }

    public static void generateObjectToSave(Context context, boolean isAnimatedPack, List<File> fileList, String namePack) {
        String nameStickerPack = namePack.trim() + " - [" + uuidPack.substring(0, 8) + "]";
        StickerPack stickerPack = new StickerPack(
                uuidPack, nameStickerPack, "vinicius", THUMBNAIL_FILE, "", "", "",
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

            String accessibility = isAnimatedPack ? String.format(
                    "Pacote animado com nome  %s",
                    nameStickerPack) : String.format(
                    "Pacote estático com nome  %s",
                    nameStickerPack);

            if (!exists) {
                stickerList.add(new Sticker(
                        file.getName().trim(),
                        "\uD83D\uDDFF",
                        "",
                        accessibility,
                        uuidPack));
            }
        }
        stickerPack.setStickers(stickerList);

        SaveStickerPackService.saveStickerPack(
                context,
                stickerPack,
                saveStickerPackCallback);
    }

    public static void resetData() {
        stickerList.clear();
        uuidPack = UUID.randomUUID().toString();
    }
}
