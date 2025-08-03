/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import java.util.Arrays;

public class ApplicationTranslate {
    private final Resources resources;

    public ApplicationTranslate(Resources resources) {
        this.resources = resources;
    }

    public LoggableString translate(@StringRes int resId, Object... args) {
        String message = resources.getString(resId, args);
        return new LoggableString(message);
    }

    public static LoggableString translate(Context context, @StringRes int resId, Object... args) {
        String message = context.getString(resId, args);
        return new LoggableString(message);
    }

    public static class LoggableString {
        public enum Level {
            VERBOSE(1), DEBUG(2), INFO(3), WARN(4), ERROR(5), ASSERT(6);

            private final int level;

            Level(int level) {
                this.level = level;
            }

            public int getLevel() {
                return level;
            }
        }

        private final String message;

        private LoggableString(String message) {
            this.message = message;
        }

        public LoggableString log(String TAG_LOG, Object... args) {
            Log.d(TAG_LOG, message + Arrays.toString(args));
            return this;
        }

        public LoggableString log(String TAG_LOG, Level priority, Object... args) {
            Throwable throwable = null;
            StringBuilder msgBuilder = new StringBuilder();

            for (Object arg : args) {
                if (arg instanceof Throwable firstThrowable && throwable == null) {
                    throwable = firstThrowable;
                } else {
                    msgBuilder.append(arg).append(' ');
                }
            }

            String message = msgBuilder.toString().trim();

            switch (priority) {
                case VERBOSE:
                    Log.v(TAG_LOG, message, throwable);
                    break;
                case INFO:
                    Log.i(TAG_LOG, message, throwable);
                    break;
                case WARN:
                    Log.w(TAG_LOG, message, throwable);
                    break;
                case ERROR:
                    Log.e(TAG_LOG, message, throwable);
                    break;
                default:
                    Log.d(TAG_LOG, message, throwable);
                    break;
            }

            return this;
        }

        public String get() {
            return message;
        }
    }
}