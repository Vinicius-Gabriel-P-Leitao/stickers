/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */
package br.arch.sticker.view.core.util.convert;

import static br.arch.sticker.core.error.ErrorCode.ERROR_PACK_CONVERSION_MEDIA;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import br.arch.sticker.core.error.throwable.media.MediaConversionException;
import br.arch.sticker.core.validation.MimeTypeValidator;
import br.arch.sticker.view.core.usecase.definition.MimeTypesSupported;
import br.arch.sticker.view.core.util.resolver.FileDetailsResolver;

public class ConvertMediaToStickerFormat {

    private final FileDetailsResolver fileDetailsResolver;
    private final ImageConverter imageConverter;
    private final VideoConverter videoConverter;

    public ConvertMediaToStickerFormat(Context paramContext) {
        Context context = paramContext.getApplicationContext();
        this.imageConverter = new ImageConverter(context);
        this.videoConverter = new VideoConverter(context);
        this.fileDetailsResolver = new FileDetailsResolver(context);
    }

    public CompletableFuture<File> convertMediaToWebPAsyncFuture(
            @NonNull Uri inputUri, @NonNull String outputFileName) throws MediaConversionException {

        Map<String, String> fileDetails = fileDetailsResolver.getFileDetailsFromUri(inputUri);
        CompletableFuture<File> future = new CompletableFuture<>();

        if (fileDetails.isEmpty()) {
            future.completeExceptionally(
                    new MediaConversionException("Unable to determine file MIME type!", ERROR_PACK_CONVERSION_MEDIA));
            return future;
        }

        for (Map.Entry<String, String> entry : fileDetails.entrySet()) {
            String filePath = entry.getKey();
            String mimeType = entry.getValue();

            try {
                if (MimeTypeValidator.validateUniqueMimeType(mimeType, MimeTypesSupported.IMAGE.getMimeTypes())) {
                    File file = imageConverter.convertImageToWebPAsyncFuture(filePath, outputFileName);
                    future.complete(file);
                    return future;
                }

                if (MimeTypeValidator.validateUniqueMimeType(mimeType, MimeTypesSupported.ANIMATED.getMimeTypes())) {
                    return videoConverter.convertVideoToWebPAsyncFuture(filePath, outputFileName);
                }

                future.completeExceptionally(new MediaConversionException(
                        String.format("MIME type não suportado: %s", mimeType),
                        ERROR_PACK_CONVERSION_MEDIA
                ));
            } catch (MediaConversionException exception) {
                future.completeExceptionally(new MediaConversionException(
                        "Error durante conversão de midia.", exception.getCause(), ERROR_PACK_CONVERSION_MEDIA));
            }
        }

        return future;
    }

    public static String ensureWebpExtension(String fileName) {
        if (!fileName.toLowerCase().endsWith(".webp")) {
            return fileName.replaceAll("\\.\\w+$", "") + ".webp";
        }
        return fileName;
    }
}

