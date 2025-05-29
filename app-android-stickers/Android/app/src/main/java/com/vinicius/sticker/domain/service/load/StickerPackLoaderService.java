/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * Modifications by Vinícius, 2025
 * Licensed under the Vinícius Non-Commercial Public License (VNCL)
 */

package com.vinicius.sticker.domain.service.load;

import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.ANDROID_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.ANIMATED_STICKER_PACK;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.AVOID_CACHE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.IMAGE_DATA_VERSION;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.IOS_APP_DOWNLOAD_LINK_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.LICENSE_AGREEMENT_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.PRIVACY_POLICY_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.PUBLISHER_EMAIL;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.PUBLISHER_WEBSITE;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_ICON_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_IDENTIFIER_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_NAME_IN_QUERY;
import static com.vinicius.sticker.domain.data.database.dao.StickerDatabaseHelper.STICKER_PACK_PUBLISHER_IN_QUERY;
import static com.vinicius.sticker.domain.service.delete.StickerDeleteService.deleteStickerByIdentifier;
import static com.vinicius.sticker.domain.service.load.StickerLoaderService.getStickersForPack;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.vinicius.sticker.BuildConfig;
import com.vinicius.sticker.core.exception.StickerFileException;
import com.vinicius.sticker.core.validation.StickerPackValidator;
import com.vinicius.sticker.core.validation.StickerValidator;
import com.vinicius.sticker.domain.data.content.provider.StickerContentProvider;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** Busca lista com pacotes de figurinhas. */
public class StickerPackLoaderService {

  /**
   * <b>Descrição:</b>Busca os pacotes de figurinhas direto do content provider.
   *
   * <pre>{@code
   * ArrayList<StickerPack> stickerPackList = StickerPackLoaderService.fetchStickerPacks(context);
   * }</pre>
   *
   * @param context Contexto da aplicação.
   * @return Lista de pacotes de figurinhas.
   * @throws IllegalStateException Caso não haja pacotes de figurinhas no content provider.
   */
  @NonNull
  public static ArrayList<StickerPack> fetchStickerPackList(Context context)
      throws IllegalStateException {
    final Cursor cursor =
        context
            .getContentResolver()
            .query(StickerContentProvider.AUTHORITY_URI, null, null, null, null);

    if (cursor == null) {
      throw new IllegalStateException(
          "could not fetch from content provider, " + BuildConfig.CONTENT_PROVIDER_AUTHORITY);
    }

    HashSet<String> identifierSet = new HashSet<>();
    final ArrayList<StickerPack> stickerPackList;

    if (cursor.moveToFirst()) {
      stickerPackList = new ArrayList<>(fetchListFromContentProvider(cursor));
    } else {
      cursor.close();
      throw new IllegalStateException("No sticker packs found in the content provider");
    }

    for (StickerPack stickerPack : stickerPackList) {
      if (identifierSet.contains(stickerPack.identifier)) {
        throw new IllegalStateException(
            "sticker pack identifiers should be unique, there are more than one pack with identifier: "
                + stickerPack.identifier);
      } else {
        identifierSet.add(stickerPack.identifier);
      }
    }

    if (stickerPackList.isEmpty()) {
      throw new IllegalStateException("There should be at least one sticker pack in the app");
    }

    for (StickerPack stickerPack : stickerPackList) {
      final List<Sticker> stickers = getStickersForPack(context, stickerPack);

      try {
        StickerPackValidator.verifyStickerPackValidity(context, stickerPack);

        for (Sticker sticker : stickerPack.getStickers()) {
          StickerValidator.verifyStickerValidity(
              context, stickerPack.identifier, sticker, stickerPack.animatedStickerPack);
        }

        stickerPack.setStickers(stickerPack.getStickers());
      } catch (IllegalStateException exception) {
        if (exception instanceof StickerFileException sizeFileLimitException) {
          // TODO: Trocar por método que vai marcar no banco de dados o pacote e figurinha.
          deleteStickerByIdentifier(
              context,
              sizeFileLimitException.getStickerPackIdentifier(),
              sizeFileLimitException.getFileName());
        }
      }
    }

    return stickerPackList;
  }

  public static StickerPack fetchStickerPack(Context context, String stickerPackIdentifier)
      throws IllegalStateException {
    Uri uri = StickerContentProvider.getSingleStinckerPackUri(stickerPackIdentifier);
    Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

    if (cursor == null) {
      throw new IllegalStateException(
          "could not fetch from content provider, " + BuildConfig.CONTENT_PROVIDER_AUTHORITY);
    }

    final StickerPack stickerPack;

    if (cursor.moveToFirst()) {
      stickerPack = StickerPack.fromCursor(cursor);
    } else {
      cursor.close();
      throw new IllegalStateException("No sticker packs found in the content provider");
    }

    try {
      StickerPackValidator.verifyStickerPackValidity(context, stickerPack);

      for (Sticker sticker : stickerPack.getStickers()) {
        StickerValidator.verifyStickerValidity(
            context, stickerPack.identifier, sticker, stickerPack.animatedStickerPack);
      }

      stickerPack.setStickers(stickerPack.getStickers());
    } catch (IllegalStateException exception) {
      if (exception instanceof StickerFileException sizeFileLimitException) {
        // TODO: Trocar por método que vai marcar no banco de dados o pacote e figurinha.
        deleteStickerByIdentifier(
            context,
            sizeFileLimitException.getStickerPackIdentifier(),
            sizeFileLimitException.getFileName());
      }
    }

    return stickerPack;
  }

  /**
   * <b>Descrição:</b>De fato busca direto do content provider instanciando o .
   *
   * @param cursor Cursor com os pacotes de figurinhas.
   * @return Lista de pacotes de figurinhas.
   */
  @NonNull
  private static ArrayList<StickerPack> fetchListFromContentProvider(Cursor cursor) {
    ArrayList<StickerPack> stickerPackList = new ArrayList<>();
    cursor.moveToFirst();

    do {
      StickerPack stickerPack = StickerPack.fromCursor(cursor);
      stickerPackList.add(stickerPack);
    } while (cursor.moveToNext());

    return stickerPackList;
  }
}
