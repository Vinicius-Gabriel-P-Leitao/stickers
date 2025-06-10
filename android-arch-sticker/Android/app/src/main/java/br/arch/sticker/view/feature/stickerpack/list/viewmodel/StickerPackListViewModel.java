/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.stickerpack.list.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import br.arch.sticker.domain.data.model.StickerPack;
import br.arch.sticker.view.feature.stickerpack.list.model.StickerPackListItem;

public class StickerPackListViewModel extends ViewModel {

    private final MutableLiveData<List<StickerPackListItem>> unifiedListLiveData = new MutableLiveData<>();

    private final MutableLiveData<StickerPackUiEvent> uiEventLiveData = new MutableLiveData<>();

    public LiveData<List<StickerPackListItem>> getUnifiedList() {
        return unifiedListLiveData;
    }

    public LiveData<StickerPackUiEvent> getUiEvent() {
        return uiEventLiveData;
    }

    public void setUnifiedList(List<StickerPackListItem> unifiedList) {
        unifiedListLiveData.setValue(unifiedList);
    }

    public void onAddButtonClicked(StickerPack stickerPack, boolean isValid) {
        if (isValid) {
            uiEventLiveData.setValue(new StickerPackUiEvent.AddStickerPackToWhatsApp(stickerPack.identifier, stickerPack.name));
        } else {
            uiEventLiveData.setValue(new StickerPackUiEvent.ShowInvalidStickerDialog(stickerPack));
        }
    }

    public static abstract class StickerPackUiEvent {
        public static class ShowInvalidStickerDialog extends StickerPackUiEvent {
            public final StickerPack stickerPack;

            public ShowInvalidStickerDialog(StickerPack stickerPack) {
                this.stickerPack = stickerPack;
            }
        }

        public static class AddStickerPackToWhatsApp extends StickerPackUiEvent {
            public final String identifier;
            public final String name;

            public AddStickerPackToWhatsApp(String identifier, String name) {
                this.identifier = identifier;
                this.name = name;
            }
        }
    }
}
