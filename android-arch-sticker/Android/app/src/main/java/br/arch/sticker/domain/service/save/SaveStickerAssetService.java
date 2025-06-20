/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.save;

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
import java.nio.file.Path;
import java.util.List;

import br.arch.sticker.core.error.code.SaveErrorCode;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.view.core.util.convert.ConvertThumbnail;

// @formatter:off
public class SaveStickerAssetService {
    public static CallbackResult<Boolean> copyStickerFromCache(@NonNull Context context, @NonNull StickerPack stickerPack,
                                                               @NonNull File stickerPackDirectory) throws StickerPackSaveException {
        if (!stickerPackDirectory.canWrite()) {
            return CallbackResult.failure(new StickerPackSaveException(
                    "Sem permissão de escrita no diretório destino: " + stickerPackDirectory,
                    SaveErrorCode.ERROR_PACK_SAVE_SERVICE));
        }

        List<Sticker> stickerList = stickerPack.getStickers();
        if (stickerList.isEmpty()) {
            Log.e("copyStickerFromCache", "Lista de stickers vazia!");
            return CallbackResult.failure(new StickerPackSaveException(
                    "Lista de stickers vazia no pacote",
                    SaveErrorCode.ERROR_PACK_SAVE_SERVICE));
        }

        File thumbnailSticker = new File(context.getCacheDir(), stickerList.get(0).imageFileName);
        CallbackResult<Void> thumbnail = ConvertThumbnail.createThumbnail(thumbnailSticker, stickerPackDirectory);
        if (!thumbnail.isDebug() && !thumbnail.isSuccess()) {
            return CallbackResult.failure(new StickerPackSaveException(
                    "Falha ao criar thumbnail: " + thumbnail.getError(),
                    SaveErrorCode.ERROR_PACK_SAVE_SERVICE));
        }

        for (Sticker sticker : stickerList) {
            String fileName = sticker.imageFileName;
            if (fileName == null || fileName.trim().isEmpty()) {
                return CallbackResult.failure(new StickerPackSaveException(
                        "Nome do arquivo do sticker é nulo ou vazio",
                        SaveErrorCode.ERROR_PACK_SAVE_SERVICE));
            }

            File sourceFile = new File(context.getCacheDir(), fileName);
            if (!sourceFile.exists()) {
                return CallbackResult.failure(new StickerPackSaveException(
                        String.format("Arquivo não encontrado: %s", fileName),
                        SaveErrorCode.ERROR_PACK_SAVE_SERVICE));
            }

            File destFile = new File(stickerPackDirectory, fileName);
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Path sourcePath = sourceFile.toPath();
                    Path destPath = destFile.toPath();

                    Files.copy(sourcePath, destPath);
                } else {
                    try (InputStream fileInputStream = new FileInputStream(sourceFile);
                         OutputStream fileOutputStream = new FileOutputStream(destFile)) {

                        byte[] buffer = new byte[8192];
                        int length;

                        while ((length = fileInputStream.read(buffer)) > 0) {
                            fileOutputStream.write(buffer, 0, length);
                        }
                    }
                }
            } catch (IOException exception) {
                return CallbackResult.failure(new StickerPackSaveException(
                        String.format("Erro copiando arquivo: %s", fileName),
                        exception,
                        SaveErrorCode.ERROR_PACK_SAVE_SERVICE));
            }
        }

        return CallbackResult.success(true);
    }
}
