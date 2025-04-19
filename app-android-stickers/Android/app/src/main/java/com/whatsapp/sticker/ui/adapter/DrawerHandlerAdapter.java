package com.whatsapp.sticker.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.whatsapp.sticker.R;

public class DrawerHandlerAdapter extends RecyclerView.Adapter<DrawerHandlerAdapter.DrawerHandlerViewHolder> {
   private final Context context;

   public DrawerHandlerAdapter(Context context) {
      this.context = context;
   }

   @NonNull
   @Override
   public DrawerHandlerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(context).inflate(R.layout.drawer_handle, parent, false);
      return new DrawerHandlerAdapter.DrawerHandlerViewHolder(view);
   }

   @Override
   public void onBindViewHolder(@NonNull DrawerHandlerViewHolder holder, int position) {
      View handle = holder.itemView.findViewById(R.id.drawer_handler);
      handle.setBackgroundResource(R.drawable.background_handler);
   }

   @Override
   public int getItemCount() {
      return 1;
   }

   static class DrawerHandlerViewHolder extends RecyclerView.ViewHolder {
      View handlerView;

      public DrawerHandlerViewHolder(View itemView) {
         super(itemView);
         handlerView = itemView.findViewById(R.id.drawer_handler);
      }
   }
}
