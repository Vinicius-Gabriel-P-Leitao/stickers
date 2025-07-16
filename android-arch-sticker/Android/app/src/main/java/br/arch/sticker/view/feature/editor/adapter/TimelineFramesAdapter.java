/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.editor.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Map;

import br.arch.sticker.R;
import br.arch.sticker.domain.util.ApplicationTranslate;
import br.arch.sticker.view.feature.editor.viewholder.FrameViewHolder;

public class TimelineFramesAdapter extends RecyclerView.Adapter<FrameViewHolder> {
    public interface OnFrameClickListener {
        void onFrameClick(int position);
    }

    private final static String TAG_LOG = TimelineFramesAdapter.class.getSimpleName();

    private final List<Bitmap> frames;
    private final OnFrameClickListener listener;
    private final ApplicationTranslate applicationTranslate;

    public TimelineFramesAdapter(Context context, List<Bitmap> frames, OnFrameClickListener listener) {
        this.frames = frames;
        this.listener = listener;
        this.applicationTranslate = new ApplicationTranslate(context.getResources());
    }

    @NonNull
    @Override
    public FrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.frames_timeline_video, parent, false);
        return new FrameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FrameViewHolder holder, int position) {
        Bitmap frame = frames.get(position);

        if (frame != null) {
            Glide.with(holder.imageView.getContext()).asBitmap().load(frame).centerCrop().into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.background_invalid_pack);
        }

        holder.imageView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onFrameClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return frames.size();
    }

    public void updateFrames(@NonNull Map<Integer, Bitmap> newFrames) {
        for (Map.Entry<Integer, Bitmap> entry : newFrames.entrySet()) {
            int index = entry.getKey();
            Bitmap frame = entry.getValue();

            if (index >= 0 && index < frames.size()) {
                frames.set(index, frame);
                notifyItemChanged(index);

                Log.d(TAG_LOG, applicationTranslate.translate(R.string.debug_log_frame_update_editor, index).get());
            } else {
                Log.w(TAG_LOG, applicationTranslate.translate(R.string.error_log_out_of_range_index).get());
            }
        }
    }
}
