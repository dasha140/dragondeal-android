package com.elvsf.dragondeal.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.elvsf.dragondeal.adapter.MessageAdapter;
import com.elvsf.dragondeal.api.chat.ChatMessageResponse;
import com.elvsf.dragondeal.api.chat.ChatRepository;
import com.elvsf.dragondeal.api.chat.ChatResponse;
import com.elvsf.dragondeal.databinding.ActivityChatBinding;
import com.elvsf.dragondeal.storage.TokenStorage;
import com.elvsf.dragondeal.viewmodel.ChatViewModel;

import java.util.List;

import static org.koin.java.KoinJavaComponent.get;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_CHAT_ID = "chat_id";

    private ActivityChatBinding binding;
    private ChatViewModel viewModel;
    private MessageAdapter messageAdapter;
    private TokenStorage tokenStorage;

    private Long orderId;
    private Long chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = get(ChatViewModel.class);
        tokenStorage = new TokenStorage(this);

        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, 0L);
        chatId = getIntent().getLongExtra(EXTRA_CHAT_ID, 0L);

        setupRecyclerView();
        setupClicks();
        setupObservers();

        if (chatId != null && chatId > 0) {
            loadMessages(chatId);
        } else if (orderId != null && orderId > 0) {
            loadChatByOrder(orderId);
        } else {
            showToast("Чат не найден");
            finish();
        }
    }

    private void setupObservers() {
        viewModel.getChat().observe(this, chat -> {
            if (chat != null && chat.getId() != null) {
                chatId = chat.getId();
                loadMessages(chatId);
                renderChatHeader(chat);
            }
        });

        viewModel.getMessages().observe(this, messages -> {
            messageAdapter.setMessages(messages);
            scrollToBottom();
            // Re-enable send button when new messages arrive (from send or load)
            binding.buttonSendMessage.setEnabled(true);
        });

        viewModel.getLoading().observe(this, this::setLoading);
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
        ChatRepository repo = get(ChatRepository.class);
        viewModel.loadChatByOrder(repo, orderId);
    }

    private void loadMessages(Long chatId) {
        ChatRepository repo = get(ChatRepository.class);
        viewModel.loadMessages(repo, chatId);
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

        ChatRepository repo = get(ChatRepository.class);
        viewModel.sendMessage(repo, chatId, content);
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
