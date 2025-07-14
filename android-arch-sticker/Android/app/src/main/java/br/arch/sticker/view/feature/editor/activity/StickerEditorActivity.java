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
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
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
import br.arch.sticker.view.core.usecase.component.RangeTimelineOverlayView;
import br.arch.sticker.view.core.usecase.definition.MimeTypesSupported;
import br.arch.sticker.view.feature.editor.adapter.TimelineFramesAdapter;
import br.arch.sticker.view.feature.editor.controller.GestureController;

public class StickerEditorActivity extends BaseActivity {
    private final static String TAG_LOG = StickerEditorActivity.class.getSimpleName();

    private RangeTimelineOverlayView rangeTimeline;
    private RecyclerView recyclerTimeline;
    private TextureView textureView;
    private FrameLayout timeline;
    private TextView timelineTitle;
    private PlayerView playerView;
    private Button buttonConfirm;
    private ImageView imageView;
    private ExoPlayer player;

    private String mediaWidth;
    private String mediaHeight;
    private long loopStartMs = 0;
    private long videoDurationMs;
    private long loopDurationMs = 5000;
    private long loopEndMs = loopDurationMs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sticker_editor);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        final Uri uri = getIntent().getData();
        if (uri == null) {
            Toast.makeText(this, "Nenhuma mídia recebida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        timeline = findViewById(R.id.timeline);
        imageView = findViewById(R.id.image_view);
        playerView = findViewById(R.id.player_view);
        rangeTimeline = findViewById(R.id.range_timeline);
        timelineTitle = findViewById(R.id.timeline_title);
        buttonConfirm = findViewById(R.id.button_confirm);

        recyclerTimeline = findViewById(R.id.recycler_timeline);
        recyclerTimeline.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        buttonConfirm.setOnClickListener(view -> {
            Rect crop = getCropRectFromTransformedTexture();
            if (crop != null) {
                Log.d("CROP", "Recorte real do vídeo: " + crop.toShortString());
            } else {
                Toast.makeText(this, "Erro ao calcular recorte", Toast.LENGTH_SHORT).show();
            }
        });

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable loopChecker = new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying()) {
                    long currentPos = player.getCurrentPosition();
                    if (currentPos >= loopEndMs) {
                        player.seekTo(loopStartMs);
                    }
                }

                handler.postDelayed(this, 100);
            }
        };

        handler.post(loopChecker);

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
        timeline.setVisibility(View.GONE);

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            mediaWidth = String.valueOf(bitmap.getWidth());
            mediaHeight = String.valueOf(bitmap.getHeight());

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
            timeline.setVisibility(View.VISIBLE);
            playerView.setVisibility(View.VISIBLE);

            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);

            textureView = (TextureView) playerView.getVideoSurfaceView();
            GestureController gestureController = new GestureController(textureView); // TODO FAZER COM SEGURANÇA
            textureView.setOnTouchListener((view, motionEvent) -> gestureController.onTouch(motionEvent));

            startVideoPlayback(uri);
        } else if ("image/gif".equalsIgnoreCase(mimeType)) {
            playerView.setVisibility(View.GONE);
            timeline.setVisibility(View.GONE);
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
                onRangeChanged();
            }
        });

        recyclerTimeline.setAdapter(adapter);
        recyclerTimeline.post(this::onRangeChanged);
    }

    private List<Bitmap> extractFrames(Uri videoUri, int frameCount, Context context) {
        List<Bitmap> frames = new ArrayList<>();

        try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            retriever.setDataSource(context, videoUri);

            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            videoDurationMs = Long.parseLong(durationStr); // TODO: FAZER COM MAIS SEGURANÇA

            mediaWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            mediaHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

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
        rangeTimeline.setOnRangeChangeListener(seconds -> {
            runOnUiThread(() -> {
                float aroundSeconds = Math.round(seconds * 10) / 10f;

                timelineTitle.setText(aroundSeconds + "s");

                loopDurationMs = (long) (aroundSeconds * 1000);
                loopEndMs = Math.min(videoDurationMs, loopStartMs + loopDurationMs);
                onRangeChanged();
            });
        });
    }

    public void onRangeChanged() {
        int scrollOffsetPx = recyclerTimeline.computeHorizontalScrollOffset();

        float startSeconds = rangeTimeline.getStartSeconds(scrollOffsetPx);
        float endSeconds = rangeTimeline.getEndSeconds(scrollOffsetPx);

        loopStartMs = (long) (startSeconds * 1000);
        loopEndMs = (long) (endSeconds * 1000);

        loopStartMs = Math.max(0, Math.min(loopStartMs, videoDurationMs));
        loopEndMs = Math.max(loopStartMs, Math.min(loopEndMs, videoDurationMs));

        if (player != null) {
            player.seekTo(loopStartMs);
        }
    }

    public @Nullable Rect getCropRectFromTransformedTexture() {
        View cropArea = findViewById(R.id.crop_area);

        if (textureView == null || cropArea == null || mediaWidth == null || mediaHeight == null) return null;

        int videoWidth = Integer.parseInt(mediaWidth);
        int videoHeight = Integer.parseInt(mediaHeight);

        Matrix transformMatrix = textureView.getTransform(null);

        Rect cropScreenRect = new Rect();
        cropArea.getGlobalVisibleRect(cropScreenRect);

        Rect textureScreenRect = new Rect();
        textureView.getGlobalVisibleRect(textureScreenRect);

        RectF cropRectInTexture = new RectF(
                cropScreenRect.left - textureScreenRect.left,
                cropScreenRect.top - textureScreenRect.top,
                cropScreenRect.right - textureScreenRect.left,
                cropScreenRect.bottom - textureScreenRect.top);

        Matrix inverseMatrix = new Matrix();
        if (!transformMatrix.invert(inverseMatrix)) return null;

        inverseMatrix.mapRect(cropRectInTexture);

        float scaleX = (float) videoWidth / textureView.getWidth();
        float scaleY = (float) videoHeight / textureView.getHeight();

        int left = Math.round(cropRectInTexture.left * scaleX);
        int top = Math.round(cropRectInTexture.top * scaleY);
        int width = Math.round(cropRectInTexture.width() * scaleX);
        int height = Math.round(cropRectInTexture.height() * scaleY);

        return new Rect(left, top, left + width, top + height);
    }
}