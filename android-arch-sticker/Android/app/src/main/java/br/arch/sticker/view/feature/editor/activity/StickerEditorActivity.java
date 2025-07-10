/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.editor.activity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;

import java.io.IOException;

import br.arch.sticker.R;
import br.arch.sticker.view.core.base.BaseActivity;

public class StickerEditorActivity extends BaseActivity {
    public final static int REQUEST_CODE_MEDIA = 1001;

    private ImageView imageView;
    private VideoView videoView;
    private LinearLayout videoControls;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_editor);

        imageView = findViewById(R.id.image_view);
        videoView = findViewById(R.id.video_view);
        videoControls = findViewById(R.id.video_player_controls);

        Uri mediaUri = getIntent().getData();
        if (mediaUri != null) {
            String mimeType = getContentResolver().getType(mediaUri);

            if (mimeType != null) {
                if (mimeType.startsWith("image/")) {
                    setupImage(mediaUri);
                } else if (mimeType.startsWith("video/")) {
                    setupVideo(mediaUri);
                } else {
                    Toast.makeText(this, "Tipo de mídia não suportado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private void setupImage(Uri uri) {
        imageView.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
        videoControls.setVisibility(View.GONE);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupVideo(Uri uri) {
        videoView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        videoControls.setVisibility(View.VISIBLE);

        videoView.setVideoURI(uri);
        videoView.seekTo(1);
        videoView.setOnPreparedListener(mp -> {
            int duration = mp.getDuration();
        });
    }
}