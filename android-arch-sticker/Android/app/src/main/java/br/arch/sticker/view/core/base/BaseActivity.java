/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * Modifications by Vinícius, 2025
 * Licensed under the Vinícius Non-Commercial Public License (VNCL)
 */

package br.arch.sticker.view.core.base;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import br.arch.sticker.core.exception.base.AppCoreStateException;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    public static final class MessageDialogFragment extends DialogFragment {
        private static final String ARG_TITLE_ID = "title_id";
        private static final String ARG_MESSAGE = "message";

        public static DialogFragment newInstance(
                @StringRes int titleId, String message) {
            DialogFragment fragment = new MessageDialogFragment();

            Bundle arguments = new Bundle();
            arguments.putInt(ARG_TITLE_ID, titleId);
            arguments.putString(ARG_MESSAGE, message);
            fragment.setArguments(arguments);

            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            if (args == null) {
                throw new AppCoreStateException("Arguments não podem ser nulos", "ERROR_BASE_ACTIVITY");
            }

            @StringRes final int title = args.getInt(ARG_TITLE_ID, 0);
            String message = getArguments().getString(ARG_MESSAGE);

            Activity activity = getActivity();
            if (activity == null) {
                throw new AppCoreStateException("Arguments não podem ser nulos", "ERROR_BASE_ACTIVITY");
            }

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity()).setMessage(message)
                            .setCancelable(true)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> dismiss());

            if (title != 0) {
                dialogBuilder.setTitle(title);
            }

            return dialogBuilder.create();
        }
    }
}
