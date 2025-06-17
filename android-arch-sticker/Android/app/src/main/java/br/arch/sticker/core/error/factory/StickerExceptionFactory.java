/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.error.factory;

import androidx.annotation.NonNull;

import java.util.Locale;

import br.arch.sticker.core.error.code.StickerAssetErrorCode;
import br.arch.sticker.core.error.code.StickerPackErrorCode;
import br.arch.sticker.core.error.throwable.sticker.StickerFileException;
import br.arch.sticker.core.error.throwable.sticker.StickerValidatorException;

public final class StickerExceptionFactory {
    private StickerExceptionFactory() {
    }

    public static StickerFileException durationExceeded(String stickerPackIdentifier, String fileName, int minDuration) {
        String message = String.format(
                Locale.ROOT,
                "O limite mínimo de duração de um quadro da figurinha animada é %d ms. Pacote: %s, Arquivo: %s",
                minDuration,
                stickerPackIdentifier,
                fileName);

        return new StickerFileException(
                message,
                StickerAssetErrorCode.ERROR_STICKER_DURATION,
                stickerPackIdentifier,
                fileName);
    }

    public static StickerFileException staticFileTooLarge(String stickerPackIdentifier, String fileName, int maxKb, int actualKb) {
        return new StickerFileException(
                String.format(
                        Locale.ROOT,
                        "A figurinha estática deve ser menor que %d KB, o arquivo atual tem %d KB, identificador do pacote: %s, arquivo: %s",
                        maxKb,
                        actualKb,
                        stickerPackIdentifier,
                        fileName),
                StickerAssetErrorCode.ERROR_FILE_SIZE,
                stickerPackIdentifier,
                fileName);
    }

    public static StickerFileException animatedFileTooLarge(String stickerPackIdentifier, String fileName, int maxKb, int actualKb) {
        return new StickerFileException(
                String.format(
                        Locale.ROOT,
                        "A figurinha animada deve ser menor que %d KB, o arquivo atual tem %d KB, identificador do pacote: %s, arquivo: %s",
                        maxKb,
                        actualKb,
                        stickerPackIdentifier,
                        fileName),
                StickerAssetErrorCode.ERROR_FILE_SIZE,
                stickerPackIdentifier,
                fileName);
    }

    public static StickerFileException invalidHeight(String stickerPackIdentifier, String fileName, int expected, int actual) {
        return new StickerFileException(
                String.format(
                        Locale.ROOT,
                        "A altura da figurinha deve ser %d, a altura atual é %d, identificador do pacote: %s, arquivo: %s",
                        expected,
                        actual,
                        stickerPackIdentifier,
                        fileName),
                StickerAssetErrorCode.ERROR_SIZE_STICKER,
                stickerPackIdentifier,
                fileName);
    }

    public static StickerFileException invalidWidth(String stickerPackIdentifier, String fileName, int expected, int actual) {
        return new StickerFileException(
                String.format(
                        Locale.ROOT,
                        "A largura da figurinha deve ser %d, a largura atual é %d, identificador do pacote: %s, arquivo: %s",
                        expected,
                        actual,
                        stickerPackIdentifier,
                        fileName),
                StickerAssetErrorCode.ERROR_SIZE_STICKER,
                stickerPackIdentifier,
                fileName);
    }

    public static StickerFileException expectedAnimatedButGotStatic(String stickerPackIdentifier, String fileName) {
        return new StickerFileException(
                String.format(
                        Locale.ROOT,
                        "Este pacote está marcado como animado, todas as figurinhas devem ser animadas. Identificador do pacote: %s, arquivo: %s",
                        stickerPackIdentifier,
                        fileName),
                StickerAssetErrorCode.ERROR_STICKER_TYPE,
                stickerPackIdentifier,
                fileName);
    }

    public static StickerFileException expectedStaticButGotAnimated(String stickerPackIdentifier, String fileName) {
        return new StickerFileException(
                String.format(
                        Locale.ROOT,
                        "Este pacote não está marcado como animado, todas as figurinhas devem ser estáticas. Identificador do pacote: %s, arquivo: %s",
                        stickerPackIdentifier,
                        fileName),
                StickerAssetErrorCode.ERROR_STICKER_TYPE,
                stickerPackIdentifier,
                fileName);
    }

    public static StickerFileException durationExceeded(String stickerPackIdentifier, String fileName, int maxDuration, int actualDuration) {
        return new StickerFileException(
                String.format(
                        Locale.ROOT,
                        "A duração máxima da animação é: %d ms, a duração atual é: %d ms, identificador do pacote: %s, arquivo: %s",
                        maxDuration,
                        actualDuration,
                        stickerPackIdentifier,
                        fileName),
                StickerAssetErrorCode.ERROR_STICKER_DURATION,
                stickerPackIdentifier,
                fileName);
    }

    public static StickerFileException invalidWebP(String stickerPackIdentifier, String fileName) {
        return new StickerFileException(
                String.format(
                        Locale.ROOT,
                        "Erro ao processar a imagem WebP. Identificador do pacote: %s, arquivo: %s",
                        stickerPackIdentifier,
                        fileName),
                StickerAssetErrorCode.ERROR_FILE_TYPE,
                stickerPackIdentifier,
                fileName);
    }

    public static StickerFileException fromStickerValidity(@NonNull String stickerPackIdentifier) {
        return new StickerFileException(
                "Figurinha inválida.",
                StickerAssetErrorCode.ERROR_FILE_SIZE,
                stickerPackIdentifier,
                null);
    }

    public static StickerValidatorException missingStickerFileName(String stickerPackIdentifier) {
        return new StickerValidatorException(
                String.format(
                        "Nenhum caminho de figurinha para o figurinha, identificador do pacote de figurinhas: %s",
                        stickerPackIdentifier),
                StickerPackErrorCode.INVALID_STICKER_FILENAME);
    }

    public static StickerValidatorException accessibilityTextTooLong(String stickerPackIdentifier, String fileName) {
        return new StickerValidatorException(
                String.format(
                        "Comprimento do texto de acessibilidade excedeu o limite, identificador do pacote de figurinhas: %s, arquivo: %s",
                        stickerPackIdentifier,
                        fileName),
                StickerPackErrorCode.INVALID_STICKER_ACCESSIBILITY);
    }
}
