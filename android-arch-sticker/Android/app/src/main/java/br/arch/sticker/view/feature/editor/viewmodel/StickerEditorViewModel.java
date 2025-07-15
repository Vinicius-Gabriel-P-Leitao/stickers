/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.editor.viewmodel;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StickerEditorViewModel extends AndroidViewModel {
    private static final String TAG_LOG = StickerEditorViewModel.class.getSimpleName();

    private final Context context;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final MutableLiveData<String> mediaWidth = new MutableLiveData<>();
    private final MutableLiveData<String> mediaHeight = new MutableLiveData<>();
    private final MutableLiveData<Long> videoDurationMsLiveData = new MutableLiveData<>();
    public final MutableLiveData<Map<Integer, Bitmap>> extractFrameResult = new MutableLiveData<>();

    public StickerEditorViewModel(@NonNull Application application) {
        super(application);
        this.context = getApplication().getApplicationContext();
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

    public void loadVideoMetadata(Uri videoUri) {
        executorService.submit(() -> {
            try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                retriever.setDataSource(context, videoUri);

                String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                String widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                String heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

                if (durationStr != null) {
                    long duration = Long.parseLong(durationStr);
                    Log.d(TAG_LOG, "Video duration: " + durationStr);
                    videoDurationMsLiveData.postValue(duration);
                } else {
                    Log.w(TAG_LOG, "Video duration unknown.");
                }

                Log.d(TAG_LOG, "Video mediaWidth: " + mediaWidth + "Video mediaHeight: " + mediaHeight);
                if (widthStr != null) mediaWidth.postValue(widthStr);
                if (heightStr != null) mediaHeight.postValue(heightStr);

            } catch (Exception exception) {
                Log.e(TAG_LOG, "Failed to load video metadata", exception);
            }
        });
    }

    public void extractFramesInWindow(Uri videoUri, long startMs, long windowDurationMs, int framesPerSecond) {
        executorService.submit(() -> {
            Map<Integer, Bitmap> extractedFrames = new HashMap<>();
            long endMs = startMs + windowDurationMs;

            Long videoDurationMs = videoDurationMsLiveData.getValue();
            if (videoDurationMs == null || videoDurationMs == 0L) {
                Log.w(TAG_LOG, "Video duration unknown, aborting frame extraction");
                return;
            }

            try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                retriever.setDataSource(context, videoUri);

                if (endMs > videoDurationMs) {
                    endMs = videoDurationMs;
                }

                long intervalMs = 1000 / framesPerSecond;
                for (long timeMs = startMs; timeMs < endMs; timeMs += intervalMs) {
                    int frameIndex = (int) ((timeMs - startMs) * framesPerSecond / 1000);

                    Log.d(TAG_LOG, ">> Extracting frame at index " + frameIndex + " (timeMs=" + timeMs + ")");
                    Bitmap frame = retriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    if (frame != null) {
                        extractedFrames.put(frameIndex, frame);
                    }
                }

            } catch (Exception exception) {
                Log.e(TAG_LOG, "Error extracting frames", exception);
            }

            extractFrameResult.postValue(extractedFrames);
            Log.d(TAG_LOG, ">> Extracted " + extractedFrames.size() + " frames");
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdownNow();
    }
}

