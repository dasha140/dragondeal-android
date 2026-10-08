package com.elvsf.dragondeal.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.elvsf.dragondeal.api.chat.ChatMessageResponse;
import com.elvsf.dragondeal.api.chat.ChatRepository;
import com.elvsf.dragondeal.api.chat.ChatResponse;
import com.elvsf.dragondeal.util.ApiCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * ViewModel for ChatActivity.
 * Holds chat info, messages, and UI state.
 */
public class ChatViewModel extends ViewModel {

    private final MutableLiveData<ChatResponse> chat = new MutableLiveData<>();
    private final MutableLiveData<List<ChatMessageResponse>> messages = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<ChatResponse> getChat() {
        return chat;
    }

    public LiveData<List<ChatMessageResponse>> getMessages() {
        return messages;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void loadChatByOrder(ChatRepository repo, Long orderId) {
        loading.setValue(true);

        repo.findChatsByOrder(orderId).enqueue(new ApiCallback<List<ChatResponse>>() {
            @Override
            protected void onSuccess(Response<List<ChatResponse>> response) {
                loading.setValue(false);
                List<ChatResponse> chats = response.body();

                if (chats != null && !chats.isEmpty()) {
                    chat.setValue(chats.get(0));
                }
            }

            @Override
            protected void showError(String message) {
                loading.setValue(false);
            }
        });
    }

    public void loadMessages(ChatRepository repo, Long chatId) {
        loading.setValue(true);

        repo.findMessagesByChat(chatId).enqueue(new ApiCallback<List<ChatMessageResponse>>() {
            @Override
            protected void onSuccess(Response<List<ChatMessageResponse>> response) {
                loading.setValue(false);
                messages.setValue(response.body());
            }

            @Override
            protected void showError(String message) {
                loading.setValue(false);
            }
        });
    }

    public void sendMessage(ChatRepository repo, Long chatId, String content) {
        repo.sendMessage(chatId, content).enqueue(new ApiCallback<ChatMessageResponse>() {
            @Override
            protected void onSuccess(Response<ChatMessageResponse> response) {
                ChatMessageResponse newMessage = response.body();
                if (newMessage != null) {
                    // Append to existing messages
                    List<ChatMessageResponse> current = messages.getValue();
                    if (current != null) {
                        current.add(newMessage);
                        messages.setValue(current);
                    } else {
                        ArrayList<ChatMessageResponse> single = new ArrayList<>();
                        single.add(newMessage);
                        messages.setValue(single);
                    }
                }
            }

            @Override
            protected void showError(String message) {
                // Error handled by Activity callback
            }
        });
    }
}
