/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.feature.editor.controller;

import android.graphics.Matrix;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import br.arch.sticker.R;

public class GestureController {
    private final static String TAG_LOG = GestureController.class.getSimpleName();

    private final View view;
    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleDetector;

    private float translateX = 0f;
    private float translateY = 0f;
    private float scaleFactor = 1.0f;
    private float minScaleFactor = 1.0f;

    public GestureController(View view) {
        this.view = view;

        scaleDetector = new ScaleGestureDetector(view.getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(1.0f, Math.min(scaleFactor, 5.0f));

                applyMatrix();
                return true;
            }
        }
        );

        gestureDetector = new GestureDetector(view.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent motionEvent1, @NonNull MotionEvent motionEvent2, float distanceX, float distanceY) {
                translateX -= distanceX;
                translateY -= distanceY;

                applyMatrix();
                return true;
            }
        }
        );
    }

    public boolean onTouch(MotionEvent event) {
        boolean handled = scaleDetector.onTouchEvent(event);
        handled = gestureDetector.onTouchEvent(event) || handled;

        return handled;
    }

    private void applyMatrix() {
        Matrix matrix = new Matrix();
        matrix.postScale(scaleFactor, scaleFactor, view.getWidth() / 2f, view.getHeight() / 2f);
        matrix.postTranslate(translateX, translateY);

        if (view instanceof TextureView textureView) {
            textureView.setTransform(matrix);
            return;
        }

        if (view instanceof ImageView imageView) {
            imageView.setImageMatrix(matrix);
            return;
        }

        Log.e(TAG_LOG, view.getContext().getString(R.string.warn_unsupported_view_transformation));
    }
}

