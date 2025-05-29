/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.service.delete;

import static com.vinicius.sticker.domain.data.content.provider.StickerContentProvider.STICKERS_ASSET;
import static com.vinicius.sticker.domain.data.database.repository.DeleteStickerPacks.deleteSticker;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.vinicius.sticker.core.exception.DeleteStickerException;
import com.vinicius.sticker.domain.pattern.CallbackResult;
import java.io.File;
import java.sql.SQLException;
import java.util.Optional;

/** Deleta o sticker tanto no banco de dados quanto o arquivo. */
public class StickerDeleteService {

  /**
   * <b>Descrição:</b>Deleta o sticker tanto no banco de dados quanto o arquivo, os métadados são deletados via
   * repositório.
   *
   * @param context Contexto da aplicação.
   * @param stickerPackIdentifier Identificador do pacote que está o sticker.
   * @param fileName nome do arquivo.
   * @return Patter para resultado com booleano para retorno.
   */
  public static CallbackResult<Boolean> deleteStickerByIdentifier(
      @NonNull Context context, @NonNull String stickerPackIdentifier, @NonNull String fileName) {
    try {
      Optional<Integer> deletedSticker = deleteSticker(context, stickerPackIdentifier, fileName);
      CallbackResult<Boolean> deletedStickerFile = deleteFileSticker(context, stickerPackIdentifier, fileName);

      if (deletedStickerFile.isSuccess()) {
        deletedSticker
            .map(
                deleted -> {
                  if (deletedStickerFile.getData() && deleted > 0) {
                    Log.i("StickerDeleteService", "Sticker deletado com sucesso");
                    return CallbackResult.success(Boolean.TRUE);
                  } else {
                    return CallbackResult.warning("Nenhum sticker deletado para fileName: " + fileName);
                  }
                })
            .orElseGet(() -> CallbackResult.warning("Nenhum sticker deletado: Optional vazio"));
      }

      return CallbackResult.failure(
          deletedStickerFile.getError() != null
              ? deletedStickerFile.getError()
              : new DeleteStickerException("Erro ao deletar arquivo!"));

    } catch (SQLException | DeleteStickerException exception) {
      return CallbackResult.failure(
          new DeleteStickerException("Erro ao deletar métadados do sticker no  banco de dados!", exception.getCause()));
    }
  }

  /**
   * <b>Descrição:</b>Deleta arquivo da figurinha apenas um arquivo.
   *
   * @param context Contexto da aplicação.
   * @param stickerPackIdentifier Identificador do pacote que está sticker.
   * @param fileName nome do arquivo.
   * @return Patter para resultado com booleano para retorno.
   */
  private static CallbackResult<Boolean> deleteFileSticker(
      @NonNull Context context, @NonNull String stickerPackIdentifier, @NonNull String fileName) {
    File mainDirectory = new File(context.getFilesDir(), STICKERS_ASSET);
    File stickerDirectory = new File(mainDirectory, stickerPackIdentifier + File.separator + fileName);

    if (stickerDirectory.exists() && mainDirectory.exists()) {
      boolean deleted = stickerDirectory.delete();

      if (deleted) {
        Log.i("StickerDelete", "Arquivo deletado: " + stickerDirectory.getAbsolutePath());
        return CallbackResult.success(Boolean.TRUE);
      } else {
        return CallbackResult.failure(
            new DeleteStickerException("Falha ao deletar arquivo: " + stickerDirectory.getAbsolutePath()));
      }
    } else {
      return CallbackResult.failure(
          new DeleteStickerException("Arquivo não encontrado para deletar: " + stickerDirectory.getAbsolutePath()));
    }
  }

  /**
   * <b>Descrição:</b>Deleta todos os stickers de um pacote.
   *
   * @param context Contexto da aplicação.
   * @param stickerPackIdentifier Identificador do sticker.
   * @return Patter para resultado com booleano para retorno.
   */
  public static CallbackResult<Void> deleteAllFilesInPack(
      @NonNull Context context, @NonNull String stickerPackIdentifier) {
    File mainDirectory = new File(context.getFilesDir(), STICKERS_ASSET);
    File stickerPackDirectory = new File(mainDirectory, stickerPackIdentifier);

    if (stickerPackDirectory.exists() && stickerPackDirectory.isDirectory()) {
      File[] files = stickerPackDirectory.listFiles();
      if (files != null) {
        for (File file : files) {
          if (!file.delete()) {
            return CallbackResult.failure(
                new DeleteStickerException("Falha ao deletar o arquivo: " + file.getAbsolutePath()));
          }
        }
      }

      return CallbackResult.success(null);
    } else {
      return CallbackResult.warning("Diretório não encontrado: " + stickerPackDirectory.getAbsolutePath());
    }
  }
}
