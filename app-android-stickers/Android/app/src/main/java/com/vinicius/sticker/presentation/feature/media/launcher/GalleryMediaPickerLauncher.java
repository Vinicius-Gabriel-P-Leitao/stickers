/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */

package com.vinicius.sticker.presentation.feature.media.launcher;

import static android.app.Activity.RESULT_OK;
import static com.vinicius.sticker.presentation.feature.media.util.CursorSearchUriMedia.getMediaUris;

import android.content.Intent;
import android.net.Uri;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.vinicius.sticker.domain.data.model.StickerPack;
import com.vinicius.sticker.presentation.feature.media.adapter.PickMediaListAdapter;
import com.vinicius.sticker.presentation.feature.media.fragment.MediaPickerBottomSheetDialogFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GalleryMediaPickerLauncher extends ViewModel {

    public static final String[] IMAGE_MIME_TYPES = {"image/jpeg", "image/png"};
    public static final String[] ANIMATED_MIME_TYPES = {"video/mp4", "image/gif"};

    public enum MediaType {
        IMAGE_MIME_TYPES, ANIMATED_MIME_TYPES
    }

    private final MutableLiveData<StickerPack> stickerPackPreview = new MutableLiveData<>();

    public LiveData<StickerPack> getStickerPackToPreview() {
        return stickerPackPreview;
    }

    private final MutableLiveData<Boolean> fragmentVisibility = new MutableLiveData<>(false);

    public LiveData<Boolean> getFragment() {
        return fragmentVisibility;
    }

    public void setStickerPackToPreview(StickerPack stickerPack) {
        stickerPackPreview.setValue(stickerPack);
    }

    public void openFragmentState() {
        fragmentVisibility.setValue(false);
    }

    public void closeFragmentState() {
        fragmentVisibility.setValue(true);
    }

    public static void launchOwnGallery(FragmentActivity activity, String[] mimeType, String namePack) {
        List<Uri> uris = new ArrayList<>();
        boolean isAnimatedPack = false;

        if (Arrays.equals(mimeType, IMAGE_MIME_TYPES)) {
            uris = getMediaUris(activity, IMAGE_MIME_TYPES);
        }

        if (Arrays.equals(mimeType, ANIMATED_MIME_TYPES)) {
            uris = getMediaUris(activity, ANIMATED_MIME_TYPES);
            isAnimatedPack = true;
        }

        MediaPickerBottomSheetDialogFragment fragment =
                MediaPickerBottomSheetDialogFragment.newInstance(new ArrayList<>(uris), namePack, isAnimatedPack, new PickMediaListAdapter.OnItemClickListener() {
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
