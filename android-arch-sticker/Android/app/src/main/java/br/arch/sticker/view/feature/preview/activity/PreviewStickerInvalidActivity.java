/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.preview.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.view.core.base.BaseActivity;

public class PreviewStickerInvalidActivity extends BaseActivity {
    private final static String TAG_LOG = PreviewStickerInvalidActivity.class.getSimpleName();

    public static final String EXTRA_INVALID_STICKER_PACK = "invalid_sticker_pack";
    public static final String EXTRA_INVALID_STICKER_LIST = "invalid_sticker_list";

    // TODO: Fazer activity que vai renderizar os stickers invalidos e dar a opção para refatorar

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String stickerPackIdentifier = getIntent().getStringExtra(EXTRA_INVALID_STICKER_PACK);
        ArrayList<Sticker> stickerArrayList = getIntent().getParcelableArrayListExtra(EXTRA_INVALID_STICKER_LIST);

        if (stickerPackIdentifier != null) Log.d(TAG_LOG, stickerPackIdentifier);
        if (stickerArrayList != null) Log.d(TAG_LOG, stickerArrayList.toString());
    }
}
