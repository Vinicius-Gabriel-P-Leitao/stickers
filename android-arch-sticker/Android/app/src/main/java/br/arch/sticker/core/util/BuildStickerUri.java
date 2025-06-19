/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.util;

import android.content.ContentResolver;
import android.net.Uri;

import br.arch.sticker.BuildConfig;
import br.arch.sticker.domain.data.content.StickerContentProvider;

public class BuildStickerUri {

    public static Uri buildStickerAssetUri(String stickerPackIdentifier, String fileName)
        {
            return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(BuildConfig.CONTENT_PROVIDER_AUTHORITY).appendPath(
                    StickerContentProvider.STICKERS_ASSET).appendPath(stickerPackIdentifier).appendPath(fileName).build();
        }
}
