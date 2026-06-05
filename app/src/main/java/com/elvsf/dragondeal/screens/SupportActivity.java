package com.elvsf.dragondeal.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.elvsf.dragondeal.ui.DarkDialog;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
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
        supportApi = com.elvsf.dragondeal.api.RetrofitClient.getInstance(this).create(SupportApi.class);

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

        supportApi.createSupportTicket(request).enqueue(new Callback<SupportTicketResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<SupportTicketResponse> call,
                    @NonNull Response<SupportTicketResponse> response
            ) {
                setSendLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(SupportActivity.this, "Обращение отправлено", Toast.LENGTH_SHORT).show();

                    binding.editTicketSubject.setText("");
                    binding.editTicketMessage.setText("");

                    loadMyTickets();
                } else {
                    String errorMessage = extractErrorMessage(response);
                    DarkDialog.showMessage(SupportActivity.this, "Ошибка", errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<SupportTicketResponse> call, @NonNull Throwable t) {
                setSendLoading(false);

                if (t instanceof IOException) {
                    DarkDialog.showMessage(
                            SupportActivity.this,
                            "Ошибка",
                            "Нет соединения с сервером"
                    );
                } else {
                    DarkDialog.showMessage(
                            SupportActivity.this,
                            "Ошибка",
                            "Произошла ошибка: " + t.getMessage()
                    );
                }
            }
        });
    }

    private void loadMyTickets() {
        binding.progressTickets.setVisibility(View.VISIBLE);
        binding.textTicketsEmpty.setVisibility(View.GONE);

        supportApi.getMySupportTickets().enqueue(new Callback<List<SupportTicketResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<SupportTicketResponse>> call,
                    @NonNull Response<List<SupportTicketResponse>> response
            ) {
                binding.progressTickets.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<SupportTicketResponse> tickets = response.body();

                    if (tickets.isEmpty()) {
                        binding.textTicketsEmpty.setVisibility(View.VISIBLE);
                    } else {
                        binding.textTicketsEmpty.setVisibility(View.GONE);
                        ticketAdapter.setTickets(tickets);
                    }
                } else {
                    binding.textTicketsEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SupportTicketResponse>> call, @NonNull Throwable t) {
                binding.progressTickets.setVisibility(View.GONE);
                binding.textTicketsEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setSendLoading(boolean loading) {
        binding.buttonSendTicket.setEnabled(!loading);
        binding.buttonSendTicket.setAlpha(loading ? 0.5f : 1.0f);
    }

    private String extractErrorMessage(Response<?> response) {
        if (response.code() == 401 || response.code() == 403) {
            return "Войдите в аккаунт, чтобы обратиться в поддержку";
        }

        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();

                if (errorBody.contains("\"message\"")) {
                    int start = errorBody.indexOf("\"message\"");
                    int colonIdx = errorBody.indexOf(":", start);

                    if (colonIdx >= 0) {
                        int valueStart = errorBody.indexOf("\"", colonIdx);
                        int valueEnd = errorBody.indexOf("\"", valueStart + 1);

                        if (valueStart >= 0 && valueEnd > valueStart) {
                            return errorBody.substring(valueStart + 1, valueEnd);
                        }
                    }
                }

                return errorBody;
            }
        } catch (Exception ignored) {
        }

        return "Не удалось отправить обращение (ошибка " + response.code() + ")";
    }
}
