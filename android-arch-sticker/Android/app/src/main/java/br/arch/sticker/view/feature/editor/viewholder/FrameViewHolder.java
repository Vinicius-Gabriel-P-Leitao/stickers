package br.arch.sticker.view.feature.editor.viewholder;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import br.arch.sticker.R;

public class FrameViewHolder extends RecyclerView.ViewHolder {
    public ImageView imageView;

    public FrameViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.image_frame);
    }
}
