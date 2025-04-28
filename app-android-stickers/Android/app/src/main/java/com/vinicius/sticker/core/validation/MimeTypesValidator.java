/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 *
 * This is **not an official GNU license**, and it is **not endorsed by the Free Software Foundation (FSF)**.
 * This license incorporates and modifies portions of the GNU GPLv3 to add a non-commercial use clause.
 *
 * Original GPLv3 license text begins below.
 */

package com.vinicius.sticker.core.validation;

import android.util.Log;

import java.util.Arrays;
import java.util.Objects;

public class MimeTypesValidator {

   public static boolean validateArraysMimeTypes(String[] mimeTypes, String[] staticMimeTypes) {
      for (String type : staticMimeTypes) {
         Log.d("MimeTypeCheck", "Comparando MIME: " + Arrays.toString(mimeTypes) + " com " + type);
         if ( Arrays.equals(mimeTypes, staticMimeTypes) ) {
            return true;
         }
      }
      return false;
   }

   public static boolean validateUniqueMimeType(String mimeType, String[] mimeTypesList) {
      for (String type : mimeTypesList) {
         Log.d("MimeTypeCheck", "Comparando MIME: " + mimeType + " com " + type);
         if ( Objects.equals(mimeType, type) ) {
            return true;
         }
      }
      return false;
   }
}
