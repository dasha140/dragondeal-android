package com.elvsf.dragondeal.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.dto.OrderResponse;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(OrderResponse order);
    }

    private final List<OrderResponse> orders = new ArrayList<>();
    private final OnOrderClickListener listener;

    public OrderAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);

        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderResponse order = orders.get(position);

        holder.textOrderTitle.setText("Заказ #" + order.getId());
        holder.textOrderListing.setText(safe(order.getListingTitle(), "Без названия"));

        String price = String.valueOf(order.getTotalPrice()) + " " + safe(order.getCurrency(), "USDT");
        holder.textOrderPrice.setText(price);

        String users = safe(order.getBuyerUsername(), "buyer")
                + " → "
                + safe(order.getSellerUsername(), "seller");

        holder.textOrderUsers.setText(users);
        holder.textOrderStatus.setText(safe(order.getStatus(), "UNKNOWN"));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void setOrders(List<OrderResponse> newOrders) {
        orders.clear();

        if (newOrders != null) {
            orders.addAll(newOrders);
        }

        notifyDataSetChanged();
    }

    private String safe(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        private final TextView textOrderTitle;
        private final TextView textOrderListing;
        private final TextView textOrderPrice;
        private final TextView textOrderUsers;
        private final TextView textOrderStatus;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            textOrderTitle = itemView.findViewById(R.id.textOrderTitle);
            textOrderListing = itemView.findViewById(R.id.textOrderListing);
            textOrderPrice = itemView.findViewById(R.id.textOrderPrice);
            textOrderUsers = itemView.findViewById(R.id.textOrderUsers);
            textOrderStatus = itemView.findViewById(R.id.textOrderStatus);
        }
    }
}