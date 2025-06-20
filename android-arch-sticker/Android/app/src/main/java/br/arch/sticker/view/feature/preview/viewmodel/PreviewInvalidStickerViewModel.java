/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.preview.viewmodel;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import br.arch.sticker.core.error.ErrorCodeProvider;
import br.arch.sticker.core.error.code.FetchErrorCode;
import br.arch.sticker.core.error.code.InvalidUrlErrorCode;
import br.arch.sticker.core.error.code.StickerAssetErrorCode;
import br.arch.sticker.core.error.code.StickerPackErrorCode;
import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;

public class PreviewInvalidStickerViewModel extends AndroidViewModel {
    private final MutableLiveData<FixAction> fixActionLiveData = new MutableLiveData<>();

    public PreviewInvalidStickerViewModel(@NonNull Application application)
        {
            super(application);
        }

    private Context getAppContext()
        {
            return getApplication().getApplicationContext();
        }

    public LiveData<FixAction> getFixActionLiveData()
        {
            return fixActionLiveData;
        }

    public void handleFixStickerClick(Sticker sticker)
        {
            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.STICKER_FILE_NOT_EXIST.name())) {
                // deletar sticker do banco
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.INVALID_STICKER_FILENAME.name())) {
                // Caso o nome do arquivo seja invalido, deletar
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_FILE_SIZE.name())) {
                // Tamanho em kv passou do permitido tentar rodar um script que vai tentar diminuir arquivo
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_SIZE_STICKER.name())) {
                // chamar método a ser feito que vai tentar fazer um resize forçado para 512x512
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_STICKER_TYPE.name())) {
                // deletar sticker provavelmente é um statico em pacote animado
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_STICKER_DURATION.name())) {
                // Tempo da animção muito grande, quase impossivel de acontecer porem pode ocorrer
            }

            if (TextUtils.equals(sticker.stickerIsValid, StickerAssetErrorCode.ERROR_FILE_TYPE.name())) {
                // O arquivo é diferente de .webp
            }
        }

    public void handleFixStickerPackClick(StickerPack stickerPack, ErrorCodeProvider errorCode)
        {
            String stickerPackIdentifier = stickerPack.identifier;

            if (errorCode.equals(FetchErrorCode.ERROR_EMPTY_STICKERPACK)) {
                // O pacote é vazio, deletar ele
            }

            if (errorCode.equals(FetchErrorCode.ERROR_CONTENT_PROVIDER)) {
                // Erro bem geral pode lançar vários tipos de erro, validar quais podem ser tratados se não dar opção de deletar
            }

            if (errorCode.equals(InvalidUrlErrorCode.INVALID_URL)) {
                // URL do pacote invalida, colocar uma vázia no lugar, impossivel de ocorrer mas a ser tratada
            }

            if (errorCode.equals(StickerPackErrorCode.INVALID_IDENTIFIER)) {
                // Erro de identificador do stickerpack bem dificil de ocorrer por ser um uuid gen4, mas dar suporte a gerar outro
            }

            if (errorCode.equals(StickerPackErrorCode.DUPLICATE_IDENTIFIER)) {
                // Mesma coisa do de cima, porem talvez nem chegue aqui
            }

            if (errorCode.equals(StickerPackErrorCode.INVALID_PUBLISHER)) {
                // Mesmo caso de INVALID_URL
            }

            if (errorCode.equals(StickerPackErrorCode.INVALID_STICKERPACK_NAME)) {
                // Pedir o usuário para inserir um novo nome
                fixActionLiveData.setValue(new FixAction.Rename(stickerPack));
            }

            if (errorCode.equals(StickerPackErrorCode.INVALID_STICKERPACK_SIZE)) {
                // O pacote tem mais figurinhas no banco do que nos arquivos, provavelmente erro de validação, fazer validação novamente e dar o
                // erro correto, caso não tenha pedir para deletar stickers sobressalentes
            }

            if (errorCode.equals(StickerPackErrorCode.INVALID_THUMBNAIL)) {
                // Gerar nova thumbnail apartir dos arquivos do pacote, o nome da thumbainail é padrão então pode ser com qualquer arquivo valido
            }

            if (errorCode.equals(StickerPackErrorCode.INVALID_ANDROID_URL_SITE)) {
                // Mesmo caso de INVALID_URL
            }

            if (errorCode.equals(StickerPackErrorCode.INVALID_IOS_URL_SITE)) {
                // Mesmo caso de INVALID_URL
            }

            if (errorCode.equals(StickerPackErrorCode.INVALID_WEBSITE)) {
                // Mesmo caso de INVALID_URL
            }

            if (errorCode.equals(StickerPackErrorCode.INVALID_EMAIL)) {
                // Mesmo caso de INVALID_URL
            }

            if (errorCode.equals(StickerPackErrorCode.INVALID_STICKER_ACCESSIBILITY)) {
                // Mesmo caso de INVALID_URL
            }
        }

    public sealed interface FixAction permits FixAction.Rename, FixAction.Delete, FixAction.Generic {
        record Rename(StickerPack stickerPack) implements FixAction {
        }

        record Delete(StickerPack stickerPack) implements FixAction {
        }

        record Generic(String message) implements FixAction {
        }
    }
}
