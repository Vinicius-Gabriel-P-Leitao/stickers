/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.preview.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import br.arch.sticker.core.error.ErrorCodeProvider;
import br.arch.sticker.core.error.code.FetchErrorCode;
import br.arch.sticker.core.error.code.InvalidUrlErrorCode;
import br.arch.sticker.core.error.code.StickerPackErrorCode;
import br.arch.sticker.domain.data.model.StickerPack;

public class PreviewInvalidStickerPackViewModel extends ViewModel {
    public sealed interface FixActionStickerPack permits FixActionStickerPack.NewThumbnail {
        record NewThumbnail(StickerPack stickerPack) implements FixActionStickerPack {
        }
    }

    private final MutableLiveData<FixActionStickerPack> stickerPackMutableLiveData = new MutableLiveData<>();

    public LiveData<FixActionStickerPack> getStickerPackMutableLiveData()
        {
            return stickerPackMutableLiveData;
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
            }

            if (errorCode.equals(StickerPackErrorCode.INVALID_STICKERPACK_SIZE)) {
                // O pacote tem mais figurinhas no banco do que nos arquivos, provavelmente erro de validação, fazer validação novamente e dar o
                // erro correto, caso não tenha pedir para deletar stickers sobressalentes
            }

            if (errorCode.equals(StickerPackErrorCode.INVALID_THUMBNAIL)) {
                // Gerar nova thumbnail apartir dos arquivos do pacote, o nome da thumbainail é padrão então pode ser com qualquer arquivo valido
                stickerPackMutableLiveData.setValue(new FixActionStickerPack.NewThumbnail(stickerPack));
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
}
