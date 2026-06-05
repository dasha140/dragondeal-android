package com.elvsf.dragondeal.screens;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.elvsf.dragondeal.BuildConfig;
import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.adapter.ListingAdapter;
import com.elvsf.dragondeal.api.ListingApi;
import com.elvsf.dragondeal.api.RetrofitClient;
import com.elvsf.dragondeal.api.UserApi;
import com.elvsf.dragondeal.databinding.ActivitySellerProfileBinding;
import com.elvsf.dragondeal.dto.ListingResponse;
import com.elvsf.dragondeal.dto.UserResponse;
import com.elvsf.dragondeal.ui.DarkDialog;
import com.elvsf.dragondeal.util.ImageUrlResolver;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerProfileActivity extends AppCompatActivity {

    private ActivitySellerProfileBinding binding;
    private UserApi userApi;
    private ListingApi listingApi;

    private final List<ListingResponse> sellerListings = new ArrayList<>();
    private ListingAdapter sellerListingsAdapter;

    private Long sellerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySellerProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userApi = RetrofitClient.getInstance(this).create(UserApi.class);
        listingApi = RetrofitClient.getInstance(this).create(ListingApi.class);

        sellerId = getIntent().getLongExtra("sellerId", 0L);

        if (sellerId == null || sellerId == 0L) {
            showError("Продавец не найден");
            return;
        }

        setupBack();
        setupListingsList();
        setupRatingClick();
        loadSellerData();
    }

    private void setupBack() {
        binding.buttonSellerProfileBack.setOnClickListener(v -> finish());
    }

    private void setupRatingClick() {
        binding.layoutSellerProfileRating.setOnClickListener(v -> openReviews());
    }

    private void openReviews() {
        if (sellerId == null) {
            return;
        }

        String sellerName = null;

        if (binding.textSellerProfileName.getText() != null) {
            sellerName = binding.textSellerProfileName.getText().toString().trim();
        }

        if (sellerName.isEmpty()) {
            sellerName = null;
        }

        Intent intent = new Intent(this, SellerReviewsActivity.class);
        intent.putExtra("sellerId", sellerId);
        intent.putExtra("sellerName", sellerName);
        startActivity(intent);
    }

    private void setupListingsList() {
        sellerListingsAdapter = new ListingAdapter(sellerListings, listing -> {
            Intent intent = new Intent(SellerProfileActivity.this, ListingDetailActivity.class);
            intent.putExtra("listingId", listing.getId());
            startActivity(intent);
        });

        binding.recyclerSellerProfileListings.setLayoutManager(
                new LinearLayoutManager(this)
        );

        binding.recyclerSellerProfileListings.setAdapter(sellerListingsAdapter);
    }

    private void loadSellerData() {
        setLoading(true);

        // Load seller info
        userApi.findUserById(sellerId).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    setLoading(false);
                    if (response.code() == 404) {
                        showError("Продавец не найден");
                    } else {
                        showError("Не удалось загрузить профиль продавца");
                    }
                    return;
                }

                UserResponse user = response.body();
                renderSellerInfo(user);
                setLoading(false);

                // Now load listings
                loadSellerListings();
            }

            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                setLoading(false);

                String msg = t.getMessage();

                if (msg != null && (msg.contains("Unable to resolve host") || msg.contains("ECONNREFUSED"))) {
                    showError("Нет соединения с сервером");
                } else {
                    showError("Не удалось загрузить профиль продавца");
                }
            }
        });
    }

    private void loadSellerListings() {
        listingApi.findListingsBySellerId(sellerId).enqueue(new Callback<List<ListingResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ListingResponse>> call, @NonNull Response<List<ListingResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    showNoListings();
                    return;
                }

                List<ListingResponse> result = response.body();

                sellerListings.clear();
                sellerListings.addAll(result);
                sellerListingsAdapter.notifyDataSetChanged();

                if (sellerListings.isEmpty()) {
                    showNoListings();
                } else {
                    binding.textSellerListingsTitle.setVisibility(View.VISIBLE);
                    binding.recyclerSellerProfileListings.setVisibility(View.VISIBLE);
                    binding.textSellerNoListings.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ListingResponse>> call, @NonNull Throwable t) {
                // Listings failure is non-critical — just show no listings
                showNoListings();
            }
        });
    }

    private void renderSellerInfo(UserResponse user) {
        binding.sellerProfileCard.setVisibility(View.VISIBLE);

        String name = user.getNameForDisplay();
        binding.textSellerProfileName.setText(name);

        String username = user.getUsername();
        if (username != null && !username.trim().isEmpty()) {
            binding.textSellerProfileUsername.setText("@" + username);
        } else {
            binding.textSellerProfileUsername.setVisibility(View.GONE);
        }

        if (user.getRating() != null) {
            binding.textSellerProfileRating.setText(user.getRating().toString());
        } else {
            binding.textSellerProfileRating.setText("0.00");
        }

        Long completedOrders = user.getCompletedOrdersCount();
        if (completedOrders == null) {
            completedOrders = 0L;
        }
        binding.textSellerProfileOrders.setText("Завершённых заказов: " + completedOrders);

        String createdAt = user.getCreatedAt();
        if (createdAt != null && !createdAt.trim().isEmpty()) {
            String since = formatCreatedAt(createdAt);
            if (since != null) {
                binding.textSellerProfileSince.setText(since);
                binding.textSellerProfileSince.setVisibility(View.VISIBLE);
            } else {
                binding.textSellerProfileSince.setVisibility(View.GONE);
            }
        } else {
            binding.textSellerProfileSince.setVisibility(View.GONE);
        }

        loadAvatar(user.getAvatarUrl());
    }

    private void loadAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            binding.imageSellerProfileAvatar.setPadding(dp(12), dp(12), dp(12), dp(12));
            binding.imageSellerProfileAvatar.setColorFilter(Color.parseColor("#FF3B30"));
            binding.imageSellerProfileAvatar.setImageResource(R.drawable.outline_account_circle_24);
            return;
        }

        binding.imageSellerProfileAvatar.setPadding(0, 0, 0, 0);
        binding.imageSellerProfileAvatar.clearColorFilter();

        Glide.with(this)
                .load(resolveImageUrl(avatarUrl))
                .circleCrop()
                .placeholder(R.drawable.outline_account_circle_24)
                .error(R.drawable.outline_account_circle_24)
                .into(binding.imageSellerProfileAvatar);
    }

    private String resolveImageUrl(String imageUrl) {
        return ImageUrlResolver.resolve(imageUrl, BuildConfig.BASE_URL);
    }

    private String formatCreatedAt(String rawDate) {
        try {
            // Try ISO-8601 format: 2025-01-15T10:30:00.000+00:00
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM yyyy", Locale.forLanguageTag("ru"));

            String datePart = rawDate;

            if (datePart.contains("T")) {
                datePart = datePart.substring(0, datePart.indexOf("T"));
            } else if (datePart.contains(" ")) {
                datePart = datePart.substring(0, datePart.indexOf(" "));
            }

            java.util.Date date = inputFormat.parse(datePart + "T00:00:00");

            if (date != null) {
                return "На сайте с: " + outputFormat.format(date);
            }
        } catch (Exception ignored) {
            // Try simpler parse
            try {
                String datePart = rawDate.substring(0, 10);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM yyyy", Locale.forLanguageTag("ru"));
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                java.util.Date date = inputFormat.parse(datePart);
                if (date != null) {
                    return "На сайте с: " + outputFormat.format(date);
                }
            } catch (Exception ignored2) {
            }
        }

        return null;
    }

    private void setLoading(boolean loading) {
        if (loading) {
            binding.progressSellerProfile.setVisibility(View.VISIBLE);
            binding.sellerProfileCard.setVisibility(View.GONE);
            binding.sellerProfileError.setVisibility(View.GONE);
            binding.textSellerListingsTitle.setVisibility(View.GONE);
            binding.recyclerSellerProfileListings.setVisibility(View.GONE);
            binding.textSellerNoListings.setVisibility(View.GONE);
        } else {
            binding.progressSellerProfile.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        setLoading(false);
        binding.sellerProfileCard.setVisibility(View.GONE);
        binding.sellerProfileError.setVisibility(View.VISIBLE);
        binding.sellerProfileError.findViewById(android.R.id.text1);

        DarkDialog.showMessage(this, "Ошибка", message);
    }

    private void showNoListings() {
        binding.textSellerListingsTitle.setVisibility(View.GONE);
        binding.recyclerSellerProfileListings.setVisibility(View.GONE);
        binding.textSellerNoListings.setVisibility(View.VISIBLE);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
