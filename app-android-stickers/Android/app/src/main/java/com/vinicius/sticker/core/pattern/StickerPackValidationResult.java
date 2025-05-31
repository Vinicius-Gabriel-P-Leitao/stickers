/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.core.pattern;

import com.vinicius.sticker.domain.data.model.Sticker;
import com.vinicius.sticker.domain.data.model.StickerPack;

import java.util.ArrayList;

// @formatter:off
public class StickerPackValidationResult {
    public record ListStickerPackResult(ArrayList<StickerPack> validStickerPacks, ArrayList<StickerPack> invalidStickerPacks, ArrayList<Sticker> invalidStickers) {
    }

    public record StickerPackResult(StickerPack validStickerPacks, Sticker invalidStickers) {
    }
}