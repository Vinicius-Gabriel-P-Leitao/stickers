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

package com.vinicius.sticker.domain.libs;

import com.vinicius.sticker.core.exception.MediaConversionException;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NativeConvertToWebp {
    static {
        System.loadLibrary("sticker");
    }

    public native boolean convertToWebp(String inputPath, String outputPath);

    public interface ConversionListener {
        void onSuccess(File file);

        void onError(Exception exception);
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void convertToWebpAsync(String inputPath, String outputPath, ConversionListener listener) throws MediaConversionException {
        executorService.submit(() -> {
            try {
                boolean success = convertToWebp(inputPath, outputPath);
                File outputFile = new File(outputPath);

                if (success && outputFile.exists()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException exception) {
                        throw new MediaConversionException(exception.getMessage(), exception.getCause());
                    }

                    listener.onSuccess(outputFile);
                } else {
                    listener.onError(new MediaConversionException("Falha na conversão ou arquivo não gerado."));
                }
            } catch (Exception exception) {
                listener.onError(new MediaConversionException("Erro inesperado durante a conversão nativa: ", exception));
            }
        });
    }
}