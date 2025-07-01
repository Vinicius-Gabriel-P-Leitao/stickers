/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.lib;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.arch.sticker.core.error.code.MediaConversionErrorCode;
import br.arch.sticker.core.error.throwable.media.MediaConversionException;

public class NativeConvertToWebp {
    static {
        System.loadLibrary("sticker");
    }

    public native boolean convertToWebp(String inputPath, String outputPath);

    public interface ConversionCallback {
        void onSuccess(File file);

        void onError(Exception exception);
    }

    private static final ExecutorService nativeExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void convertToWebpAsync(String inputPath, String outputPath, ConversionCallback callback) throws MediaConversionException {
        nativeExecutor.submit(() -> {
            try {
                boolean success = convertToWebp(inputPath, outputPath);
                File outputFile = new File(outputPath);

                if (success && outputFile.exists()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException exception) {
                        throw new MediaConversionException(
                                exception.getMessage() != null ? exception.getMessage() : "Erro fazer ao pausar a thread, e não foi retornado mensagem de erro!",
                                exception.getCause(),
                                MediaConversionErrorCode.ERROR_NATIVE_CONVERSION);
                    }

                    callback.onSuccess(outputFile);
                } else {
                    callback.onError(new MediaConversionException(
                            "Falha na conversão ou arquivo não gerado.",
                            MediaConversionErrorCode.ERROR_NATIVE_CONVERSION));
                }
            } catch (Exception exception) {
                callback.onError(new MediaConversionException(
                        "Erro inesperado durante a conversão nativa: ",
                        exception,
                        MediaConversionErrorCode.ERROR_NATIVE_CONVERSION));
            }
        });
    }
}