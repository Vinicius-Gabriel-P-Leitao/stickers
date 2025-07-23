/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.util.convert;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.media.MediaConversionException;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;

public class ImageConverter {
    private final static String TAG_LOG = ImageConverter.class.getSimpleName();

    private final ApplicationTranslate applicationTranslate;
    private final Context context;

    public ImageConverter(Context context) {
        this.context = context.getApplicationContext();
        this.applicationTranslate = new ApplicationTranslate(this.context.getResources());
    }

    public File convertImageToWebPAsyncFuture(@NonNull String inputPath, @NonNull String outputFileName) throws MediaConversionException {
        String finalOutputFileName = ConvertMediaToStickerFormat.ensureWebpExtension(outputFileName);
        File outputFile = new File(context.getCacheDir(), finalOutputFileName);

        String cleanedPath = inputPath.startsWith("file://") ? inputPath.substring(7) : inputPath;
        Bitmap bitmap = BitmapFactory.decodeFile(cleanedPath);

        if (bitmap == null) {
            throw new MediaConversionException(
                    applicationTranslate.translate(R.string.error_decode_image, cleanedPath).log(TAG_LOG, Level.ERROR).get(),
                    ErrorCode.ERROR_PACK_CONVERSION_MEDIA
            );
        }

        Bitmap squareBitmap = cropImageAndResizeToSquare(bitmap);

        try (FileOutputStream out = new FileOutputStream(outputFile)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                squareBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, out);
            } else {
                squareBitmap.compress(Bitmap.CompressFormat.WEBP, 80, out);
            }

            return outputFile.getAbsoluteFile();
        } catch (IOException exception) {
            throw new MediaConversionException(
                    applicationTranslate.translate(R.string.error_media_conversion).log(TAG_LOG, Level.ERROR, exception).get(), exception.getCause(),
                    ErrorCode.ERROR_PACK_CONVERSION_MEDIA
            );
        }
    }

    private static Bitmap cropImageAndResizeToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newEdge = Math.min(width, height);
        int xOffset = (width - newEdge) / 2;
        int yOffset = (height - newEdge) / 2;

        Bitmap squareBitmap = Bitmap.createBitmap(bitmap, xOffset, yOffset, newEdge, newEdge);
        return Bitmap.createScaledBitmap(squareBitmap, 512, 512, true);
    }
}
