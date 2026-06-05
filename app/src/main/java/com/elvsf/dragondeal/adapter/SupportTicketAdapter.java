package com.elvsf.dragondeal.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.dto.SupportTicketResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SupportTicketAdapter extends RecyclerView.Adapter<SupportTicketAdapter.TicketViewHolder> {

    private final List<SupportTicketResponse> tickets = new ArrayList<>();

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_support_ticket, parent, false);

        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        SupportTicketResponse ticket = tickets.get(position);

        holder.textSubject.setText(safe(ticket.getSubject(), "Без темы"));
        holder.textMessage.setText(safe(ticket.getMessage(), ""));
        holder.textStatus.setText(mapStatus(ticket.getStatus()));
        holder.textDate.setText(formatDate(ticket.getCreatedAt()));

        String adminReply = ticket.getAdminReply();

        if (adminReply != null && !adminReply.trim().isEmpty()) {
            holder.textReply.setText(adminReply.trim());
            holder.layoutReply.setVisibility(View.VISIBLE);
        } else {
            holder.layoutReply.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    public void setTickets(List<SupportTicketResponse> newTickets) {
        tickets.clear();

        if (newTickets != null) {
            tickets.addAll(newTickets);
        }

        notifyDataSetChanged();
    }

    private String mapStatus(String status) {
        if (status == null) {
            return "Неизвестно";
        }

        switch (status.toUpperCase()) {
            case "OPEN":
                return "Открыто";
            case "ANSWERED":
                return "Отвечено";
            case "CLOSED":
                return "Закрыто";
            default:
                return status;
        }
    }

    private String formatDate(String createdAt) {
        if (createdAt == null || createdAt.trim().isEmpty()) {
            return "";
        }

        try {
            String datePart = createdAt.substring(0, Math.min(10, createdAt.length()));

            if (datePart.contains("T")) {
                datePart = datePart.split("T")[0];
            }

            String[] parts = datePart.split("-");

            if (parts.length == 3) {
                return String.format(Locale.getDefault(), "%s.%s.%s", parts[2], parts[1], parts[0]);
            }

            return datePart;
        } catch (Exception e) {
            return createdAt;
        }
    }

    private String safe(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {

        private final TextView textSubject;
        private final TextView textStatus;
        private final TextView textDate;
        private final TextView textMessage;
        private final TextView textReply;
        private final LinearLayout layoutReply;

        TicketViewHolder(@NonNull View itemView) {
            super(itemView);

            textSubject = itemView.findViewById(R.id.textTicketSubject);
            textStatus = itemView.findViewById(R.id.textTicketStatus);
            textDate = itemView.findViewById(R.id.textTicketDate);
            textMessage = itemView.findViewById(R.id.textTicketMessage);
            textReply = itemView.findViewById(R.id.textTicketReply);
            layoutReply = itemView.findViewById(R.id.layoutTicketReply);
        }
    }
}
