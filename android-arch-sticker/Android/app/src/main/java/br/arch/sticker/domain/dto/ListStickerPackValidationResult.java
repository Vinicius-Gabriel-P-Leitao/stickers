/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.domain.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.arch.sticker.domain.data.model.Sticker;
import br.arch.sticker.domain.data.model.StickerPack;

public record ListStickerPackValidationResult(ArrayList<StickerPack> validPacks, ArrayList<StickerPack> invalidPacks,
                                              HashMap<StickerPack, List<Sticker>> validPacksWithInvalidStickers) {
}
