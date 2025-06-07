/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package com.vinicius.sticker.view.feature.stickerpack.creation.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.vinicius.sticker.R;
import com.vinicius.sticker.view.feature.stickerpack.creation.viewmodel.PermissionRequestViewModel;
import com.vinicius.sticker.view.feature.stickerpack.creation.viewmodel.PermissionSettingsViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PermissionRequestFragment extends BottomSheetDialogFragment {
   private final static String TAG_LOG = PermissionRequestFragment.class.getSimpleName();

   private PermissionRequestViewModel permissionRequestViewModel;
   private PermissionSettingsViewModel permissionSettingsViewModel;

   private ActivityResultLauncher<String[]> permissionLauncher;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setStyle(STYLE_NORMAL, R.style.BottomSheetStyle);
   }

   @Nullable
   @Override
   public View onCreateView(
       LayoutInflater inflater,
       @Nullable ViewGroup container,
       @Nullable Bundle savedInstanceState
   ) {
      permissionRequestViewModel = new ViewModelProvider(requireActivity()).get(PermissionRequestViewModel.class);
      permissionSettingsViewModel = new ViewModelProvider(requireActivity()).get(PermissionSettingsViewModel.class);

      View view = inflater.inflate( R.layout.dialog_permission_request, container, false );

      Button buttonGrantPermission = view.findViewById(R.id.open_permission_request);
      buttonGrantPermission.setOnClickListener( viewAccept -> requestPermissionsLogic() );

      Button buttonCancelPermission = view.findViewById( R.id.cancel_permission_button );
      buttonCancelPermission.setOnClickListener( viewCancel -> {
         permissionRequestViewModel.setPermissionDenied();
         dismiss();
      } );

      return view;
   }

   @NonNull
   @Override
   public Dialog onCreateDialog(Bundle savedInstanceState) {
      BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
      dialog.setOnShowListener(dialogInterface -> {
         BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
         FrameLayout bottomSheet = bottomSheetDialog.findViewById(R.id.design_bottom_sheet);

         if (bottomSheet != null) {
            bottomSheet.setBackground(new ColorDrawable(Color.TRANSPARENT));
         }
      });
      return dialog;
   }

   private void requestPermissionsLogic() {
      String[] permissionsToRequest = permissionRequestViewModel.getPermissionsToRequest().getValue();

      if ((permissionsToRequest != null ? permissionsToRequest.length : 0) != 0) {
         List<String> permissionsNotGranted = new ArrayList<>();
         for (String permission : permissionsToRequest) {
            if ( ContextCompat.checkSelfPermission(
                requireContext(), permission ) != PackageManager.PERMISSION_GRANTED ) {
               permissionsNotGranted.add( permission );
            }
         }

         if ( permissionsNotGranted.isEmpty() ) {
            permissionRequestViewModel.setPermissionGranted();
            dismiss();
         } else {
            permissionLauncher.launch( permissionsNotGranted.toArray( new String[0] ) );
         }
      } else {
         permissionRequestViewModel.setPermissionGranted();
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
                Log.i(TAG_LOG, permission + ": " + isGranted);

                if ( !isGranted ) {
                   allGranted = false;
                   deniedPermissions.add( permission );
                }
             }

             if ( allGranted ) {
                permissionRequestViewModel.setPermissionGranted();
                dismiss();
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
                          permissionSettingsViewModel.getPermissionGranted().observe(
                                  requireActivity(), granted -> {
                                     if (granted != null && granted) {
                                        permissionRequestViewModel.setPermissionGranted();
                                        dismiss();
                                     }
                                  });

                          permissionSettingsViewModel.getPermissionDenied().observe(
                                  requireActivity(), denied -> {
                                     permissionRequestViewModel.setPermissionDenied();
                                     dismiss();
                                  });

                          PermissionSettingsViewModel.launchPermissionSettings(requireActivity());
                       }, 250
                   );
                } else {
                   permissionRequestViewModel.setPermissionDenied();
                   dismiss();
                }
             }
          }
      );
   }
}
