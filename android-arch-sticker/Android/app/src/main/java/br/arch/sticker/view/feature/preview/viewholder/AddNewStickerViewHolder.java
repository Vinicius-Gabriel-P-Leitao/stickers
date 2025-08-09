package br.arch.sticker.view.feature.preview.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import br.arch.sticker.R;

public class AddNewStickerViewHolder extends RecyclerView.ViewHolder {
    public final MaterialButton materialButton;

    public AddNewStickerViewHolder(@NonNull View itemView) {
        super(itemView);
        materialButton = itemView.findViewById(R.id.button_add_sticker);
    }
}
