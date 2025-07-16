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
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.arch.sticker.R;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.view.core.base.BaseActivity;
import br.arch.sticker.view.core.usecase.component.RangeTimelineOverlayView;
import br.arch.sticker.view.core.usecase.definition.MimeTypesSupported;
import br.arch.sticker.view.feature.editor.adapter.TimelineFramesAdapter;
import br.arch.sticker.view.feature.editor.controller.GestureController;
import br.arch.sticker.view.feature.editor.viewmodel.StickerEditorViewModel;
import br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;

public class StickerEditorActivity extends BaseActivity {
    private final static String TAG_LOG = StickerEditorActivity.class.getSimpleName();
    public final static int FRAMES_PER_SECOND = 1;

    private StickerEditorViewModel stickerEditorViewModel;
    private ApplicationTranslate applicationTranslate;
    private RangeTimelineOverlayView rangeTimeline;
    private RecyclerView recyclerTimeline;
    private TextureView textureView;
    private FrameLayout timeline;
    private TextView timelineTitle;
    private PlayerView playerView;
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

        applicationTranslate = new ApplicationTranslate(getResources());
        stickerEditorViewModel = new ViewModelProvider(this).get(StickerEditorViewModel.class);

        final Uri uri = getIntent().getData();
        if (uri == null) {
            Toast.makeText(this, getString(R.string.error_message_no_media_received), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        timeline = findViewById(R.id.timeline);
        imageView = findViewById(R.id.image_view);
        playerView = findViewById(R.id.player_view);
        rangeTimeline = findViewById(R.id.range_timeline);
        timelineTitle = findViewById(R.id.timeline_title);
        Button buttonConfirm = findViewById(R.id.button_confirm);

        recyclerTimeline = findViewById(R.id.recycler_timeline);
        recyclerTimeline.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        buttonConfirm.setOnClickListener(view -> {
            Rect crop = getCropRectFromTransformedTexture();
            if (crop != null) {
                Log.d(TAG_LOG, getString(R.string.debug_log_actual_video_crop, crop.toShortString()));
            } else {
                Toast.makeText(this, getString(R.string.error_message_calculation_clipping), Toast.LENGTH_SHORT).show();
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

        processUris(uri);
        getVideoSeconds();
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

    private void processUris(Uri uri) {
        try {
            String mimeType = getContentResolver().getType(uri);
            MimeTypesSupported mediaType = MimeTypesSupported.fromMimeType(mimeType);

            if (mediaType == null) {
                Toast.makeText(this, getString(R.string.error_message_unsupported_mimetype, mimeType), Toast.LENGTH_SHORT).show();
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

            GestureController gestureController = new GestureController(imageView);
            imageView.setOnTouchListener((view, motionEvent) -> gestureController.onTouch(motionEvent));

            imageView.setImageBitmap(bitmap);
        } catch (IOException exception) {
            Toast.makeText(this, getString(R.string.error_message_error_loading_image), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @OptIn(markerClass = UnstableApi.class)
    private void handleAnimated(Uri uri, String mimeType) {
        imageView.setVisibility(View.GONE);

        if ("video/mp4".equalsIgnoreCase(mimeType)) {
            playerView.setVisibility(View.VISIBLE);
            timeline.setVisibility(View.VISIBLE);

            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);

            textureView = (TextureView) playerView.getVideoSurfaceView();
            if (textureView == null) {
                Toast.makeText(this, getString(R.string.error_message_invalid_view_render_video), Toast.LENGTH_SHORT).show();
                return;
            }

            GestureController gestureController = new GestureController(textureView);
            textureView.setOnTouchListener((view, motionEvent) -> gestureController.onTouch(motionEvent));

            startVideoTimeline(uri);
            return;
        }

        if ("image/gif".equalsIgnoreCase(mimeType)) {
            imageView.setVisibility(View.VISIBLE);

            playerView.setVisibility(View.GONE);
            timeline.setVisibility(View.GONE);

            Glide.with(this).asGif().load(uri).into(imageView);
            return;
        }

        Toast.makeText(this, getString(R.string.error_message_unsupported_mimetype, mimeType), Toast.LENGTH_SHORT).show();
    }

    private void startVideoTimeline(Uri videoUri) {
        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlayWhenReady(true);

        stickerEditorViewModel.loadVideoMetadata(videoUri);

        stickerEditorViewModel.getMediaWidth().observe(this, width -> {
            if (width != null) {
                mediaWidth = width;
            }
        });

        stickerEditorViewModel.getMediaHeight().observe(this, height -> {
            if (height != null) {
                mediaHeight = height;
            }
        });

        stickerEditorViewModel.getVideoDurationMsLiveData().observe(this, duration -> {
            if (duration != null) {
                videoDurationMs = duration;
            }

            if (videoDurationMs == 0) {
                    Toast.makeText(this, applicationTranslate.translate(R.string.error_log_timeline_invalid_duration).log(TAG_LOG, Level.ERROR).get(), Toast.LENGTH_SHORT).show();
                return;
            }

            int totalFrames = (int) (videoDurationMs / 1000f * FRAMES_PER_SECOND);
            final List<Bitmap> frames = new ArrayList<>(Collections.nCopies(totalFrames, null));

            TimelineFramesAdapter adapter = new TimelineFramesAdapter(this, frames, position -> {
                long seekMs = (position * 1000L) / FRAMES_PER_SECOND;
                if (player != null) {
                    player.seekTo(seekMs);
                }
            });

            recyclerTimeline.setAdapter(adapter);
            recyclerTimeline.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    onTimelineChanged();
                }
            });

            stickerEditorViewModel.startIncrementalExtraction(videoUri);
            stickerEditorViewModel.getExtractFrameResult().observe(this, adapter::updateFrames);
        });

        recyclerTimeline.post(this::onTimelineChanged);
    }

    private void getVideoSeconds() {
        rangeTimeline.setOnRangeChangeListener(seconds -> {
            runOnUiThread(() -> {
                float aroundSeconds = Math.round(seconds * 10) / 10f;

                timelineTitle.setText(getString(R.string.timeline_title_seconds, String.valueOf(aroundSeconds)));

                loopDurationMs = (long) (aroundSeconds * 1000);
                loopEndMs = Math.min(videoDurationMs, loopStartMs + loopDurationMs);
                onTimelineChanged();
            });
        });
    }

    private void onTimelineChanged() {
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

    private @Nullable Rect getCropRectFromTransformedTexture() {
        View cropArea = findViewById(R.id.crop_area);

        if (textureView == null || cropArea == null || mediaWidth == null || mediaHeight == null)
            return null;

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