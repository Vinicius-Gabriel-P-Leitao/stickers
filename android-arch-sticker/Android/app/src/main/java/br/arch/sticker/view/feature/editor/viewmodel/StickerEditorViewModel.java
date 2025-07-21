/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.editor.viewmodel;

import static br.arch.sticker.view.feature.editor.activity.StickerEditorActivity.FRAMES_PER_SECOND;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import br.arch.sticker.R;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;

public class StickerEditorViewModel extends AndroidViewModel {
    private static final String TAG_LOG = StickerEditorViewModel.class.getSimpleName();

    private final Context context;
    private final ApplicationTranslate applicationTranslate;

    private int nextFrameIndex = 0;
    private final int batchSize = 5;
    private MediaMetadataRetriever retriever;
    private volatile boolean isExtracting = false;
    private final Map<Integer, Bitmap> cachedFrames = new ConcurrentHashMap<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final MutableLiveData<String> mediaWidth = new MutableLiveData<>();
    private final MutableLiveData<String> mediaHeight = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Long> videoDurationMsLiveData = new MutableLiveData<>();
    public final MutableLiveData<Map<Integer, Bitmap>> extractFrameResult = new MutableLiveData<>();

    public StickerEditorViewModel(@NonNull Application application) {
        super(application);
        this.context = getApplication().getApplicationContext();
        this.applicationTranslate = new ApplicationTranslate(context.getResources());
    }

    public MutableLiveData<String> getMediaWidth() {
        return mediaWidth;
    }

    public MutableLiveData<String> getMediaHeight() {
        return mediaHeight;
    }

    public MutableLiveData<Long> getVideoDurationMsLiveData() {
        return videoDurationMsLiveData;
    }

    public MutableLiveData<Map<Integer, Bitmap>> getExtractFrameResult() {
        return extractFrameResult;
    }

    public MutableLiveData<String> getErrorMessageLiveData() {
        return errorMessageLiveData;
    }

    public void loadVideoMetadata(Uri videoUri) {
        executorService.submit(() -> {
            try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                retriever.setDataSource(context, videoUri);

                String durationStr = retriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_DURATION);
                String widthStr = retriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                String heightStr = retriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

                if (durationStr == null) {
                    Log.w(TAG_LOG, applicationTranslate.translate(R.string.error_unknown).get());
                    return;
                }

                if (widthStr == null) {
                    Log.w(TAG_LOG, applicationTranslate.translate(R.string.error_unknown).get());
                    return;
                }

                if (heightStr == null) {
                    Log.w(TAG_LOG, applicationTranslate.translate(R.string.error_unknown).get());
                }

                videoDurationMsLiveData.postValue(Long.parseLong(durationStr));
                mediaWidth.postValue(widthStr);
                mediaHeight.postValue(heightStr);
            } catch (Exception exception) {
                errorMessageLiveData.postValue(
                        applicationTranslate.translate(R.string.error_failed_load_video_metadata)
                                .log(TAG_LOG, Level.ERROR, exception).get());
            }
        });
    }

    public void startIncrementalExtraction(Uri videoUri) {
        if (isExtracting) return;

        isExtracting = true;
        nextFrameIndex = 0;

        retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, videoUri);

        scheduler.scheduleWithFixedDelay(() -> {
                    if (!isExtracting) {
                        scheduler.shutdown();
                        return;
                    }

                    final Long videoDuration = videoDurationMsLiveData.getValue();
                    if (videoDuration == null || videoDuration == 0L) return;

                    int totalFrames = (int) ((videoDuration / 1000f) * FRAMES_PER_SECOND);

                    if (nextFrameIndex >= totalFrames) {
                        isExtracting = false;
                        scheduler.shutdown();
                        return;
                    }

                    long startMs = (nextFrameIndex * 1000L) / FRAMES_PER_SECOND;
                    long windowDurationMs = batchSize * (1000L / FRAMES_PER_SECOND);

                    extractFramesInWindow(startMs, windowDurationMs, FRAMES_PER_SECOND, nextFrameIndex,
                            batchSize
                    );

                    nextFrameIndex += batchSize;

                }, 0, 500, TimeUnit.MILLISECONDS
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopIncrementalExtraction();
        executorService.shutdownNow();
    }

    private void extractFramesInWindow(long startMs, long windowDurationMs, int framesPerSecond, int frameOffset, int framesPerBatch) {
        executorService.submit(() -> {
            Map<Integer, Bitmap> extractedFrames = new HashMap<>();
            long endMs = startMs + windowDurationMs;

            final Long videoDurationMs = videoDurationMsLiveData.getValue();
            if (videoDurationMs == null || videoDurationMs == 0L) {
                errorMessageLiveData.postValue(
                        applicationTranslate.translate(R.string.error_invalid_timeline_duration)
                                .log(TAG_LOG, Level.ERROR).get());
                return;
            }

            try {
                if (endMs > videoDurationMs) {
                    endMs = videoDurationMs;
                }

                final long intervalMs = 1000 / framesPerSecond;
                final long maxTimeMs = Math.min(endMs, startMs + framesPerBatch * intervalMs);

                for (long timeMs = startMs; timeMs < maxTimeMs; timeMs += intervalMs) {
                    final int frameIndex = (int) (timeMs - startMs) * framesPerSecond / 1000;
                    final int globalIndex = frameOffset + frameIndex;

                    if (cachedFrames.containsKey(globalIndex)) {
                        continue;
                    }

                    Log.d(TAG_LOG, applicationTranslate.translate(R.string.debug_extracting_frame,
                                    globalIndex, timeMs
                            ).get()
                    );
                    Bitmap frame = retriever.getFrameAtTime(timeMs * 1000,
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    );
                    if (frame != null) {
                        extractedFrames.put(globalIndex, frame);
                    }
                }
            } catch (Exception exception) {
                errorMessageLiveData.postValue(
                        applicationTranslate.translate(R.string.error_extracting_frames)
                                .log(TAG_LOG, Level.ERROR, exception).get());
            }

            if (!extractedFrames.isEmpty()) {
                cachedFrames.putAll(extractedFrames);
                extractFrameResult.postValue(extractedFrames);
                Log.d(TAG_LOG,
                        applicationTranslate.translate(R.string.debug_extracted_frames).get()
                );
            }
        });
    }

    private void stopIncrementalExtraction() {
        isExtracting = false;
        scheduler.shutdown();
    }
}

