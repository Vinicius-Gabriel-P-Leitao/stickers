/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.service.fetch;

import static com.vinicius.sticker.domain.data.content.StickerContentProvider.STICKERS_ASSET;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.vinicius.sticker.BuildConfig;
import com.vinicius.sticker.domain.data.content.StickerContentProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FetchStickerAssetService {

    // @formatter:off
    public static byte[] fetchStickerAsset(
            @NonNull final String stickerPackIdentifier, @NonNull final String fileName, Context context) throws IOException {
        File stickerFile = new File(new File(new File(context.getFilesDir(), STICKERS_ASSET), stickerPackIdentifier), fileName);

        if (stickerFile.exists()) {
            try (InputStream inputStream = new FileInputStream(stickerFile); ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                int read;
                byte[] data = new byte[16384];

                while ((read = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, read);
                }

                return buffer.toByteArray();
            } catch (FileNotFoundException fileNotFoundException) {
                throw new IOException("Não foi possível ler a figurinha: " + stickerPackIdentifier + "/" + fileName, fileNotFoundException);
            } catch (IOException exception) {
                throw new IOException("Erro ao ler figurinha: " + stickerPackIdentifier + "/" + fileName, exception);
            }
        } else {
            throw new FileNotFoundException("Arquivo de figurinha não encontrado: " + stickerFile.getAbsolutePath());
        }
    }

    public static Uri buildStickerAssetUri(String identifier, String stickerName) {
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY)
                .appendPath(STICKERS_ASSET).appendPath(identifier).appendPath(stickerName).build();
    }
}
