package br.arch.sticker.view.feature.editor.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import br.arch.sticker.R;
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
        holder.imageView'   .setImageBitmap(frame);

        holder.imageView.setOnClickListener(v -> {
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
