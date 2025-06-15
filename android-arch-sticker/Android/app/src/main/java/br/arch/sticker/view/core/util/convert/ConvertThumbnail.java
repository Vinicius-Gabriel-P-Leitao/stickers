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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import br.arch.sticker.core.exception.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.service.save.SaveStickerPackService;

public class ConvertThumbnail {
    public static final String THUMBNAIL_FILE = "thumbnail.jpg";

    public static void createThumbnail(File originalFile, File destinationDir, SaveStickerPackService.SaveStickerPackCallback callback) {
        if (!originalFile.exists()) {
            callback.onStickerPackSaveResult(
                    CallbackResult.failure(new StickerPackSaveException("Arquivo para thumbnail não encontrado: " + originalFile.getAbsolutePath())));
            return;
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath());
            if (bitmap == null) {
                callback.onStickerPackSaveResult(CallbackResult.warning("Erro ao decodificar o bitmap."));
                return;
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

            FileOutputStream fileOutputStream = new FileOutputStream(thumbnailFile);
            fileOutputStream.write(compressedBytes);
            fileOutputStream.flush();
            fileOutputStream.getFD().sync();
            fileOutputStream.close();

            callback.onStickerPackSaveResult(CallbackResult.debug("Thumbnail salva com sucesso: " + thumbnailFile.getAbsolutePath()));
        } catch (IOException exception) {
            callback.onStickerPackSaveResult(CallbackResult.failure(exception));
        }
    }
}
