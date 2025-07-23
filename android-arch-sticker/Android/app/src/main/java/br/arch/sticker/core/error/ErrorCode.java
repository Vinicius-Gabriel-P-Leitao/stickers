/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.error;

import androidx.annotation.Nullable;

import br.arch.sticker.R;

// @formatter: off
public enum ErrorCode {
    ERROR_CURSOR_NULL(R.string.error_cursor_invalid_fields),
    ERROR_CONTENT_PROVIDER(R.string.error_content_provider),
    ERROR_EMPTY_STICKERPACK(R.string.error_empty_stickerpack),
    ERROR_PACK_DELETE_DB(R.string.error_delete_stickerpack_db),
    ERROR_STICKER_DELETE_DB(R.string.error_delete_sticker_list_db),
    ERROR_PACK_DELETE_SERVICE(R.string.error_delete_stickerpack_service),
    ERROR_PACK_DELETE_UTIL(R.string.error_delete_stickerpack_util),
    ERROR_PACK_DELETE_UI(R.string.error_delete_stickerpack_ui),
    INVALID_IDENTIFIER(R.string.error_invalid_identifier),
    DUPLICATE_IDENTIFIER(R.string.error_duplicate_identifier),
    INVALID_PUBLISHER(R.string.error_invalid_publisher),
    INVALID_STICKERPACK_NAME(R.string.error_invalid_pack_name),
    INVALID_STICKERPACK_SIZE(R.string.error_invalid_stickerpack_size),
    INVALID_THUMBNAIL(R.string.error_invalid_thumbnail),
    INVALID_ANDROID_URL_SITE(R.string.error_invalid_android_url),
    INVALID_IOS_URL_SITE(R.string.error_invalid_ios_url),
    INVALID_WEBSITE(R.string.error_invalid_website),
    INVALID_EMAIL(R.string.error_invalid_email),
    INVALID_STICKER_ACCESSIBILITY(R.string.error_accessibility_text_length),
    ERROR_FILE_SIZE(R.string.error_exceeded_file_size),
    ERROR_SIZE_STICKER(R.string.error_invalid_sticker_size),
    ERROR_STICKER_TYPE(R.string.error_sticker_type_mismatch),
    ERROR_STICKER_DURATION(R.string.error_animated_sticker_duration),
    ERROR_FILE_TYPE(R.string.error_unsupported_file_type),
    INVALID_STICKER_PATH(R.string.error_invalid_sticker_filename),
    ERROR_UNKNOWN(R.string.error_unknown),
    ERROR_BASE_ACTIVITY(R.string.error_base_activity),
    ERROR_OPERATION_NOT_POSSIBLE(R.string.error_operation_failed),
    STICKER_FILE_NOT_EXIST(R.string.error_file_not_found),
    ERROR_PACK_SAVE_DB(R.string.error_save_stickerpack_db),
    ERROR_PACK_SAVE_SERVICE(R.string.error_save_stickerpack_service),
    ERROR_PACK_SAVE_UTIL(R.string.error_save_stickerpack_util),
    ERROR_PACK_SAVE_UI(R.string.error_save_stickerpack_ui),
    ERROR_PACK_SAVE_THUMBNAIL(R.string.error_save_thumbnail),
    ERROR_NATIVE_CONVERSION(R.string.error_native_conversion),
    ERROR_PACK_CONVERSION_MEDIA(R.string.error_media_conversion),
    INVALID_URL(R.string.error_invalid_url);

    private final int message;

    ErrorCode(int message) {
        this.message = message;
    }

    public int getMessageResId() {
        return message;
    }

    @Nullable
    public static ErrorCode fromName(@Nullable String name) {
        if (name == null) return null;

        try {
            return ErrorCode.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
