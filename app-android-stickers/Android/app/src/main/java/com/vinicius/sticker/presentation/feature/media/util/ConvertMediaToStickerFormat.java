/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */
package com.vinicius.sticker.presentation.feature.media.util;

import static com.vinicius.sticker.core.validation.MimeTypesValidator.validateUniqueMimeType;
import static com.vinicius.sticker.presentation.feature.media.launcher.GalleryMediaPickerLauncher.ANIMATED_MIME_TYPES;
import static com.vinicius.sticker.presentation.feature.media.launcher.GalleryMediaPickerLauncher.IMAGE_MIME_TYPES;
import static com.vinicius.sticker.presentation.feature.media.util.CursorSearchUriMedia.getAbsolutePath;
import static com.vinicius.sticker.presentation.feature.media.util.CursorSearchUriMedia.getFileDetailsFromUri;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.vinicius.sticker.core.exception.MediaConversionException;
import com.vinicius.sticker.domain.libs.NativeConvertToWebp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class ConvertMediaToStickerFormat {
    private static MediaConversionCallback callback;

    public interface MediaConversionCallback {
        void onSuccess(File outputFile);

        void onError(Exception exception);
    }

    public static void convertMediaToWebP(Context context, Uri inputUri, String outputFile, MediaConversionCallback callback) {
        Map<String, String> mapDetailsFile = getFileDetailsFromUri(context, inputUri);
        if (mapDetailsFile.isEmpty()) {
            throw new MediaConversionException("Não foi possível determinar o tipo MIME do arquivo!");
        }

        mapDetailsFile.forEach((String fileName, String mimeType) -> {
            try {
                if (validateUniqueMimeType(mimeType, IMAGE_MIME_TYPES)) {
                    File imageOutputFile = convertImageToWebP(context, fileName, outputFile);
                    callback.onSuccess(imageOutputFile);
                } else if (validateUniqueMimeType(mimeType, ANIMATED_MIME_TYPES)) {
                    // NOTE: Callback já é dada no método
                    convertVideoToWebP(context, inputUri, outputFile, callback);
                } else {
                    throw new IllegalArgumentException("Tipo MIME não suportado para conversão: " + mimeType);
                }
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    public static File convertImageToWebP(Context context, String inputPath, String outputFileName) throws IOException {
        if (!outputFileName.toLowerCase().endsWith(".webp")) {
            outputFileName = outputFileName.replaceAll("\\.\\w+$", "") + ".webp";
        }

        String cleanedPath = inputPath.startsWith("file://") ? inputPath.substring(7) : inputPath;
        Bitmap bitmap = BitmapFactory.decodeFile(cleanedPath);

        if (bitmap != null) {
            Bitmap squareBitmap = cropAndResizeToSquare(bitmap);

            File outputFile = new File(context.getCacheDir(), outputFileName);
            try (FileOutputStream out = new FileOutputStream(outputFile)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    squareBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 80, out);
                } else {
                    squareBitmap.compress(Bitmap.CompressFormat.WEBP, 80, out);
                }

                return outputFile.getAbsoluteFile();
            } catch (IOException exception) {
                callback.onError(new MediaConversionException(exception.getMessage(), exception.getCause()));
            }
        }

        return null;
    }

    public static void convertVideoToWebP(Context context, Uri inputPath, String outputFileName, MediaConversionCallback callback) {
        if (!outputFileName.toLowerCase().endsWith(".webp")) {
            outputFileName = outputFileName.replaceAll("\\.\\w+$", "") + ".webp";
        }
        File outputFile = new File(context.getCacheDir(), outputFileName);

        NativeConvertToWebp nativeConvertToWebp = new NativeConvertToWebp();
        nativeConvertToWebp.convertToWebpAsync(getAbsolutePath(context, inputPath), outputFile.getAbsolutePath(), new NativeConvertToWebp.ConversionListener() {
            @Override
            public void onSuccess(File file) {
                callback.onSuccess(file);
            }

            @Override
            public void onError(Exception exception) {
                callback.onError(new MediaConversionException(exception.getMessage(), exception.getCause()));
            }
        });
    }

    private static Bitmap cropAndResizeToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newEdge = Math.min(width, height);

        int xOffset = (width - newEdge) / 2;
        int yOffset = (height - newEdge) / 2;

        Bitmap squareBitmap = Bitmap.createBitmap(bitmap, xOffset, yOffset, newEdge, newEdge);

        return Bitmap.createScaledBitmap(squareBitmap, 512, 512, true);
    }
}
