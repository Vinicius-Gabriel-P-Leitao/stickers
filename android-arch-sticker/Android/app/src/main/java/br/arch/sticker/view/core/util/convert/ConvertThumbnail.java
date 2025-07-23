/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.util.convert;

import static br.arch.sticker.core.error.ErrorCode.ERROR_PACK_SAVE_THUMBNAIL;
import static br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import br.arch.sticker.R;
import br.arch.sticker.core.error.throwable.sticker.StickerPackSaveException;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.util.ApplicationTranslate;

public class ConvertThumbnail {
    private final static String TAG_LOG = ConvertThumbnail.class.getSimpleName();

    public static final String THUMBNAIL_FILE = "thumbnail.jpg";

    @NonNull
    public static CallbackResult<Boolean> createThumbnail(Context context, @NonNull File originalFile, @NonNull File destinationDir) {
        if (!originalFile.exists()) {
            return CallbackResult.failure(new StickerPackSaveException(
                    ApplicationTranslate.translate(context, R.string.error_thumbnail_file_not_found)
                            .log(TAG_LOG, Level.ERROR, originalFile.getAbsolutePath()).get(), ERROR_PACK_SAVE_THUMBNAIL
            ));
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(originalFile.getAbsolutePath());
            if (bitmap == null) {
                return CallbackResult.failure(new StickerPackSaveException(
                        ApplicationTranslate.translate(context, R.string.error_decode_bitmap).log(TAG_LOG, Level.ERROR).get(),
                        ERROR_PACK_SAVE_THUMBNAIL
                ));
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

            return CallbackResult.success(true);
        } catch (IOException exception) {
            return CallbackResult.failure(exception);
        }
    }
}
