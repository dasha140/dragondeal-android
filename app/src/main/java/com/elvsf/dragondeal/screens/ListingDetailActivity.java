package com.elvsf.dragondeal.screens;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.elvsf.dragondeal.BuildConfig;
import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.adapter.ListingAdapter;
import com.elvsf.dragondeal.api.ListingApi;
import com.elvsf.dragondeal.api.OrderApi;
import com.elvsf.dragondeal.api.RetrofitClient;
import com.elvsf.dragondeal.databinding.ActivityListingDetailBinding;
import com.elvsf.dragondeal.dto.CreateOrderRequest;
import com.elvsf.dragondeal.dto.ListingResponse;
import com.elvsf.dragondeal.dto.OrderResponse;
import com.elvsf.dragondeal.payment.PaymentDialogFragment;
import com.elvsf.dragondeal.storage.TokenStorage;
import com.elvsf.dragondeal.ui.DarkDialog;
import com.elvsf.dragondeal.util.ImageUrlResolver;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListingDetailActivity extends AppCompatActivity {

    private final List<ListingResponse> otherSellerListings = new ArrayList<>();

    private ListingAdapter otherSellerListingsAdapter;
    private ActivityListingDetailBinding binding;

    private ListingApi listingApi;
    private OrderApi orderApi;
    private TokenStorage tokenStorage;

    private Long listingId;
    private ListingResponse currentListing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityListingDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        listingApi = RetrofitClient.getInstance(this).create(ListingApi.class);
        orderApi = RetrofitClient.getInstance(this).create(OrderApi.class);
        tokenStorage = new TokenStorage(this);

        setupOtherSellerListings();
        setupClicks();

        listingId = getIntent().getLongExtra("listingId", 0L);

        if (listingId == 0L) {
            Toast.makeText(this, "Некорректное объявление", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadListing();
    }

    private void setupClicks() {
        binding.buttonBackDetail.setOnClickListener(view -> finish());
        binding.buttonBuy.setOnClickListener(view -> createOrderAndOpenPayment());
        binding.sellerBlock.setOnClickListener(view -> openSellerProfile());
        binding.buttonDeactivateListing.setOnClickListener(view -> showDeactivateConfirm());
    }

    private void setupOtherSellerListings() {
        otherSellerListingsAdapter = new ListingAdapter(otherSellerListings, listing -> {
            Intent intent = new Intent(ListingDetailActivity.this, ListingDetailActivity.class);
            intent.putExtra("listingId", listing.getId());
            startActivity(intent);
        });

        binding.recyclerSellerListings.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        binding.recyclerSellerListings.setAdapter(otherSellerListingsAdapter);
    }

    private void loadListing() {
        setLoading(true);

        listingApi.findListingById(listingId).enqueue(new Callback<ListingResponse>() {
            @Override
            public void onResponse(Call<ListingResponse> call, Response<ListingResponse> response) {
                setLoading(false);

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(
                            ListingDetailActivity.this,
                            "Не удалось загрузить объявление: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                    return;
                }

                currentListing = response.body();
                renderListing(currentListing);
            }

            @Override
            public void onFailure(Call<ListingResponse> call, Throwable throwable) {
                setLoading(false);

                Toast.makeText(
                        ListingDetailActivity.this,
                        "Ошибка подключения: " + throwable.getMessage(),
                        Toast.LENGTH_LONG
                ).show();

                finish();
            }
        });
    }

    private void renderListing(ListingResponse listing) {
        binding.textDetailTitle.setText(safeText(listing.getTitle(), "Без названия"));
        binding.textDetailDescription.setText(safeText(listing.getDescription(), "Описание отсутствует"));

        binding.textDetailPrice.setText(
                listing.getPrice() + " " + safeText(listing.getCurrency(), "USDT")
        );

        String meta = safeText(listing.getGameName(), "Игра")
                + " • " + safeText(listing.getCategoryName(), "Категория")
                + " • " + safeText(listing.getNetwork(), "Сеть");

        binding.textDetailMeta.setText(meta);

        // Show status for INACTIVE listings
        String status = listing.getStatus();
        if ("INACTIVE".equals(status)) {
            binding.textDetailStatus.setText("Снято с публикации");
            binding.textDetailStatus.setVisibility(View.VISIBLE);
        } else if ("SOLD".equals(status)) {
            binding.textDetailStatus.setText("Уже продано");
            binding.textDetailStatus.setVisibility(View.VISIBLE);
        } else {
            binding.textDetailStatus.setVisibility(View.GONE);
        }

        binding.textSellerName.setText(safeText(listing.getSellerUsername(), "Продавец"));

        binding.textSellerRating.setText(
                "Рейтинг: " + (listing.getSellerRating() != null ? listing.getSellerRating() : "0")
        );

        Long completedOrders = listing.getSellerCompletedOrdersCount();

        if (completedOrders == null) {
            completedOrders = 0L;
        }

        binding.textSellerOrdersCount.setText(
                "Завершённых заказов: " + completedOrders
        );

        loadListingImage(listing);
        loadSellerAvatar(listing);

        loadOtherSellerListings(listing.getSellerId(), listing.getId());

        updateBuyButton(listing);
    }

    private void loadListingImage(ListingResponse listing) {
        Glide.with(this)
                .load(getPrimaryImageUrl(listing))
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(binding.imageDetailListing);
    }

    private void loadSellerAvatar(ListingResponse listing) {
        String avatarUrl = listing.getSellerAvatarUrl();

        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            binding.imageSellerAvatar.setPadding(dp(10), dp(10), dp(10), dp(10));
            binding.imageSellerAvatar.setColorFilter(Color.parseColor("#FF3B30"));
            binding.imageSellerAvatar.setImageResource(R.drawable.outline_account_circle_24);
            return;
        }

        binding.imageSellerAvatar.setPadding(0, 0, 0, 0);
        binding.imageSellerAvatar.clearColorFilter();

        Glide.with(this)
                .load(resolveImageUrl(avatarUrl))
                .circleCrop()
                .placeholder(R.drawable.outline_account_circle_24)
                .error(R.drawable.outline_account_circle_24)
                .into(binding.imageSellerAvatar);
    }

    private void loadOtherSellerListings(Long sellerId, Long currentListingId) {
        if (sellerId == null) {
            binding.textOtherSellerListingsTitle.setText("Других объявлений продавца пока нет");
            return;
        }

        listingApi.findListingsBySellerId(sellerId).enqueue(new Callback<List<ListingResponse>>() {
            @Override
            public void onResponse(Call<List<ListingResponse>> call, Response<List<ListingResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    binding.textOtherSellerListingsTitle.setText("Других объявлений продавца пока нет");
                    return;
                }

                List<ListingResponse> result = response.body();

                otherSellerListings.clear();

                for (ListingResponse item : result) {
                    if (item.getId() != null && !item.getId().equals(currentListingId)) {
                        otherSellerListings.add(item);
                    }
                }

                otherSellerListingsAdapter.notifyDataSetChanged();

                if (otherSellerListings.isEmpty()) {
                    binding.textOtherSellerListingsTitle.setText("Других объявлений продавца пока нет");
                } else {
                    binding.textOtherSellerListingsTitle.setText("Другие объявления продавца");
                }
            }

            @Override
            public void onFailure(Call<List<ListingResponse>> call, Throwable throwable) {
                binding.textOtherSellerListingsTitle.setText("Других объявлений продавца пока нет");
            }
        });
    }

    private void createOrderAndOpenPayment() {
        if (currentListing == null || currentListing.getId() == null) {
            Toast.makeText(this, "Объявление ещё не загружено", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!"ACTIVE".equals(currentListing.getStatus())) {
            Toast.makeText(this, "Это объявление уже недоступно", Toast.LENGTH_SHORT).show();
            updateBuyButton(currentListing);
            return;
        }

        setLoading(true);

        CreateOrderRequest request = new CreateOrderRequest(currentListing.getId());

        orderApi.createOrder(request).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                setLoading(false);

                if (!response.isSuccessful()) {
                    String errorMessage = "Не удалось создать заказ: " + response.code();

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

                    if (errorMessage.contains("Seller cannot buy own listing")) {
                        DarkDialog.showMessage(
                                ListingDetailActivity.this,
                                "Ошибка создания заказа",
                                "Вы не можете купить собственное объявление"
                        );
                        return;
                    }

                    DarkDialog.showMessage(
                            ListingDetailActivity.this,
                            "Ошибка создания заказа",
                            errorMessage
                    );

                    loadListing();
                    return;
                }

                OrderResponse order = response.body();

                if (order == null || order.getId() == null) {
                    Toast.makeText(
                            ListingDetailActivity.this,
                            "Пустой ответ заказа",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                openPaymentDialog(order);
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable throwable) {
                setLoading(false);

                Toast.makeText(
                        ListingDetailActivity.this,
                        "Ошибка подключения: " + throwable.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
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
        new TokenStorage(ListingDetailActivity.this).clear();

        DarkDialog.showConfirm(
                ListingDetailActivity.this,
                "Сессия устарела",
                "Нужно заново войти в аккаунт",
                "Войти заново",
                "Закрыть",
                () -> {
                    Intent intent = new Intent(ListingDetailActivity.this, SigninActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
        );
    }

    private void openPaymentDialog(OrderResponse order) {
        String listingTitle = safeText(order.getListingTitle(), "Заказ #" + order.getId());

        String totalPrice = "0";

        if (order.getTotalPrice() != null) {
            totalPrice = order.getTotalPrice().toString();
        }

        String currency = safeText(order.getCurrency(), "USDT");

        PaymentDialogFragment dialog = PaymentDialogFragment.newInstance(
                order.getId(),
                listingTitle,
                totalPrice + " " + currency
        );

        dialog.show(getSupportFragmentManager(), "PaymentDialog");
    }

    private void updateBuyButton(ListingResponse listing) {
        boolean isOwner = listing != null
                && listing.getSellerId() != null
                && listing.getSellerId().equals(tokenStorage.getUserId());

        if (listing == null) {
            binding.buttonBuy.setEnabled(false);
            binding.buttonBuy.setAlpha(0.5f);
            binding.buttonBuy.setText("Недоступно");
            binding.buttonDeactivateListing.setVisibility(View.GONE);
            return;
        }

        String status = listing.getStatus();

        if ("ACTIVE".equals(status)) {
            if (isOwner) {
                // Owner viewing own ACTIVE listing — show deactivate button
                binding.buttonBuy.setEnabled(false);
                binding.buttonBuy.setAlpha(0.5f);
                binding.buttonBuy.setText("Это ваше объявление");
                binding.buttonDeactivateListing.setVisibility(View.VISIBLE);
                return;
            }

            binding.buttonBuy.setEnabled(true);
            binding.buttonBuy.setAlpha(1.0f);
            binding.buttonBuy.setText("Купить");
            binding.buttonDeactivateListing.setVisibility(View.GONE);
            return;
        }

        binding.buttonBuy.setEnabled(false);
        binding.buttonBuy.setAlpha(0.5f);
        binding.buttonDeactivateListing.setVisibility(View.GONE);

        if ("SOLD".equals(status)) {
            binding.buttonBuy.setText("Уже продано");
        } else if ("INACTIVE".equals(status)) {
            binding.buttonBuy.setText("Снято с публикации");
        } else {
            binding.buttonBuy.setText("Недоступно");
        }
    }

    private String getPrimaryImageUrl(ListingResponse listing) {
        String imageUrl = null;

        if (listing.getImageUrls() != null && !listing.getImageUrls().isEmpty()) {
            imageUrl = listing.getImageUrls().get(0);
        } else if (listing.getGameImageUrl() != null && !listing.getGameImageUrl().trim().isEmpty()) {
            imageUrl = listing.getGameImageUrl();
        }

        return resolveImageUrl(imageUrl);
    }

    private String resolveImageUrl(String imageUrl) {
        return ImageUrlResolver.resolve(imageUrl, BuildConfig.BASE_URL);
    }

    private String safeText(String value, String fallback) {
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        return fallback;
    }

    private void setLoading(boolean loading) {
        binding.progressDetail.setVisibility(loading ? View.VISIBLE : View.GONE);

        if (loading) {
            binding.buttonBuy.setEnabled(false);
            binding.buttonBuy.setAlpha(0.7f);
            binding.buttonBuy.setText("Загрузка...");
            return;
        }

        if (currentListing != null) {
            updateBuyButton(currentListing);
        } else {
            binding.buttonBuy.setEnabled(false);
            binding.buttonBuy.setAlpha(0.5f);
            binding.buttonBuy.setText("Недоступно");
        }
    }

    private void openSellerProfile() {
        if (currentListing == null || currentListing.getSellerId() == null) {
            return;
        }

        Intent intent = new Intent(ListingDetailActivity.this, SellerProfileActivity.class);
        intent.putExtra("sellerId", currentListing.getSellerId());
        startActivity(intent);
    }

    private void showDeactivateConfirm() {
        if (currentListing == null || currentListing.getId() == null) {
            return;
        }

        if (!"ACTIVE".equals(currentListing.getStatus())) {
            Toast.makeText(this, "Снять можно только активное объявление", Toast.LENGTH_SHORT).show();
            return;
        }

        DarkDialog.showConfirm(
                this,
                "Снять объявление с публикации?",
                "Объявление больше не будет отображаться другим пользователям, но останется в ваших объявлениях.",
                "Снять",
                "Отмена",
                () -> deactivateListing()
        );
    }

    private void deactivateListing() {
        if (currentListing == null || currentListing.getId() == null) {
            return;
        }

        setLoading(true);

        listingApi.deactivateListing(currentListing.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                setLoading(false);

                if (!response.isSuccessful()) {
                    String errorMessage = "Не удалось снять объявление: " + response.code();
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
                            ListingDetailActivity.this,
                            "Ошибка",
                            "Не удалось снять объявление с публикации"
                    );
                    return;
                }

                Toast.makeText(
                        ListingDetailActivity.this,
                        "Объявление снято с публикации",
                        Toast.LENGTH_SHORT
                ).show();

                // Refresh the listing
                loadListing();
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                setLoading(false);

                String msg = t.getMessage();
                if (msg != null && (msg.contains("Unable to resolve host") || msg.contains("ECONNREFUSED"))) {
                    Toast.makeText(
                            ListingDetailActivity.this,
                            "Нет соединения с сервером",
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(
                            ListingDetailActivity.this,
                            "Ошибка сети: " + msg,
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}