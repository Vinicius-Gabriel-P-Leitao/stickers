/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.feature.media.fragment;

import static com.vinicius.sticker.core.validation.StickerPackValidator.STICKER_SIZE_MIN;
import static com.vinicius.sticker.view.core.util.ConvertMediaToStickerFormat.convertMediaToWebP;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.vinicius.sticker.R;
import com.vinicius.sticker.core.exception.MediaConversionException;
import com.vinicius.sticker.core.exception.StickerPackSaveException;
import com.vinicius.sticker.core.exception.base.InternalAppException;
import com.vinicius.sticker.domain.orchestrator.StickerPackOrchestrator;
import com.vinicius.sticker.view.core.component.BottomFadingRecyclerView;
import com.vinicius.sticker.view.core.util.ConvertMediaToStickerFormat;
import com.vinicius.sticker.view.feature.media.adapter.PickMediaListAdapter;
import com.vinicius.sticker.view.feature.media.launcher.GalleryMediaPickerLauncher;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MediaPickerBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private static final String KEY_IS_ANIMATED = "key_is_animated";
    private static final String KEY_MEDIA_URIS = "key_media_uris";
    private static final String KEY_NAME_PACK = "key_name_pack";

    private final List<File> mediaConvertedFile = new ArrayList<>();
    private GalleryMediaPickerLauncher viewModel;
    private boolean isAnimatedPack;
    private List<Uri> mediaUris;
    private String namePack;

    private int completedConversions = 0;
    private int totalConversions = 0;
    private ProgressBar progressBar;

    ExecutorService executor = new ThreadPoolExecutor(5, 20, 1L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    private final Handler handler = new Handler(Looper.getMainLooper());

    private PickMediaListAdapter.OnItemClickListener listener;

    public MediaPickerBottomSheetDialogFragment() {
    }

    public static MediaPickerBottomSheetDialogFragment newInstance(
            ArrayList<Uri> mediaUris, String namePack, boolean isAnimatedPack, PickMediaListAdapter.OnItemClickListener listener) {
        MediaPickerBottomSheetDialogFragment fragment = new MediaPickerBottomSheetDialogFragment();
        Bundle args = new Bundle();

        args.putParcelableArrayList(KEY_MEDIA_URIS, mediaUris);
        args.putString(KEY_NAME_PACK, namePack);
        args.putBoolean(KEY_IS_ANIMATED, isAnimatedPack);

        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }

    private void setListener(PickMediaListAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mediaUris = getArguments().getParcelableArrayList(KEY_MEDIA_URIS);
            namePack = getArguments().getString(KEY_NAME_PACK);
            isAnimatedPack = getArguments().getBoolean(KEY_IS_ANIMATED);
        }
    }

    @Override
    public int getTheme() {
        return R.style.TransparentBottomSheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_recyclerview_select_media, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(GalleryMediaPickerLauncher.class);
        viewModel.getFragment().observe(
                getViewLifecycleOwner(), isVisible -> {
                    if (isVisible && isAdded() && getActivity() != null) {
                        handler.postDelayed(
                                () -> {
                                    if (isAdded() && getActivity() != null) {
                                        dismiss();
                                    }
                                }, 1500);
                    }
                });

        BottomFadingRecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        PickMediaListAdapter mediaListAdapter = new PickMediaListAdapter(
                getContext(), mediaUri -> {
            listener.onItemClick(mediaUri);
            dismiss();
        });
        mediaListAdapter.submitList(mediaUris);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mediaListAdapter);

        progressBar = view.findViewById(R.id.progress_bar_media);
        if (progressBar == null) {
            Log.e("MediaPickerFragment", "ProgressBar não encontrado!");
        } else {
            progressBar.setVisibility(View.GONE);
        }

        Button selectButton = view.findViewById(R.id.select_medias_button);
        selectButton.setOnClickListener(buttonView -> {
            Set<Uri> selectedMediaPaths = mediaListAdapter.getSelectedMediaPaths();

            if (selectedMediaPaths.size() >= STICKER_SIZE_MIN) {
                totalConversions = selectedMediaPaths.size();
                completedConversions = 0;
                progressBar.setVisibility(View.VISIBLE);

                for (Uri uri : selectedMediaPaths) {
                    convertMediaAndSaveAsync(uri);
                }
            } else {
                Toast.makeText(view.getContext(), "Numero minimo de itens selecionados, adicione no minimo 3!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void convertMediaAndSaveAsync(Uri uri) {
        if (uri == null || uri.getPath() == null) {
            throw new MediaConversionException("Caminho do arquivo  inválido ou caminho nulo!");
        }

        Context context = getContext();
        if (context == null) {
            Log.e("MediaPickerFragment", "Contexto nulo, não será possível converter mídia.");
            return;
        }

        executor.submit(() -> {
            convertMediaToWebP(
                    getContext(), uri, new File(uri.getPath()).getName(), new ConvertMediaToStickerFormat.MediaConversionCallback() {
                        @Override
                        public void onSuccess(File outputFile) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                mediaConvertedFile.add(outputFile);
                                checkAllConversionsCompleted();
                            });
                        }

                        @Override
                        public void onError(Exception exception) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                if (exception instanceof MediaConversionException) {
                                    Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Erro critico ao converter imagens!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
        });
    }

    private void checkAllConversionsCompleted() {
        completedConversions++;

        if (completedConversions == totalConversions) {
            progressBar.setVisibility(View.GONE);

            StickerPackOrchestrator.generateObjectToSave(
                    requireContext(), isAnimatedPack, mediaConvertedFile, namePack, callbackResult -> {
                        if (getContext() != null && isAdded()) {
                            switch (callbackResult.getStatus()) {
                                case SUCCESS:
                                    viewModel.setStickerPackToPreview(callbackResult.getData());
                                    viewModel.closeFragmentState();
                                    break;
                                case WARNING:
                                    Toast.makeText(getContext(), "Aviso: " + callbackResult.getWarningMessage(), Toast.LENGTH_SHORT).show();
                                    break;
                                case FAILURE:
                                    if (callbackResult.getError() instanceof InternalAppException exception) {
                                        Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                                        break;
                                    }

                                    Toast.makeText(getContext(), callbackResult.getError().getMessage(), Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        } else {
                            Log.e("MediaPickerFragment", "Fragment ou Contexto não estão mais válidos.");
                            throw new StickerPackSaveException("Fragment ou Contexto não estão mais válidos.");
                        }
                    });
        }
    }
}