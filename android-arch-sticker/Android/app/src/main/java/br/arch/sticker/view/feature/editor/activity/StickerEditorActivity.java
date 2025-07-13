/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.editor.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.Locale;

import br.arch.sticker.R;
import br.arch.sticker.view.core.base.BaseActivity;
import br.arch.sticker.view.core.usecase.definition.MimeTypesSupported;
import br.arch.sticker.view.feature.editor.controller.GestureController;

public class StickerEditorActivity extends BaseActivity {
    private ExoPlayer player;
    private ImageView imageView;
    private View cropOverlayView;
    private SeekBar seekBarVideo;
    private PlayerView playerView;
    private TextView textSelectedTime;
    private LinearLayout videoControls;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_editor);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        imageView = findViewById(R.id.image_view);
        playerView = findViewById(R.id.player_view);
        cropOverlayView = findViewById(R.id.crop_overlay);
        videoControls = findViewById(R.id.video_player_controls);
        seekBarVideo = findViewById(R.id.seek_bar_video);
        textSelectedTime = findViewById(R.id.text_selected_time);

        Uri uri = getIntent().getData();
        if (uri == null) {
            Toast.makeText(this, "Nenhuma mídia recebida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        processUris(uri);
    }

    private void processUris(Uri uri) {
        try {
            String mimeType = getContentResolver().getType(uri);
            MimeTypesSupported mediaType = MimeTypesSupported.fromMimeType(mimeType);

            if (mediaType == null) {
                Toast.makeText(this, "Tipo não suportado: " + mimeType, Toast.LENGTH_SHORT).show();
                return;
            }

            switch (mediaType) {
                case IMAGE:
                    handleImage(uri);
                    break;
                case ANIMATED:
                    handleAnimated(uri, mimeType);
                    break;
            }
        } catch (IllegalArgumentException exception) {
            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleImage(Uri uri) {
        imageView.setVisibility(View.VISIBLE);
        playerView.setVisibility(View.GONE);
        videoControls.setVisibility(View.GONE);
        cropOverlayView.setVisibility(View.GONE);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            imageView.setImageBitmap(bitmap);
        } catch (IOException exception) {
            Toast.makeText(this, "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @OptIn(markerClass = UnstableApi.class)
    private void handleAnimated(Uri uri, String mimeType) {
        imageView.setVisibility(View.GONE);

        if ("video/mp4".equalsIgnoreCase(mimeType)) {
            videoControls.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.VISIBLE);
            cropOverlayView.setVisibility(View.VISIBLE);

            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);

            seekBarVideo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        player.seekTo(progress);
                        updateSelectedTimeText();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    player.setPlayWhenReady(false);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    player.setPlayWhenReady(true);
                }
            });

            TextureView textureView = (TextureView) playerView.getVideoSurfaceView();
            GestureController gestureController = new GestureController(textureView);
            textureView.setOnTouchListener((view, motionEvent) -> gestureController.onTouch(motionEvent));

            startVideoPlayback(uri);
        } else if ("image/gif".equalsIgnoreCase(mimeType)) {
            playerView.setVisibility(View.GONE);
            videoControls.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);

            Glide.with(this).asGif().load(uri).into(imageView);
        } else {
            Toast.makeText(this, "Formato animado não suportado: " + mimeType, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.setPlayWhenReady(false);
            playerView.setPlayer(null);
            player.release();
            player = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void startVideoPlayback(Uri uri) {
        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true);
        updateSeekBar();
    }

    private void updateSeekBar() {
        if (player != null) {
            seekBarVideo.setProgress((int) player.getCurrentPosition());
            updateSelectedTimeText();
            if (player.isPlaying()) {
                playerView.postDelayed(this::updateSeekBar, 1000);
            }
        }
    }

    private void updateSelectedTimeText() {
        if (player != null) {
            long currentPosition = player.getCurrentPosition() / 1000;
            long duration = player.getDuration() / 1000;
            textSelectedTime.setText(String.format(Locale.ROOT, "Selecionado: %ds - %ds", currentPosition, duration));
        }
    }
}