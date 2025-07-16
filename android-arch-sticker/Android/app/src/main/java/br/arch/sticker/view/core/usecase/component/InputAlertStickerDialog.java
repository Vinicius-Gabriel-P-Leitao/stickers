/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.usecase.component;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;

import br.arch.sticker.R;

public class InputAlertStickerDialog extends Dialog {
    private TextView titleTextView;
    private TextView messageTextView;
    private TextInputEditText textInputEditText;
    private Button fixButton;
    private ImageView iconImageView;

    public InputAlertStickerDialog(@NonNull Context context)
        {
            super(context, R.style.AlterDialogStyle);
            init();
        }

    private void init()
        {
            setContentView(R.layout.dialog_alert_input);
            setCancelable(true);

            titleTextView = findViewById(R.id.dialog_title);
            messageTextView = findViewById(R.id.dialog_message);
            textInputEditText = findViewById(R.id.et_user_input);
            fixButton = findViewById(R.id.button_fix_operation);
            iconImageView = findViewById(R.id.dialog_icon);
        }

    public void setTitleText(String title)
        {
            titleTextView.setText(title);
        }

    public void setMessageText(String message)
        {
            messageTextView.setText(message);
        }

    public String getUserInput()
        {
            return textInputEditText.getText() != null
                   ? textInputEditText.getText().toString().trim()
                   : "";
        }

    public void showError(String errorMessage)
        {
            textInputEditText.setError(errorMessage);
        }

    public void setTextInput(String text)
        {
            textInputEditText.setHint(text);
        }

    public void setTextFixButton(String text)
        {
            fixButton.setText(text);
        }

    public void setVisibilityFixButton(int text)
        {
            fixButton.setVisibility(text);
        }

    public void setVisibilityIgnoreButton(int text)
        {
            textInputEditText.setVisibility(text);
        }

    public void setOnFixClick(View.OnClickListener listener)
        {
            fixButton.setOnClickListener(listener);
        }

    public void setIconImageView(int icon)
        {
            iconImageView.setImageResource(icon);
        }
}
