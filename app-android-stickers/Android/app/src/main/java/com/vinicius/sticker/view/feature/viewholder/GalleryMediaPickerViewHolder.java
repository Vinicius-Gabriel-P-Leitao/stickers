/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.feature.viewholder;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.view.core.util.CursorSearchUriMedia;
import com.vinicius.sticker.view.feature.adapter.PickMediaListAdapter;
import com.vinicius.sticker.view.feature.presentation.fragment.MediaPickerBottomFragment;
import com.vinicius.sticker.view.feature.usecase.MimeTypesSupported;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// @formatter:off
public class GalleryMediaPickerViewHolder extends ViewModel {
    // NOTE: Pacote para o preview na activity
    private final MutableLiveData<StickerPack> stickerPackPreview = new MutableLiveData<>();
    public LiveData<StickerPack> getStickerPackToPreview() {
        return stickerPackPreview;
    }

    public void setStickerPackToPreview(StickerPack stickerPack) { stickerPackPreview.setValue(stickerPack); }

    // NOTE: Estado do fragment
    private final MutableLiveData<Boolean> fragmentVisibility = new MutableLiveData<>(false);

    public LiveData<Boolean> getFragment() { return fragmentVisibility; }

    public void openFragmentState() {
        fragmentVisibility.setValue(false);
    }

    public void closeFragmentState() {
        fragmentVisibility.setValue(true);
    }

    public static void launchOwnGallery(FragmentActivity activity, String[] mimeType, String namePack) {
        boolean isAnimatedPack = Arrays.equals(mimeType,MimeTypesSupported.ANIMATED.getMimeTypes());
        List<Uri> uris = CursorSearchUriMedia.fetchMediaUri(activity, mimeType);


        MediaPickerBottomFragment fragment = MediaPickerBottomFragment.newInstance(
                new ArrayList<>(uris), namePack, isAnimatedPack, new PickMediaListAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(String imagePath) {
                        Uri selectedImageUri = Uri.fromFile(new File(imagePath));
                        Intent resultIntent = new Intent();
                        resultIntent.setData(selectedImageUri);

                        activity.setResult(RESULT_OK, resultIntent);
                        activity.finish();
                    }
                });

        fragment.show(activity.getSupportFragmentManager(), "MediaPickerBottomSheetDialogFragment");
    }
}
