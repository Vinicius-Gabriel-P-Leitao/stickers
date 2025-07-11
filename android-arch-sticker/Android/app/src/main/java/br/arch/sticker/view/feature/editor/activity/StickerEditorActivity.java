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

import com.bumptech.glide.Glide;

import java.io.IOException;

import br.arch.sticker.R;
import br.arch.sticker.view.core.base.BaseActivity;
import br.arch.sticker.view.core.usecase.definition.MimeTypesSupported;

public class StickerEditorActivity extends BaseActivity {
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

        Uri uriList = getIntent().getData();
        if (uriList == null) {
            Toast.makeText(this, "Nenhuma mídia recebida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        processUris(uriList);
    }

    private void processUris(Uri uri) {
        try {
            String type = getContentResolver().getType(uri);
            MimeTypesSupported mediaType = MimeTypesSupported.fromMimeType(type);

            if (mediaType == null) {
                Toast.makeText(this, "Tipo não suportado: " + type, Toast.LENGTH_SHORT).show();
                return;
            }

            switch (mediaType) {
                case IMAGE:
                    handleImage(uri);
                    break;
                case ANIMATED:
                    handleAnimated(uri, type);
                    break;
            }
        } catch (IllegalArgumentException exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImage(Uri uri) {
        imageView.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
        videoControls.setVisibility(View.GONE);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            imageView.setImageBitmap(bitmap);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void handleAnimated(Uri uri, String mimeType) {
        imageView.setVisibility(View.GONE);

        if ("video/mp4".equalsIgnoreCase(mimeType)) {
            videoView.setVisibility(View.VISIBLE);
            videoControls.setVisibility(View.VISIBLE);

            videoView.bringToFront();
            videoView.invalidate();
            videoView.setVideoURI(uri);
            videoView.seekTo(1);
            videoView.setOnPreparedListener(mp -> {
                int duration = mp.getDuration();
                videoView.seekTo(100);
                videoView.start();
            });
        } else if ("image/gif".equalsIgnoreCase(mimeType)) {
            videoView.setVisibility(View.GONE);
            videoControls.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);

            Glide.with(this).asGif().load(uri).into(imageView);
        }
    }
}