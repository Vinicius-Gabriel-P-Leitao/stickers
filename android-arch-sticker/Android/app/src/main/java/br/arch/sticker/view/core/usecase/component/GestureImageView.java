/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.usecase.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatImageView;

import br.arch.sticker.view.feature.editor.controller.GestureController;

public class GestureImageView extends AppCompatImageView {
    private GestureController gestureController;

    public GestureImageView(Context context) {
        super(context);
        init();
    }

    public GestureImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GestureImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void init() {
        gestureController = new GestureController(this);

        setOnTouchListener((view, event) -> {
            if (gestureController.onTouch(event)) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    performClick();
                }
                return true;
            }
            return false;
        });
    }
}

