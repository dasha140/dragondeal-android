package com.elvsf.dragondeal.api.chat;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatApi {

    @GET("chats/my")
    Call<List<ChatResponse>> findMyChats();

    @GET("chats/order/{orderId}")
    Call<List<ChatResponse>> findChatsByOrder(@Path("orderId") Long orderId);

    @GET("chats/{chatId}")
    Call<ChatResponse> findChatById(@Path("chatId") Long chatId);

    @GET("chats/{chatId}/messages")
    Call<List<ChatMessageResponse>> findMessagesByChat(@Path("chatId") Long chatId);

    @POST("chats/messages")
    Call<ChatMessageResponse> sendMessage(@Body SendChatMessageRequest request);
}