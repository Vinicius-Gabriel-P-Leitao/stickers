/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.feature.presentation.fragment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.vinicius.sticker.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionRequestFragment extends BottomSheetDialogFragment {
   private ActivityResultLauncher<String[]> permissionLauncher;
   private String[] permissionsToRequest;
   private PermissionCallback callback;

   public interface PermissionCallback {
      void onPermissionsGranted();

      void onPermissionsDenied();
   }

   public void setCallback(PermissionCallback callback) {
      this.callback = callback;
   }

   public void setPermissions(String[] permissions) {
      this.permissionsToRequest = permissions;
   }

   @Nullable
   @Override
   public View onCreateView(
       LayoutInflater inflater,
       @Nullable ViewGroup container,
       @Nullable Bundle savedInstanceState
   ) {
      View view = inflater.inflate( R.layout.dialog_permission_request, container, false );

      Button buttonGrantPermission = view.findViewById( R.id.grant_permission_button );
      buttonGrantPermission.setOnClickListener( viewAccept -> requestPermissionsLogic() );

      Button buttonCancelPermission = view.findViewById( R.id.cancel_permission_button );
      buttonCancelPermission.setOnClickListener( viewCancel -> {
         callback.onPermissionsDenied();
         dismiss();
      } );

      return view;
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate( savedInstanceState );
      setStyle( STYLE_NORMAL, R.style.TransparentBottomSheet );
   }

   private void requestPermissionsLogic() {
      if ( permissionsToRequest.length != 0 ) {
         List<String> permissionsNotGranted = new ArrayList<>();
         for (String permission : permissionsToRequest) {
            if ( ContextCompat.checkSelfPermission(
                requireContext(), permission ) != PackageManager.PERMISSION_GRANTED ) {
               permissionsNotGranted.add( permission );
            }
         }

         if ( permissionsNotGranted.isEmpty() ) {
            if ( callback != null ) {
               callback.onPermissionsGranted();
            }
            dismiss();
         } else {
            permissionLauncher.launch( permissionsNotGranted.toArray( new String[0] ) );
         }
      } else {
         if ( callback != null ) {
            callback.onPermissionsGranted();
         }
         dismiss();
      }
   }

   @Override
   public void onAttach(
       @NonNull Context context
   ) {
      super.onAttach( context );
      permissionLauncher = registerForActivityResult(
          new ActivityResultContracts.RequestMultiplePermissions(), result -> {
             boolean allGranted = true;
             List<String> deniedPermissions = new ArrayList<>();

             for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                String permission = entry.getKey();
                boolean isGranted = entry.getValue();
                Log.i( "ResultLog", permission + ": " + isGranted );
                if ( !isGranted ) {
                   allGranted = false;
                   deniedPermissions.add( permission );
                }
             }

             if ( allGranted ) {
                if ( callback != null ) {
                   callback.onPermissionsGranted();
                   dismiss();
                }
             } else {
                boolean permanentlyDenied = false;
                for (String permission : deniedPermissions) {
                   if ( !shouldShowRequestPermissionRationale( permission ) ) {
                      permanentlyDenied = true;
                      break;
                   }
                }
                if ( permanentlyDenied ) {
                   new Handler( Looper.getMainLooper() ).postDelayed(
                       () -> {
                          PermissionSettingsFragment dialog = getPermissionSettingsDialogFragment();
                          dialog.show( getChildFragmentManager(), "PermissionSettingsDialog" );
                       }, 250
                   );
                } else {
                   if ( callback != null ) {
                      callback.onPermissionsDenied();
                      dismiss();
                   }
                }
             }
          }
      );
   }

   @NonNull
   private PermissionSettingsFragment getPermissionSettingsDialogFragment() {
      PermissionSettingsFragment dialog = new PermissionSettingsFragment();
      dialog.setCallback( new PermissionSettingsFragment.PermissionCallback() {
         @Override
         public void onPermissionsGranted() {
            dismiss();
         }

         @Override
         public void onPermissionsDenied() {
            callback.onPermissionsDenied();
            dismiss();
         }
      } );
      return dialog;
   }
}
