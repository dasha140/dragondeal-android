package com.elvsf.dragondeal.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.api.payment.PaymentMethodResponse;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodAdapter extends RecyclerView.Adapter<PaymentMethodAdapter.PaymentMethodViewHolder> {

    public interface OnPaymentMethodClickListener {
        void onPaymentMethodClick(PaymentMethodResponse paymentMethod);
    }

    public interface OnSelectionChangeListener {
        void onSelectionChanged(boolean hasSelection);
    }

    private final List<PaymentMethodResponse> paymentMethods = new ArrayList<>();
    private final OnPaymentMethodClickListener clickListener;
    private OnSelectionChangeListener selectionChangeListener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public PaymentMethodAdapter(OnPaymentMethodClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setSelectionChangeListener(OnSelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    @NonNull
    @Override
    public PaymentMethodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payment_method, parent, false);
        return new PaymentMethodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentMethodViewHolder holder, int position) {
        PaymentMethodResponse pm = paymentMethods.get(position);
        boolean isEnabled = Boolean.TRUE.equals(pm.getEnabled());
        boolean isSelected = position == selectedPosition;

        // Title: "USDC · Hardhat Local"
        String networkLabel = formatNetwork(pm.getNetwork());
        holder.titleTextView.setText(pm.getCurrency() + " · " + networkLabel);

        // Subtitle: chain ID
        holder.subtitleTextView.setText("Chain ID: " + pm.getChainId());

        // Token address abbreviated
        holder.tokenTextView.setText("Token: " + abbreviateAddress(pm.getTokenContract()));

        // Status badge
        if (isEnabled) {
            holder.statusText.setText("Доступно");
            holder.statusText.setBackgroundResource(R.drawable.bg_status_available);
            holder.statusText.setTextColor(Color.parseColor("#FF9500"));
        } else {
            holder.statusText.setText("Недоступно");
            holder.statusText.setBackgroundResource(R.drawable.bg_status_unavailable);
            holder.statusText.setTextColor(Color.parseColor("#6E6E73"));
        }

        // Card background based on state
        if (!isEnabled) {
            holder.rootView.setBackgroundResource(R.drawable.bg_payment_method_disabled);
        } else if (isSelected) {
            holder.rootView.setBackgroundResource(R.drawable.bg_payment_method_selected);
        } else {
            holder.rootView.setBackgroundResource(R.drawable.bg_payment_method_card);
        }

        // Enabled/disabled visual state
        holder.rootView.setEnabled(isEnabled);
        holder.radioButton.setEnabled(isEnabled);
        holder.radioButton.setChecked(isSelected);

        float alpha = isEnabled ? 1.0f : 0.4f;
        holder.titleTextView.setAlpha(alpha);
        holder.subtitleTextView.setAlpha(alpha);
        holder.tokenTextView.setAlpha(alpha);
        holder.statusText.setAlpha(alpha);

        // Click handling — only if enabled
        holder.itemView.setOnClickListener(v -> {
            if (isEnabled) {
                selectPaymentMethod(holder.getBindingAdapterPosition());
            }
        });
        holder.radioButton.setOnClickListener(v -> {
            if (isEnabled) {
                selectPaymentMethod(holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return paymentMethods.size();
    }

    public void setPaymentMethods(List<PaymentMethodResponse> newPaymentMethods) {
        paymentMethods.clear();
        if (newPaymentMethods != null) {
            paymentMethods.addAll(newPaymentMethods);
        }

        // Auto-select first enabled method
        selectedPosition = findFirstEnabledPosition();
        notifySelectionChange();
        notifyDataSetChanged();
    }

    public PaymentMethodResponse getSelectedPaymentMethod() {
        if (selectedPosition == RecyclerView.NO_POSITION) return null;
        if (selectedPosition < 0 || selectedPosition >= paymentMethods.size()) return null;
        PaymentMethodResponse pm = paymentMethods.get(selectedPosition);
        if (!Boolean.TRUE.equals(pm.getEnabled())) return null;
        return pm;
    }

    private void selectPaymentMethod(int position) {
        if (position == RecyclerView.NO_POSITION) return;
        if (position < 0 || position >= paymentMethods.size()) return;
        if (!Boolean.TRUE.equals(paymentMethods.get(position).getEnabled())) return;

        int oldPosition = selectedPosition;
        selectedPosition = position;

        if (oldPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(oldPosition);
        }
        notifyItemChanged(selectedPosition);
        notifySelectionChange();

        if (clickListener != null) {
            clickListener.onPaymentMethodClick(paymentMethods.get(selectedPosition));
        }
    }

    private int findFirstEnabledPosition() {
        for (int i = 0; i < paymentMethods.size(); i++) {
            if (Boolean.TRUE.equals(paymentMethods.get(i).getEnabled())) {
                return i;
            }
        }
        return RecyclerView.NO_POSITION;
    }

    private void notifySelectionChange() {
        if (selectionChangeListener != null) {
            selectionChangeListener.onSelectionChanged(getSelectedPaymentMethod() != null);
        }
    }

    private static String formatNetwork(String network) {
        if (network == null) return "";
        switch (network) {
            case "HARDHAT_LOCAL": return "Hardhat Local";
            case "POLYGON": return "Polygon";
            case "POLYGON_AMOY": return "Polygon Amoy";
            default: return network;
        }
    }

    private static String abbreviateAddress(String address) {
        if (address == null || address.length() < 10) return address;
        return address.substring(0, 6) + "..." + address.substring(address.length() - 4);
    }

    static class PaymentMethodViewHolder extends RecyclerView.ViewHolder {
        private final View rootView;
        private final RadioButton radioButton;
        private final TextView titleTextView;
        private final TextView subtitleTextView;
        private final TextView tokenTextView;
        private final TextView statusText;

        public PaymentMethodViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.layoutPaymentMethodRoot);
            radioButton = itemView.findViewById(R.id.radioPaymentMethod);
            titleTextView = itemView.findViewById(R.id.textPaymentMethodTitle);
            subtitleTextView = itemView.findViewById(R.id.textPaymentMethodSubtitle);
            tokenTextView = itemView.findViewById(R.id.textPaymentMethodToken);
            statusText = itemView.findViewById(R.id.textPaymentMethodStatus);
        }
    }
}
