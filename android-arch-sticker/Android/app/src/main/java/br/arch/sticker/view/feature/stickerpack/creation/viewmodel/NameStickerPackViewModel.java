/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.stickerpack.creation.viewmodel;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import br.arch.sticker.view.feature.stickerpack.creation.fragment.NameStickerPackFragment;

public class NameStickerPackViewModel extends ViewModel {
    private final MutableLiveData<String> nameStickerPack = new MutableLiveData<>();

    public LiveData<String> getNameStickerPack() {
        return nameStickerPack;
    }

    public void setNameStickerPack(String name) {
        nameStickerPack.setValue(name);
    }

    public static void launchNameStickerPack(FragmentActivity activity) {
        NameStickerPackFragment nameStickerPackFragment = new NameStickerPackFragment();
        nameStickerPackFragment.show(activity.getSupportFragmentManager(), NameStickerPackFragment.class.getSimpleName());
    }
}
