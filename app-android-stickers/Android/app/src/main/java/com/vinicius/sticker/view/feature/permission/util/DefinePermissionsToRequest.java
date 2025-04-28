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
package com.vinicius.sticker.view.feature.permission.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class DefinePermissionsToRequest {
   public static String[] getPermissionsToRequest(
       @NonNull Context context
   ) {
      List<String> permissionsNeeded = new ArrayList<>();

      if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ) {
         if ( ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) !=
             PackageManager.PERMISSION_GRANTED ) {
            permissionsNeeded.add(Manifest.permission.READ_MEDIA_VIDEO);
         }

         if ( ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) !=
             PackageManager.PERMISSION_GRANTED ) {
            permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
         }
      } else {
         if ( ContextCompat.checkSelfPermission(context,
                                                Manifest.permission.READ_EXTERNAL_STORAGE
         ) != PackageManager.PERMISSION_GRANTED ) {
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
         }

         if ( Build.VERSION.SDK_INT <= Build.VERSION_CODES.P ) {
            if ( ContextCompat.checkSelfPermission(context,
                                                   Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ) {
               permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
         }
      }

      return permissionsNeeded.toArray(new String[0]);
   }
}
