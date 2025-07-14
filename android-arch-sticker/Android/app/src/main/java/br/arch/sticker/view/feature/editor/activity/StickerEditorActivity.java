/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.editor.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.arch.sticker.R;
import br.arch.sticker.view.core.base.BaseActivity;
import br.arch.sticker.view.core.usecase.component.RangeSelectorOverlayView;
import br.arch.sticker.view.core.usecase.definition.MimeTypesSupported;
import br.arch.sticker.view.feature.editor.adapter.TimelineFramesAdapter;
import br.arch.sticker.view.feature.editor.controller.GestureController;

public class StickerEditorActivity extends BaseActivity {
    private final static String TAG_LOG = StickerEditorActivity.class.getSimpleName();

    private RangeSelectorOverlayView rangeSelector;
    private RecyclerView recyclerTimeline;
    private FrameLayout videoControls;
    private PlayerView playerView;
    private View cropOverlayView;
    private ImageView imageView;
    private ExoPlayer player;

    private long videoDurationMs;

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
        rangeSelector = findViewById(R.id.range_selector);

        recyclerTimeline = findViewById(R.id.recycler_timeline);
        recyclerTimeline.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        Uri uri = getIntent().getData();
        if (uri == null) {
            Toast.makeText(this, "Nenhuma mídia recebida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        getVideoSeconds();
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

            TextureView textureView = (TextureView) playerView.getVideoSurfaceView();
            GestureController gestureController = new GestureController(textureView); // TODO FAZER COM SEGURANÇA
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

        List<Bitmap> frames = extractFrames(uri, 15, this);

        TimelineFramesAdapter adapter = new TimelineFramesAdapter(frames, position -> {
            long seekMs = (position * 1000L);
            if (player != null) {
                player.seekTo(seekMs);
            }
        });

        recyclerTimeline.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int offset = recyclerView.computeHorizontalScrollOffset();
                int range = recyclerView.computeHorizontalScrollRange() - recyclerView.computeHorizontalScrollExtent();

                if (range > 0) {
                    float scrollPercent = (float) offset / range;

                    long seekMs = (long) (videoDurationMs * scrollPercent);

                    if (player != null) {
                        player.seekTo(seekMs);
                    }
                }
            }
        });

        recyclerTimeline.setAdapter(adapter);
    }

    private List<Bitmap> extractFrames(Uri videoUri, int frameCount, Context context) {
        List<Bitmap> frames = new ArrayList<>();

        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(context, videoUri);

            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            videoDurationMs = Long.parseLong(durationStr); // TODO: FAZER COM MAIS SEGURANÇA
            long interval = videoDurationMs / frameCount;

            for (int counter = 0; counter < frameCount; counter++) {
                long timeUs = counter * interval * 1000;
                Bitmap frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                if (frame != null) {
                    frames.add(frame);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return frames;
    }

    public void getVideoSeconds() {
        rangeSelector.setOnRangeChangeListener(seconds -> {
            runOnUiThread(() -> {
                // TODO FAZER FORMA DE ARMAZENAR ISSO PARA PEGAR A AREA DE CROP DO VIDEO
                Log.d(TAG_LOG, "Segundos: " + seconds);
            });
        });
    }
}