/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.util.convert;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import br.arch.sticker.core.error.code.SaveErrorCode;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.service.save.SaveStickerPackService;

public class ConvertThumbnail {
    public static final String THUMBNAIL_FILE = "thumbnail.jpg";

    @NonNull
    public static CallbackResult<Void> createThumbnail(@NonNull File originalFile, @NonNull File destinationDir)
        {
            if (!originalFile.exists()) {
                return CallbackResult.failure(
                        new StickerPackSaveException(String.format("Arquivo para thumbnail não encontrado: %s", originalFile.getAbsolutePath()),
                                SaveErrorCode.ERROR_PACK_SAVE_THUMBNAIL));
            }

            try {
                Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath());
                if (bitmap == null) {
                    return CallbackResult.warning("Erro ao decodificar o bitmap.");
                }

                File thumbnailFile = new File(destinationDir, THUMBNAIL_FILE);
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();

                int quality = 100;
                byte[] compressedBytes;

                do {
                    outStream.reset();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream);
                    compressedBytes = outStream.toByteArray();
                    quality -= 5;
                } while (compressedBytes.length > 40 * 1024 && quality > 5);

                try (FileOutputStream fileOutputStream = new FileOutputStream(thumbnailFile)) {
                    fileOutputStream.write(compressedBytes);
                    fileOutputStream.flush();
                    fileOutputStream.getFD().sync();
                }

                return CallbackResult.debug(String.format("Thumbnail salva com sucesso: %s", thumbnailFile.getAbsolutePath()));
            } catch (IOException exception) {
                return CallbackResult.failure(exception);
            }
        }
}
