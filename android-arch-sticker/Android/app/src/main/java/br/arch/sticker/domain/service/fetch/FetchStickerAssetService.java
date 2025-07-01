/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.fetch;

import static br.arch.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import br.arch.sticker.core.error.code.FetchErrorCode;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerException;

public class FetchStickerAssetService {
    private final Context context;

    public FetchStickerAssetService(Context context)
        {
            this.context = context.getApplicationContext();
        }

    public byte[] fetchStickerAsset(@NonNull final String stickerPackIdentifier, @NonNull final String fileName) throws FetchStickerException
        {
            File stickerFile = new File(new File(new File(context.getFilesDir(), STICKERS_ASSET), stickerPackIdentifier), fileName);

            if (stickerFile.exists()) {
                try (InputStream inputStream = new FileInputStream(stickerFile); ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                    int read;
                    byte[] bytes = new byte[16384];

                    while ((read = inputStream.read(bytes, 0, bytes.length)) != -1) {
                        buffer.write(bytes, 0, read);
                    }

                    return buffer.toByteArray();
                } catch (FileNotFoundException fileNotFoundException) {
                    throw new FetchStickerException(
                            "Não foi possível ler a figurinha: " + stickerPackIdentifier + "/" + fileName,
                                                    fileNotFoundException, FetchErrorCode.ERROR_EMPTY_STICKERPACK);
                } catch (IOException exception) {
                    throw new FetchStickerException(
                            "Erro ao ler figurinha: " + stickerPackIdentifier + "/" + fileName, exception, FetchErrorCode.ERROR_EMPTY_STICKERPACK);
                }
            } else {
                throw new FetchStickerException(
                        "Arquivo de figurinha não encontrado: " + stickerFile.getAbsolutePath(), FetchErrorCode.ERROR_EMPTY_STICKERPACK);
            }
        }
}
