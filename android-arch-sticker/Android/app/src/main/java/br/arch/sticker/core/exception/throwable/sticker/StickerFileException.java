/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.exception.throwable.sticker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

import br.arch.sticker.core.exception.throwable.base.AppCoreStateException;

public class StickerFileException extends AppCoreStateException {
    private final String stickerPackIdentifier;
    @Nullable
    private final String fileName;

    public StickerFileException(@NonNull String message, @NonNull String errorCode) {
        super(message, errorCode);
        this.stickerPackIdentifier = null;
        this.fileName = null;
    }

    public StickerFileException(
            @NonNull String message, @NonNull String errorCode, @Nullable String stickerPackIdentifier, @Nullable String fileName) {
        super(message, errorCode);
        this.stickerPackIdentifier = stickerPackIdentifier;
        this.fileName = fileName;
    }

    public String getStickerPackIdentifier() {
        return stickerPackIdentifier;
    }

    @Nullable
    public String getFileName() {
        return fileName;
    }

    public static StickerFileException durationExceeded(@NonNull String identifier, @Nullable String fileName, int minDuration) {
        String message = String.format(
                Locale.ROOT, "O limite mínimo de duração de um quadro da figurinha animada é %d ms, identificador do pacote: %s, arquivo: %s",
                minDuration, identifier, fileName);

        return new StickerFileException(message, ErrorFileCode.ERROR_STICKER_DURATION.name(), identifier, fileName);
    }

    // @formatter:off
    public enum ErrorFileCode {
        ERROR_FILE_SIZE("O arquivo ultrapassou o tamanho em KB permitido."),
        ERROR_SIZE_STICKER("O tamanho da figurinha deve ser de 512x512 e ultrapassou"),
        ERROR_STICKER_TYPE("As figurinhas não são condizente com o tipo do pacote"),
        ERROR_STICKER_DURATION("A duração da figurinha animada ultrapaça o permitido"),
        ERROR_FILE_TYPE("Tipo de arquivo não suportado.");

        private final String message;

        ErrorFileCode(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
