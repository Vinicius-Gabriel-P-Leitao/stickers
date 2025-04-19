package com.whatsapp.sticker.ui.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.whatsapp.sticker.R;
import com.whatsapp.sticker.ui.adapter.PickMediaListAdapter;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class ImagePickerBottomSheet extends BottomSheetDialogFragment {
    private final List<String> imagePaths;
    private final PickMediaListAdapter.OnItemClickListener listener;

    public ImagePickerBottomSheet(List<String> imagePaths, PickMediaListAdapter.OnItemClickListener listener) {
        this.imagePaths = imagePaths;
        this.listener = listener;
    }

    @Override
    public void onStart() {
        super.onStart();

        View view = getView();
        if (view != null) {
            View parent = (View) view.getParent();


            BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(parent);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            parent.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            parent.requestLayout();
        }
    }

    @Override
    public int getTheme() {
        return R.style.TransparentBottomSheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_recyclerview_select_image, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3, RecyclerView.VERTICAL, false));

        PickMediaListAdapter adapter = new PickMediaListAdapter(getContext(), imagePaths, imagePath -> {
            listener.onItemClick(imagePath);
            dismiss();
        });

        recyclerView.setAdapter(adapter);

        Button selectButton = view.findViewById(R.id.select_medias_button);
        selectButton.setOnClickListener(buttonView -> {
            Set<String> selectedImagePaths = adapter.getSelectedImagePaths();

            if (selectedImagePaths.isEmpty()) {
                Toast.makeText(getContext(), "Nenhuma imagem selecionada", Toast.LENGTH_SHORT).show();
            } else {
                for (String path : selectedImagePaths) {
                    Log.d("Selecionado", path); // Todo: Onde o caminho imagens vem
                }
            }
        });
    }
}