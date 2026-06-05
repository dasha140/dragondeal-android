package com.elvsf.dragondeal.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.api.chat.ChatMessageResponse;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final List<ChatMessageResponse> messages = new ArrayList<>();
    private final Long currentUserId;

    public MessageAdapter(Long currentUserId) {
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessageResponse message = messages.get(position);

        boolean isMine = currentUserId != null
                && message.getSenderId() != null
                && currentUserId.equals(message.getSenderId());

        LinearLayout root = (LinearLayout) holder.itemView;
        root.setGravity(isMine ? Gravity.END : Gravity.START);

        holder.textMessageSender.setText(isMine ? "Вы" : safe(message.getSenderUsername(), "Пользователь"));
        holder.textMessageContent.setText(safe(message.getContent(), ""));
        holder.textMessageTime.setText(shortTime(message.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(List<ChatMessageResponse> newMessages) {
        messages.clear();

        if (newMessages != null) {
            messages.addAll(newMessages);
        }

        notifyDataSetChanged();
    }

    public void addMessage(ChatMessageResponse message) {
        if (message == null) {
            return;
        }

        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    private String safe(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }

    private String shortTime(String createdAt) {
        if (createdAt == null || createdAt.trim().isEmpty()) {
            return "";
        }

        if (createdAt.length() >= 16) {
            return createdAt.substring(0, 16).replace("T", " ");
        }

        return createdAt;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        private final TextView textMessageSender;
        private final TextView textMessageContent;
        private final TextView textMessageTime;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            textMessageSender = itemView.findViewById(R.id.textMessageSender);
            textMessageContent = itemView.findViewById(R.id.textMessageContent);
            textMessageTime = itemView.findViewById(R.id.textMessageTime);
        }
    }
}