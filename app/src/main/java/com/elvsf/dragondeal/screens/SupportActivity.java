package com.elvsf.dragondeal.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.adapter.SupportTicketAdapter;
import com.elvsf.dragondeal.api.SupportApi;
import com.elvsf.dragondeal.databinding.ActivitySupportBinding;
import com.elvsf.dragondeal.dto.SupportTicketRequest;
import com.elvsf.dragondeal.dto.SupportTicketResponse;
import com.elvsf.dragondeal.storage.TokenStorage;
import com.elvsf.dragondeal.ui.AuthGate;
import com.elvsf.dragondeal.util.ApiCallback;

import org.koin.java.KoinJavaComponent;

import java.util.List;

import static org.koin.java.KoinJavaComponent.get;

import retrofit2.Response;

public class SupportActivity extends AppCompatActivity {

    private ActivitySupportBinding binding;
    private SupportApi supportApi;
    private SupportTicketAdapter ticketAdapter;
    private TokenStorage tokenStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthGate.requireAuth(this, "Войдите в аккаунт, чтобы обратиться в поддержку")) {
            finish();
            return;
        }

        binding = ActivitySupportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenStorage = new TokenStorage(this);
        supportApi = get(SupportApi.class);

        setupRecyclerView();
        setupClicks();
        loadMyTickets();
    }

    private void setupRecyclerView() {
        ticketAdapter = new SupportTicketAdapter();

        binding.recyclerTickets.setLayoutManager(
                new LinearLayoutManager(this)
        );

        binding.recyclerTickets.setAdapter(ticketAdapter);
    }

    private void setupClicks() {
        binding.buttonSupportBack.setOnClickListener(view -> finish());

        binding.buttonSendTicket.setOnClickListener(view -> sendTicket());
    }

    private void sendTicket() {
        String subject = binding.editTicketSubject.getText().toString().trim();
        String message = binding.editTicketMessage.getText().toString().trim();

        if (subject.isEmpty()) {
            binding.editTicketSubject.setError("Введите тему обращения");
            return;
        }

        if (subject.length() < 3) {
            binding.editTicketSubject.setError("Тема должна содержать минимум 3 символа");
            return;
        }

        if (message.isEmpty()) {
            binding.editTicketMessage.setError("Введите сообщение");
            return;
        }

        if (message.length() < 10) {
            binding.editTicketMessage.setError("Сообщение должно содержать минимум 10 символов");
            return;
        }

        setSendLoading(true);

        SupportTicketRequest request = new SupportTicketRequest(subject, message);

        supportApi.createSupportTicket(request).enqueue(new ApiCallback<SupportTicketResponse>(this) {
            @Override
            protected void onSuccess(Response<SupportTicketResponse> response) {
                setSendLoading(false);

                Toast.makeText(SupportActivity.this, "Обращение отправлено", Toast.LENGTH_SHORT).show();

                binding.editTicketSubject.setText("");
                binding.editTicketMessage.setText("");

                loadMyTickets();
            }
        });
    }

    private void loadMyTickets() {
        binding.progressTickets.setVisibility(View.VISIBLE);
        binding.textTicketsEmpty.setVisibility(View.GONE);

        supportApi.getMySupportTickets().enqueue(new ApiCallback<List<SupportTicketResponse>>(this) {
            @Override
            protected void onSuccess(Response<List<SupportTicketResponse>> response) {
                binding.progressTickets.setVisibility(View.GONE);

                List<SupportTicketResponse> tickets = response.body();

                if (tickets.isEmpty()) {
                    binding.textTicketsEmpty.setVisibility(View.VISIBLE);
                } else {
                    binding.textTicketsEmpty.setVisibility(View.GONE);
                    ticketAdapter.setTickets(tickets);
                }
            }
        });
    }

    private void setSendLoading(boolean loading) {
        binding.buttonSendTicket.setEnabled(!loading);
        binding.buttonSendTicket.setAlpha(loading ? 0.5f : 1.0f);
    }
}
