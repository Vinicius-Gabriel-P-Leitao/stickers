/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.util.convert;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import br.arch.sticker.core.error.code.MediaConversionErrorCode;
import br.arch.sticker.core.error.throwable.media.MediaConversionException;
import br.arch.sticker.core.validation.MimeTypeValidator;
import br.arch.sticker.view.core.usecase.definition.MimeTypesSupported;
import br.arch.sticker.view.core.util.resolver.FileDetailsResolver;

public class ConvertMediaToStickerFormat {
    public interface MediaConversionCallback {
        void onSuccess(File outputFile);
        void onError(Exception exception);
    }

    public static void convertMediaToWebP(Context context, @NonNull Uri inputUri, @NonNull String outputFile, MediaConversionCallback callback) {
        Map<String, String> fileDetails = FileDetailsResolver.getFileDetailsFromUri(context, inputUri);
        if (fileDetails.isEmpty()) {
            callback.onError(new MediaConversionException(
                    "Unable to determine file MIME type!",
                    MediaConversionErrorCode.ERROR_PACK_CONVERSION_MEDIA));
            return;
        }

        fileDetails.forEach((filePath, mimeType) -> {
            try {
                if (MimeTypeValidator.validateUniqueMimeType(mimeType, MimeTypesSupported.IMAGE.getMimeTypes())) {
                    File imageOutputFile = ImageConverter.convertImageToWebP(context, filePath, outputFile);
                    callback.onSuccess(imageOutputFile);
                } else if (MimeTypeValidator.validateUniqueMimeType(mimeType, MimeTypesSupported.ANIMATED.getMimeTypes())) {
                    VideoConverter.convertVideoToWebP(context, inputUri, outputFile, callback);
                } else {
                    callback.onError(new IllegalArgumentException("Unsupported MIME type for conversion: " + mimeType));
                }
            } catch (IOException exception) {
                callback.onError(new MediaConversionException(
                        "Error during media conversion",
                        exception.getCause(),
                        MediaConversionErrorCode.ERROR_PACK_CONVERSION_MEDIA));
            }
        });
    }
}
