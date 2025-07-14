/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.editor.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.MultiTransformation;

import java.util.List;

import br.arch.sticker.R;
import br.arch.sticker.view.core.util.transformation.CropSquareTransformation;
import br.arch.sticker.view.feature.editor.viewholder.FrameViewHolder;

public class TimelineFramesAdapter extends RecyclerView.Adapter<FrameViewHolder> {
    private final List<Bitmap> frames;
    private final OnFrameClickListener listener;

    public interface OnFrameClickListener {
        void onFrameClick(int position);
    }

    public TimelineFramesAdapter(List<Bitmap> frames, OnFrameClickListener listener) {
        this.frames = frames;
        this.listener = listener;
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

        RequestManager glide = Glide.with(holder.imageView.getContext());
        MultiTransformation<Bitmap> commonTransform = new MultiTransformation<>(new CropSquareTransformation(20f, 0, R.color.catppuccin_overlay2));
        RequestBuilder<Bitmap> requestBuilder = glide.asBitmap().load(frame);
        requestBuilder.centerCrop().transform(commonTransform).into(holder.imageView);

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
}
