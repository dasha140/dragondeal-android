package com.elvsf.dragondeal.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.elvsf.dragondeal.adapter.MessageAdapter;
import com.elvsf.dragondeal.api.chat.ChatMessageResponse;
import com.elvsf.dragondeal.api.chat.ChatRepository;
import com.elvsf.dragondeal.api.chat.ChatResponse;
import com.elvsf.dragondeal.databinding.ActivityChatBinding;
import com.elvsf.dragondeal.storage.TokenStorage;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_CHAT_ID = "chat_id";

    private ActivityChatBinding binding;
    private ChatRepository chatRepository;
    private MessageAdapter messageAdapter;
    private TokenStorage tokenStorage;

    private Long orderId;
    private Long chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        chatRepository = new ChatRepository(this);
        tokenStorage = new TokenStorage(this);

        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, 0L);
        chatId = getIntent().getLongExtra(EXTRA_CHAT_ID, 0L);

        setupRecyclerView();
        setupClicks();

        if (chatId != null && chatId > 0) {
            loadMessages(chatId);
        } else if (orderId != null && orderId > 0) {
            loadChatByOrder(orderId);
        } else {
            showToast("Чат не найден");
            finish();
        }
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(tokenStorage.getUserId());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);

        binding.recyclerMessages.setLayoutManager(layoutManager);
        binding.recyclerMessages.setAdapter(messageAdapter);
    }

    private void setupClicks() {
        binding.buttonChatBack.setOnClickListener(v -> finish());
        binding.buttonSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void loadChatByOrder(Long orderId) {
        setLoading(true);

        chatRepository.findChatsByOrder(orderId).enqueue(new Callback<List<ChatResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<ChatResponse>> call,
                    @NonNull Response<List<ChatResponse>> response
            ) {
                setLoading(false);

                if (!response.isSuccessful() || response.body() == null) {
                    showToast("Не удалось загрузить чат: " + response.code());
                    return;
                }

                List<ChatResponse> chats = response.body();

                if (chats.isEmpty()) {
                    showToast("Чат по заказу не найден");
                    return;
                }

                ChatResponse chat = chats.get(0);
                chatId = chat.getId();

                renderChatHeader(chat);
                loadMessages(chatId);
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<ChatResponse>> call,
                    @NonNull Throwable t
            ) {
                setLoading(false);
                showToast("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void loadMessages(Long chatId) {
        setLoading(true);

        chatRepository.findMessagesByChat(chatId).enqueue(new Callback<List<ChatMessageResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<ChatMessageResponse>> call,
                    @NonNull Response<List<ChatMessageResponse>> response
            ) {
                setLoading(false);

                if (!response.isSuccessful() || response.body() == null) {
                    showToast("Не удалось загрузить сообщения: " + response.code());
                    return;
                }

                messageAdapter.setMessages(response.body());
                scrollToBottom();
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<ChatMessageResponse>> call,
                    @NonNull Throwable t
            ) {
                setLoading(false);
                showToast("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void sendMessage() {
        String content = binding.editMessage.getText().toString().trim();

        if (chatId == null || chatId <= 0) {
            showToast("Чат ещё не загружен");
            return;
        }

        if (content.isEmpty()) {
            showToast("Введите сообщение");
            return;
        }

        binding.buttonSendMessage.setEnabled(false);

        chatRepository.sendMessage(chatId, content).enqueue(new Callback<ChatMessageResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<ChatMessageResponse> call,
                    @NonNull Response<ChatMessageResponse> response
            ) {
                binding.buttonSendMessage.setEnabled(true);

                if (!response.isSuccessful() || response.body() == null) {
                    showToast("Не удалось отправить: " + response.code());
                    return;
                }

                binding.editMessage.setText("");
                messageAdapter.addMessage(response.body());
                scrollToBottom();
            }

            @Override
            public void onFailure(
                    @NonNull Call<ChatMessageResponse> call,
                    @NonNull Throwable t
            ) {
                binding.buttonSendMessage.setEnabled(true);
                showToast("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void renderChatHeader(ChatResponse chat) {
        String buyer = safe(chat.getBuyerUsername(), "Покупатель");
        String seller = safe(chat.getSellerUsername(), "Продавец");

        binding.textChatTitle.setText("Заказ #" + chat.getOrderId());
        binding.textChatSubtitle.setText(buyer + " ↔ " + seller);
    }

    private void scrollToBottom() {
        int count = messageAdapter.getItemCount();

        if (count > 0) {
            binding.recyclerMessages.scrollToPosition(count - 1);
        }
    }

    private void setLoading(boolean loading) {
        binding.progressChat.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String safe(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}