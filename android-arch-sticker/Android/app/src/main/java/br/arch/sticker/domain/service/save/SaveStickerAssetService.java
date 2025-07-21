/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.save;

import static br.arch.sticker.domain.util.StickerPackPlaceholder.PLACEHOLDER_ANIMATED;
import static br.arch.sticker.domain.util.StickerPackPlaceholder.PLACEHOLDER_STATIC;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.view.core.util.convert.ConvertThumbnail;

public class SaveStickerAssetService {
    private final Context context;

    public SaveStickerAssetService(Context context) {
        this.context = context.getApplicationContext();
    }

    public CallbackResult<Boolean> saveStickerFromCache(
            @NonNull StickerPack stickerPack,
            @NonNull File stickerPackDirectory) throws StickerPackSaveException {
        if (!stickerPackDirectory.canWrite()) {
            return CallbackResult.failure(new StickerPackSaveException(
                    "Sem permissão de escrita no diretório destino: " + stickerPackDirectory,
                    ErrorCode.ERROR_PACK_SAVE_SERVICE
            ));
        }

        List<Sticker> stickerList = stickerPack.getStickers(); if (stickerList.isEmpty()) {
            Log.e("copyStickerFromCache", "Lista de stickers vazia!");
            return CallbackResult.failure(
                    new StickerPackSaveException("Lista de stickers vazia no pacote",
                                                 ErrorCode.ERROR_PACK_SAVE_SERVICE
                    ));
        }

        File thumbnailSticker = new File(context.getCacheDir(), stickerList.get(0).imageFileName);
        CallbackResult<Boolean> thumbnail = ConvertThumbnail.createThumbnail(thumbnailSticker,
                                                                             stickerPackDirectory
        ); if (thumbnail.isFailure() || thumbnail.isWarning()) {
            return CallbackResult.failure(new StickerPackSaveException(
                    "Falha ao criar thumbnail: " + thumbnail.getError(),
                    ErrorCode.ERROR_PACK_SAVE_SERVICE
            ));
        }

        Set<String> filesAlreadyCopied = new HashSet<>();

        for (Sticker sticker : stickerList) {
            String fileName = sticker.imageFileName;

            if (fileName == null || fileName.trim().isEmpty()) {
                return CallbackResult.failure(
                        new StickerPackSaveException("Nome do arquivo do sticker é nulo ou vazio",
                                                     ErrorCode.ERROR_PACK_SAVE_SERVICE
                        ));
            }

            if (PLACEHOLDER_ANIMATED.equals(fileName) || PLACEHOLDER_STATIC.equals(fileName)) {
                filesAlreadyCopied.add(fileName);
            }

            if (filesAlreadyCopied.contains(fileName)) {
                continue;
            }

            File sourceFile = new File(context.getCacheDir(), fileName); if (!sourceFile.exists()) {
                return CallbackResult.failure(new StickerPackSaveException(
                        String.format("Arquivo não encontrado: %s", fileName),
                        ErrorCode.ERROR_PACK_SAVE_SERVICE
                ));
            }

            File destFile = new File(stickerPackDirectory, fileName); try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Files.copy(sourceFile.toPath(), destFile.toPath());
                } else {
                    try (InputStream fileInputStream = new FileInputStream(
                            sourceFile); OutputStream fileOutputStream = new FileOutputStream(
                            destFile)) {

                        byte[] buffer = new byte[8192]; int length;

                        while ((length = fileInputStream.read(buffer)) > 0) {
                            fileOutputStream.write(buffer, 0, length);
                        }
                    }
                }

                filesAlreadyCopied.add(fileName);
            } catch (IOException exception) {
                return CallbackResult.failure(new StickerPackSaveException(
                        String.format("Erro ao copiar arquivo: %s", fileName), exception,
                        ErrorCode.ERROR_PACK_SAVE_SERVICE
                ));
            }
        }

        return CallbackResult.success(true);
    }
}
