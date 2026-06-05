package com.elvsf.dragondeal.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.model.MenuItemModel;

import java.util.List;

public class TopMenuAdapter extends RecyclerView.Adapter<TopMenuAdapter.TopMenuViewHolder> {

    public interface OnMenuClickListener {
        void onMenuClick(MenuItemModel item);
    }

    private final List<MenuItemModel> items;
    private final OnMenuClickListener listener;

    private int selectedPosition = 0;

    public TopMenuAdapter(List<MenuItemModel> items, OnMenuClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TopMenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_menu, parent, false);
        return new TopMenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopMenuViewHolder holder, int position) {
        MenuItemModel item = items.get(position);

        holder.textMenuTitle.setText(item.getTitle());
        holder.imageMenuIcon.setImageResource(item.getIconResId());

        if (position == selectedPosition) {
            holder.chipRoot.setBackgroundResource(R.drawable.bg_chip_category_selected);
            holder.textMenuTitle.setTextColor(0xFFFFFFFF);
            holder.imageMenuIcon.setColorFilter(0xFFFFFFFF);
        } else {
            holder.chipRoot.setBackgroundResource(R.drawable.bg_chip_category_unselected);
            holder.textMenuTitle.setTextColor(0xFFEAF2F5);
            holder.imageMenuIcon.setColorFilter(0xFF8E8E93);
        }

        holder.itemView.setOnClickListener(view -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getBindingAdapterPosition();

            if (selectedPosition == RecyclerView.NO_POSITION) {
                return;
            }

            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onMenuClick(items.get(selectedPosition));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class TopMenuViewHolder extends RecyclerView.ViewHolder {

        View chipRoot;
        ImageView imageMenuIcon;
        TextView textMenuTitle;

        public TopMenuViewHolder(@NonNull View itemView) {
            super(itemView);
            chipRoot = itemView;
            imageMenuIcon = itemView.findViewById(R.id.imageMenuIcon);
            textMenuTitle = itemView.findViewById(R.id.textMenuTitle);
        }
    }
}
