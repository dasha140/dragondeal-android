package com.elvsf.dragondeal.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.elvsf.dragondeal.api.ListingApi;
import com.elvsf.dragondeal.ui.DarkDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.elvsf.dragondeal.adapter.ListingAdapter;
import com.elvsf.dragondeal.api.RetrofitClient;
import com.elvsf.dragondeal.databinding.ActivityMyListingsBinding;
import com.elvsf.dragondeal.dto.ListingResponse;
import com.elvsf.dragondeal.storage.TokenStorage;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyListingsActivity extends AppCompatActivity {

    private final List<ListingResponse> listings = new ArrayList<>();

    private ActivityMyListingsBinding binding;
    private ListingApi listingApi;
    private ListingAdapter listingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMyListingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        listingApi = RetrofitClient.getInstance(this).create(ListingApi.class);

        setupRecyclerView();
        setupClicks();

        loadMyListings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyListings();
    }

    private void setupRecyclerView() {
        listingAdapter = new ListingAdapter(listings, this::showListingActions);

        binding.recyclerMyListings.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerMyListings.setAdapter(listingAdapter);
    }

    private void setupClicks() {
        binding.buttonBack.setOnClickListener(v -> finish());

        binding.buttonCreateListing.setOnClickListener(v -> {
            Intent intent = new Intent(MyListingsActivity.this, AddListingActivity.class);
            startActivity(intent);
        });
    }

    private void loadMyListings() {
        setLoading(true);

        listingApi.findMyListings().enqueue(new Callback<List<ListingResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<ListingResponse>> call,
                    @NonNull Response<List<ListingResponse>> response
            ) {
                setLoading(false);

                if (!response.isSuccessful() || response.body() == null) {
                    handleLoadError(response);
                    return;
                }

                listings.clear();
                listings.addAll(response.body());
                listingAdapter.notifyDataSetChanged();

                binding.textMyListingsEmpty.setVisibility(
                        listings.isEmpty() ? View.VISIBLE : View.GONE
                );
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<ListingResponse>> call,
                    @NonNull Throwable t
            ) {
                setLoading(false);
                showToast("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void handleLoadError(Response<List<ListingResponse>> response) {
        String errorMessage = "Не удалось загрузить объявления: " + response.code();

        try {
            if (response.errorBody() != null) {
                errorMessage = response.errorBody().string();
            }
        } catch (Exception ignored) {
        }

        if (isAuthError(errorMessage)) {
            handleExpiredSession();
            return;
        }

        DarkDialog.showMessage(
                MyListingsActivity.this,
                "Ошибка загрузки",
                errorMessage
        );
    }

    private boolean isAuthError(String errorMessage) {
        if (errorMessage == null) {
            return false;
        }

        return errorMessage.contains("User not found")
                || errorMessage.contains("Invalid or expired token")
                || errorMessage.contains("Invalid token")
                || errorMessage.contains("JWT")
                || errorMessage.contains("Unauthorized");
    }

    private void handleExpiredSession() {
        new TokenStorage(MyListingsActivity.this).clear();

        DarkDialog.showConfirm(
                MyListingsActivity.this,
                "Сессия устарела",
                "Нужно заново войти в аккаунт",
                "Войти заново",
                "Закрыть",
                () -> {
                    Intent intent = new Intent(MyListingsActivity.this, SigninActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
        );
    }

    private void showListingActions(ListingResponse listing) {
        if (listing == null || listing.getId() == null) {
            showToast("Некорректное объявление");
            return;
        }

        openListingDetail(listing);
    }

    private void openListingDetail(ListingResponse listing) {
        Intent intent = new Intent(this, ListingDetailActivity.class);
        intent.putExtra("listingId", listing.getId());
        startActivity(intent);
    }

    private void deactivateListing(ListingResponse listing) {
        if (!"ACTIVE".equals(listing.getStatus())) {
            showToast("Снять можно только ACTIVE объявление");
            return;
        }

        setLoading(true);

        listingApi.deactivateListing(listing.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(
                    @NonNull Call<Void> call,
                    @NonNull Response<Void> response
            ) {
                setLoading(false);

                if (!response.isSuccessful()) {
                    showToast("Не удалось снять объявление: " + response.code());
                    return;
                }

                showToast("Объявление снято");
                loadMyListings();
            }

            @Override
            public void onFailure(
                    @NonNull Call<Void> call,
                    @NonNull Throwable t
            ) {
                setLoading(false);
                showToast("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressMyListings.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}