/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.core.component;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.vinicius.sticker.R;

public class FormatStickerPopupWindow {
    public interface OnOptionClickListener {
        void onStaticStickerSelected();

        void onAnimatedStickerSelected();
    }

    public static void popUpButtonChooserStickerModel(@NonNull Context context, @NonNull View anchorView, @NonNull OnOptionClickListener listener) {
        View popupView = LayoutInflater.from(context).inflate(R.layout.dropdown_custom_menu, null);
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setElevation(12f);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.menu_dropdown_background));
        popupWindow.setAnimationStyle(R.style.PopupBounceAnimation);

        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);

        int popupHeight = popupView.getMeasuredHeight();
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());
        int yPosition = location[1] - popupHeight - margin;

        popupWindow.showAtLocation(anchorView, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, yPosition);

        popupView.findViewById(R.id.item_option_static).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onStaticStickerSelected();
                popupWindow.dismiss();
            }
        });

        popupView.findViewById(R.id.item_option_animated).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onAnimatedStickerSelected();
                popupWindow.dismiss();
            }
        });
    }

}
