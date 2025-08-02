/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.util.convert;

import static br.arch.sticker.core.validation.StickerValidator.IMAGE_HEIGHT;
import static br.arch.sticker.core.validation.StickerValidator.IMAGE_WIDTH;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import br.arch.sticker.R;
import br.arch.sticker.core.error.ErrorCode;
import br.arch.sticker.core.error.throwable.media.MediaConversionException;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;
import br.arch.sticker.view.core.util.resolver.FileDetailsResolver;

public class ImageConverter {
    private final static String TAG_LOG = ImageConverter.class.getSimpleName();

    private final ApplicationTranslate applicationTranslate;
    private final FileDetailsResolver fileDetailsResolver;
    private final Context context;

    public ImageConverter(Context context) {
        this.context = context.getApplicationContext();
        this.fileDetailsResolver = new FileDetailsResolver(this.context);
        this.applicationTranslate = new ApplicationTranslate(this.context.getResources());
    }

    public File convertImageToWebp(@NonNull String inputPath, @NonNull File outputFile, int quality) throws MediaConversionException {
        String cleanedPath = inputPath.startsWith("file://") ? inputPath.substring(7) : inputPath;
        Bitmap bitmap = BitmapFactory.decodeFile(cleanedPath);

        if (bitmap == null) {
            throw new MediaConversionException(
                    applicationTranslate.translate(R.string.error_decode_image, cleanedPath).log(TAG_LOG, Level.ERROR).get(),
                    ErrorCode.ERROR_PACK_CONVERSION_MEDIA
            );
        }

        Bitmap squareBitmap = cropImageAndResizeToSquare(bitmap);
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                squareBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, fileOutputStream);
            } else {
                squareBitmap.compress(Bitmap.CompressFormat.WEBP, quality, fileOutputStream);
            }

            return outputFile.getAbsoluteFile();
        } catch (IOException exception) {
            throw new MediaConversionException(
                    applicationTranslate.translate(R.string.error_media_conversion).log(TAG_LOG, Level.ERROR, exception).get(), exception.getCause(),
                    ErrorCode.ERROR_PACK_CONVERSION_MEDIA
            );
        }
    }

    public File convertImageToWebpCropped(Uri uri, Rect cropRect) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);

        Bitmap croppedBitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawColor(Color.BLACK);

        float dx = (IMAGE_WIDTH / 2f) - (cropRect.width() / 2f);
        float dy = (IMAGE_HEIGHT / 2f) - (cropRect.height() / 2f);

        Matrix drawMatrix = new Matrix();
        drawMatrix.postTranslate(dx - cropRect.left, dy - cropRect.top);
        canvas.drawBitmap(bitmap, drawMatrix, null);

        String inputFile = fileDetailsResolver.getAbsolutePath(uri);
        String inputFileName = new File(inputFile).getName();

        String outputFile = new File(context.getCacheDir(), inputFileName).getAbsolutePath();
        String finalOutputFileName = ConvertMediaToStickerFormat.ensureWebpExtension(outputFile);

        File fileToSave = new File(finalOutputFileName);

        try (FileOutputStream fileOutputStream = new FileOutputStream(fileToSave)) {
            croppedBitmap.compress(Bitmap.CompressFormat.WEBP, 100, fileOutputStream);
            fileOutputStream.flush();

            return fileToSave;
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
