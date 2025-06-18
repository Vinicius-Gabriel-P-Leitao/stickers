/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.service.fetch;

import static br.arch.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import br.arch.sticker.BuildConfig;
import br.arch.sticker.core.error.code.FetchErrorCode;
import br.arch.sticker.core.error.throwable.sticker.FetchStickerException;

public class FetchStickerAssetService {
    public static byte[] fetchStickerAsset(
            @NonNull final String stickerPackIdentifier, @NonNull final String fileName, @NonNull Context context) throws FetchStickerException
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

    public static Uri buildStickerAssetUri(String identifier, String stickerName)
        {
            return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(
                    STICKERS_ASSET).appendPath(identifier).appendPath(stickerName).build();
        }
}
