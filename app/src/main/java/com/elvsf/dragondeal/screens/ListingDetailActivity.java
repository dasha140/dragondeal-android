package com.elvsf.dragondeal.screens;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.elvsf.dragondeal.BuildConfig;
import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.adapter.ListingAdapter;
import com.elvsf.dragondeal.api.ListingApi;
import com.elvsf.dragondeal.api.OrderApi;
import com.elvsf.dragondeal.databinding.ActivityListingDetailBinding;
import com.elvsf.dragondeal.dto.CreateOrderRequest;
import com.elvsf.dragondeal.dto.ListingResponse;
import com.elvsf.dragondeal.dto.OrderResponse;
import com.elvsf.dragondeal.payment.PaymentDialogFragment;
import com.elvsf.dragondeal.storage.TokenStorage;
import com.elvsf.dragondeal.ui.DarkDialog;
import com.elvsf.dragondeal.util.ApiCallback;
import com.elvsf.dragondeal.util.ImageUrlResolver;
import com.elvsf.dragondeal.viewmodel.ListingDetailViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

import static org.koin.java.KoinJavaComponent.get;

public class ListingDetailActivity extends AppCompatActivity {

    private final List<ListingResponse> otherSellerListings = new ArrayList<>();

    private ListingAdapter otherSellerListingsAdapter;
    private ActivityListingDetailBinding binding;

    private ListingDetailViewModel viewModel;
    private OrderApi orderApi;
    private TokenStorage tokenStorage;

    private Long listingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityListingDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = get(ListingDetailViewModel.class);
        orderApi = get(OrderApi.class);
        tokenStorage = new TokenStorage(this);

        setupOtherSellerListings();
        setupClicks();
        setupObservers();

        listingId = getIntent().getLongExtra("listingId", 0L);

        if (listingId == 0L) {
            Toast.makeText(this, "Некорректное объявление", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadListing();
    }

    private void setupObservers() {
        viewModel.getListing().observe(this, listing -> {
            if (listing != null) {
                renderListing(listing);
            }
        });

        viewModel.getSellerListings().observe(this, listings -> {
            otherSellerListings.clear();
            if (listings != null) {
                otherSellerListings.addAll(listings);
            }
            otherSellerListingsAdapter.notifyDataSetChanged();

            if (otherSellerListings.isEmpty()) {
                binding.textOtherSellerListingsTitle.setText("Других объявлений продавца пока нет");
            } else {
                binding.textOtherSellerListingsTitle.setText("Другие объявления продавца");
            }
        });

        viewModel.getLoading().observe(this, loading -> {
            setLoading(loading);
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
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
        ListingApi api = get(ListingApi.class);
        viewModel.loadListing(api, listingId);
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

        ListingApi api = get(ListingApi.class);
        viewModel.loadSellerListings(api, listing.getSellerId(), listing.getId());

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

    private void createOrderAndOpenPayment() {
        ListingResponse currentListing = viewModel.getListing().getValue();
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

        orderApi.createOrder(request).enqueue(new ApiCallback<OrderResponse>(this) {
            @Override
            protected void onSuccess(Response<OrderResponse> response) {
                setLoading(false);
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
            protected void showError(String message) {
                setLoading(false);

                if (message.contains("Seller cannot buy own listing")) {
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
                        message
                );

                loadListing();
            }
        });
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

        ListingResponse current = viewModel.getListing().getValue();

        if (loading) {
            binding.buttonBuy.setEnabled(false);
            binding.buttonBuy.setAlpha(0.7f);
            binding.buttonBuy.setText("Загрузка...");
            return;
        }

        if (current != null) {
            updateBuyButton(current);
        } else {
            binding.buttonBuy.setEnabled(false);
            binding.buttonBuy.setAlpha(0.5f);
            binding.buttonBuy.setText("Недоступно");
        }
    }

    private void openSellerProfile() {
        ListingResponse current = viewModel.getListing().getValue();
        if (current == null || current.getSellerId() == null) {
            return;
        }

        Intent intent = new Intent(ListingDetailActivity.this, SellerProfileActivity.class);
        intent.putExtra("sellerId", current.getSellerId());
        startActivity(intent);
    }

    private void showDeactivateConfirm() {
        ListingResponse current = viewModel.getListing().getValue();
        if (current == null || current.getId() == null) {
            return;
        }

        if (!"ACTIVE".equals(current.getStatus())) {
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
        ListingResponse current = viewModel.getListing().getValue();
        if (current == null || current.getId() == null) {
            return;
        }

        ListingApi api = get(ListingApi.class);
        viewModel.deactivateListing(api, current.getId());
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
