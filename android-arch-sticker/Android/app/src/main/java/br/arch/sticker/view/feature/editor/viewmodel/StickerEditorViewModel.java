/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.editor.viewmodel;

import static br.arch.sticker.core.validation.StickerValidator.IMAGE_HEIGHT;
import static br.arch.sticker.core.validation.StickerValidator.IMAGE_WIDTH;
import static br.arch.sticker.view.feature.editor.activity.StickerEditorActivity.FRAMES_PER_SECOND;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import br.arch.sticker.R;
import br.arch.sticker.core.lib.NativeCropMedia;
import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.domain.util.ApplicationTranslate.LoggableString.Level;
import br.arch.sticker.view.core.util.convert.ConvertMediaToStickerFormat;
import br.arch.sticker.view.core.util.resolver.FileDetailsResolver;

public class StickerEditorViewModel extends AndroidViewModel {
    private static final String TAG_LOG = StickerEditorViewModel.class.getSimpleName();

    private final Context context;
    private final FileDetailsResolver fileDetailsResolver;
    private final ApplicationTranslate applicationTranslate;

    private int nextFrameIndex = 0;
    private final int batchSize = 5;
    private MediaMetadataRetriever retriever;
    private volatile boolean isExtracting = false;
    private final Map<Integer, Bitmap> cachedFrames = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final MutableLiveData<String> mediaWidth = new MutableLiveData<>();
    private final MutableLiveData<String> mediaHeight = new MutableLiveData<>();
    private final MutableLiveData<File> fileConverted = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();
    private final MutableLiveData<Long> videoDurationMsLiveData = new MutableLiveData<>();
    public final MutableLiveData<Map<Integer, Bitmap>> extractFrameResult = new MutableLiveData<>();

    public StickerEditorViewModel(@NonNull Application application) {
        super(application);
        this.context = getApplication().getApplicationContext();
        this.fileDetailsResolver = new FileDetailsResolver(this.context);
        this.applicationTranslate = new ApplicationTranslate(this.context.getResources());
    }

    public MutableLiveData<String> getMediaWidth() {
        return mediaWidth;
    }

    public MutableLiveData<String> getMediaHeight() {
        return mediaHeight;
    }

    public MutableLiveData<File> getFileConverted() {
        return fileConverted;
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
        executor.submit(() -> {
            try (MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
                retriever.setDataSource(context, videoUri);

                String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                String widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                String heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

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
                errorMessageLiveData.postValue(applicationTranslate.translate(R.string.error_failed_load_video_metadata).log(TAG_LOG, Level.ERROR, exception).get());
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

            extractFramesInWindow(startMs, windowDurationMs, nextFrameIndex, batchSize);

            nextFrameIndex += batchSize;

        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopIncrementalExtraction();
        executor.shutdownNow();
    }

    private void extractFramesInWindow(long startMs, long windowDurationMs, int frameOffset, int framesPerBatch) {
        executor.submit(() -> {
            Map<Integer, Bitmap> extractedFrames = new HashMap<>();
            long endMs = startMs + windowDurationMs;

            final Long videoDurationMs = videoDurationMsLiveData.getValue();
            if (videoDurationMs == null || videoDurationMs == 0L) {
                errorMessageLiveData.postValue(applicationTranslate.translate(R.string.error_invalid_timeline_duration).log(TAG_LOG, Level.ERROR).get());
                return;
            }

            try {
                if (endMs > videoDurationMs) {
                    endMs = videoDurationMs;
                }

                final long intervalMs = 1000 / FRAMES_PER_SECOND;
                final long maxTimeMs = Math.min(endMs, startMs + framesPerBatch * intervalMs);

                for (long timeMs = startMs; timeMs < maxTimeMs; timeMs += intervalMs) {
                    final int frameIndex = (int) (timeMs - startMs) * FRAMES_PER_SECOND / 1000;
                    final int globalIndex = frameOffset + frameIndex;

                    if (cachedFrames.containsKey(globalIndex)) {
                        continue;
                    }

                    Log.d(TAG_LOG, applicationTranslate.translate(R.string.debug_extracting_frame, globalIndex, timeMs).get());
                    Bitmap frame = retriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    if (frame != null) {
                        extractedFrames.put(globalIndex, frame);
                    }
                }
            } catch (Exception exception) {
                errorMessageLiveData.postValue(applicationTranslate.translate(R.string.error_extracting_frames).log(TAG_LOG, Level.ERROR, exception).get());
            }

            if (!extractedFrames.isEmpty()) {
                cachedFrames.putAll(extractedFrames);
                extractFrameResult.postValue(extractedFrames);
                Log.d(TAG_LOG, applicationTranslate.translate(R.string.debug_extracted_frames).get());
            }
        });
    }

    private void stopIncrementalExtraction() {
        isExtracting = false;
        scheduler.shutdown();
    }

    public void createCroppedBitmap(Uri uri, Rect cropRect) {
        if (uri == null || cropRect == null) return;

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);

            Bitmap croppedBitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(croppedBitmap);
            canvas.drawColor(Color.BLACK);

            float dx = (IMAGE_WIDTH / 2f) - (cropRect.width() / 2f);
            float dy = (IMAGE_HEIGHT / 2f) - (cropRect.height() / 2f);

            Matrix drawMatrix = new Matrix();
            drawMatrix.postTranslate(dx - cropRect.left, dy - cropRect.top);
            canvas.drawBitmap(bitmap, drawMatrix, null);

            String inputFile = fileDetailsResolver.getAbsolutePath(uri);
            String inputFileName = new File(inputFile).getName();

            String outputFile = new File(context.getCacheDir(), inputFileName).getAbsolutePath();
            String finalOutputFileName = ConvertMediaToStickerFormat.ensureWebpExtension(outputFile);

            File fileToSave = new File(finalOutputFileName);

            try (FileOutputStream fileOutputStream = new FileOutputStream(fileToSave)) {
                croppedBitmap.compress(Bitmap.CompressFormat.WEBP, 100, fileOutputStream);
                fileOutputStream.flush();

                fileConverted.postValue(fileToSave);
            }
        } catch (IOException exception) {
            errorMessageLiveData.postValue(applicationTranslate.translate(R.string.error_conversion_failed).log(TAG_LOG, Level.ERROR).get());
        }
    }

    public void createCroppedNative(Uri uri, int x, int y, int width, int height, float startSeconds, float endSeconds) {
        executor.submit(() -> {
            String inputFile = fileDetailsResolver.getAbsolutePath(uri);
            String inputFileName = new File(inputFile).getName();

            String outputFile = new File(context.getCacheDir(), inputFileName).getAbsolutePath();
            String finalOutputFileName = ConvertMediaToStickerFormat.ensureWebpExtension(outputFile);

            NativeCropMedia nativeCropMedia = new NativeCropMedia(context.getResources());
            nativeCropMedia.processWebpAsync(inputFile, finalOutputFileName, x, y, width, height, startSeconds, endSeconds, new NativeCropMedia.CropCallback() {
                @Override
                public void onSuccess(File file) {
                    fileConverted.postValue(file);
                }

                @Override
                public void onError(Exception exception) {
                    errorMessageLiveData.postValue(applicationTranslate.translate(R.string.error_conversion_failed).log(TAG_LOG, Level.ERROR, exception).get());
                }
            });
        });
    }

}

