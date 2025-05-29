/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.domain.service.save;

import android.content.Context;
import android.util.JsonReader;
import android.util.Log;

import com.vinicius.sticker.core.exception.DeleteStickerException;
import com.vinicius.sticker.core.exception.PackValidatorException;
import com.vinicius.sticker.core.exception.StickerPackSaveException;
import com.vinicius.sticker.core.exception.base.InternalAppException;
import com.vinicius.sticker.domain.builder.JsonParserStickerPackBuilder;
import com.vinicius.sticker.domain.builder.StickerPackParserJsonBuilder;
import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.core.pattern.CallbackResult;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class StickerPackCreatorManager {

  private static final List<Sticker> stickers = new ArrayList<>();
  private static final String uuidPack = UUID.randomUUID().toString();

  @FunctionalInterface
  public interface JsonValidateCallback {
    void onJsonValidateDataComplete(String contentJson);
  }

  @FunctionalInterface
  public interface SavedStickerPackCallback {
    void onSavedStickerPack(CallbackResult<StickerPack> callbackResult);
  }

  public static void generateJsonPack(
      Context context,
      boolean isAnimatedPack,
      List<File> fileList,
      String namePack,
      JsonValidateCallback jsonValidateCallback,
      SavedStickerPackCallback savedStickerPackCallback) {
    try {
      stickers.clear();
      StickerPackParserJsonBuilder builder = new StickerPackParserJsonBuilder();

      builder
          .setIdentifier(uuidPack)
          .setName(namePack.trim())
          .setPublisher("vinicius")
          .setTrayImageFile("thumbnail.jpg")
          .setImageDataVersion("1")
          .setAvoidCache(false)
          .setPublisherWebsite("")
          .setPublisherEmail("")
          .setPrivacyPolicyWebsite("")
          .setLicenseAgreementWebsite("")
          .setAnimatedStickerPack(isAnimatedPack);

      for (File file : fileList) {
        boolean exists = false;
        for (Sticker sticker : stickers) {
          if (sticker.imageFileName.equals(file.getName())) {
            exists = true;
            break;
          }
        }

        if (!exists) {
          stickers.add(new Sticker(file.getName().trim(), "\uD83D\uDDFF", "Sticker pack"));
        }
      }

      for (Sticker sticker : stickers) {

        builder.addSticker(sticker.imageFileName, sticker.emojis, sticker.accessibilityText);
      }

      String contentJson = builder.build();
      try (JsonReader jsonReader = new JsonReader(new StringReader(contentJson))) {
        StickerPack stickerPack = JsonParserStickerPackBuilder.readStickerPack(jsonReader);

        if (jsonValidateCallback != null) {
          jsonValidateCallback.onJsonValidateDataComplete(contentJson);
        }

        if (stickerPack.identifier == null) {
          throw new DeleteStickerException("Erro ao encontrar o id do pacote para deletar.");
        }

        StickerPackSaveService.generateStructureForSavePack(
            context,
            stickerPack,
            stickerPack.identifier,
            callbackResult -> {
              switch (callbackResult.getStatus()) {
                case SUCCESS:
                  if (savedStickerPackCallback != null) {
                    savedStickerPackCallback.onSavedStickerPack(
                        CallbackResult.success(callbackResult.getData()));
                  } else {
                    Log.d("SaveStickerPack", "Callback não foi retornada corretamente!");
                  }
                  break;
                case WARNING:
                  Log.w("SaveStickerPack", callbackResult.getWarningMessage());
                  break;
                case DEBUG:
                  Log.d("SaveStickerPack", callbackResult.getDebugMessage());
                  break;
                case FAILURE:
                  if (callbackResult.getError()
                      instanceof StickerPackSaveException stickerPackSaveException) {
                    Log.e(
                        "SaveStickerPack",
                        stickerPackSaveException.getMessage() != null
                            ? stickerPackSaveException.getMessage()
                            : "Erro interno desconhecido!");
                    savedStickerPackCallback.onSavedStickerPack(
                        CallbackResult.failure(stickerPackSaveException));
                    break;
                  }

                  if (callbackResult.getError()
                      instanceof PackValidatorException packValidatorException) {
                    // NOTE: É garantido que não vai nulo, caso lançe
                    // nullpointer o erro é no código
                    // do projeto
                    Log.e(
                        "SaveStickerPack",
                        Objects.requireNonNull(packValidatorException.getMessage()));
                    // TODO: Caso receba essa exception aplicar tratamento para
                    // pacote invalido
                  }

                  if (callbackResult.getError()
                      instanceof StickerPackSaveException stickerPackSaveException) {
                    // NOTE: É garantido que não vai nulo, caso lançe
                    // nullpointer o erro é no código
                    // do projeto
                    Log.e(
                        "SaveStickerPack",
                        Objects.requireNonNull(stickerPackSaveException.getMessage()));
                    // TODO: Caso receba essa exception aplicar tratamento para
                    // pacote invalido
                    break;
                  }

                  savedStickerPackCallback.onSavedStickerPack(
                      CallbackResult.failure(
                          new InternalAppException("Erro interno desconhecido!")));
                  break;
              }
            });
      }
    } catch (JSONException | IOException exception) {
      savedStickerPackCallback.onSavedStickerPack(
          CallbackResult.failure(
              new InternalAppException(
                  exception.getMessage() != null
                      ? exception.getMessage()
                      : "Erro interno desconhecido!",
                  exception,
                  exception instanceof JSONException ? exception.getMessage() : null)));
    }
  }
}
