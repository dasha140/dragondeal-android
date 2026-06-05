package com.elvsf.dragondeal.api.chat;

import android.content.Context;

import com.elvsf.dragondeal.api.RetrofitClient;

import java.util.List;

import retrofit2.Call;

public class ChatRepository {

    private final ChatApi chatApi;

    public ChatRepository(Context context) {
        chatApi = RetrofitClient.getInstance(context).create(ChatApi.class);
    }

    public Call<List<ChatResponse>> findMyChats() {
        return chatApi.findMyChats();
    }

    public Call<List<ChatResponse>> findChatsByOrder(Long orderId) {
        return chatApi.findChatsByOrder(orderId);
    }

    public Call<ChatResponse> findChatById(Long chatId) {
        return chatApi.findChatById(chatId);
    }

    public Call<List<ChatMessageResponse>> findMessagesByChat(Long chatId) {
        return chatApi.findMessagesByChat(chatId);
    }

    public Call<ChatMessageResponse> sendMessage(Long chatId, String content) {
        return chatApi.sendMessage(new SendChatMessageRequest(chatId, content, null));
    }
}