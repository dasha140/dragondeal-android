package com.elvsf.dragondeal.screens;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.elvsf.dragondeal.api.OrderRepository;
import com.elvsf.dragondeal.api.RetrofitClient;
import com.elvsf.dragondeal.api.ReviewApi;
import com.elvsf.dragondeal.databinding.ActivityOrderDetailBinding;
import com.elvsf.dragondeal.dto.CreateReviewRequest;
import com.elvsf.dragondeal.dto.OrderResponse;
import com.elvsf.dragondeal.dto.ReviewResponse;
import com.elvsf.dragondeal.payment.PaymentDialogFragment;
import com.elvsf.dragondeal.storage.TokenStorage;
import com.elvsf.dragondeal.ui.AuthGate;
import com.elvsf.dragondeal.ui.DarkDialog;
import com.elvsf.dragondeal.api.DisputeApi;
import com.elvsf.dragondeal.databinding.DialogCreateDisputeBinding;
import com.elvsf.dragondeal.dto.CreateDisputeRequest;
import com.elvsf.dragondeal.dto.DisputeResponse;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "order_id";

    private ActivityOrderDetailBinding binding;
    private OrderRepository orderRepository;
    private ReviewApi reviewApi;
    private TokenStorage tokenStorage;

    private DisputeApi disputeApi;
    private Long orderId;
    private OrderResponse currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderRepository = new OrderRepository(this);
        reviewApi = RetrofitClient.getInstance(this).create(ReviewApi.class);
        disputeApi = RetrofitClient.getInstance(this).create(DisputeApi.class);
        tokenStorage = new TokenStorage(this);

        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, 0L);

        if (orderId == null || orderId <= 0) {
            Toast.makeText(this, "Некорректный заказ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupClicks();
        loadOrder();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (orderId != null && orderId > 0) {
            loadOrder();
        }
    }

    private void setupClicks() {
        binding.buttonBack.setOnClickListener(v -> finish());

        binding.buttonOpenPayment.setOnClickListener(v -> openPayment());
        binding.buttonOpenChat.setOnClickListener(v -> openChat());
        binding.buttonSellerDone.setOnClickListener(v -> markSellerDone());
        binding.buttonReviewSeller.setOnClickListener(v -> showReviewDialog());

        binding.buttonOpenDispute.setOnClickListener(v -> showCreateDisputeDialog());
    }

    private void loadOrder() {
        setLoading(true);

        orderRepository.findOrderById(orderId).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<OrderResponse> call,
                    @NonNull Response<OrderResponse> response
            ) {
                setLoading(false);

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(
                            OrderDetailActivity.this,
                            "Не удалось загрузить заказ: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                currentOrder = response.body();
                renderOrder(currentOrder);
            }

            @Override
            public void onFailure(
                    @NonNull Call<OrderResponse> call,
                    @NonNull Throwable t
            ) {
                setLoading(false);

                Toast.makeText(
                        OrderDetailActivity.this,
                        "Ошибка сети: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void renderOrder(OrderResponse order) {
        binding.textOrderDetailTitle.setText("Заказ #" + order.getId());
        binding.textOrderDetailListing.setText(safe(order.getListingTitle(), "Без названия"));

        String price = "0";

        if (order.getTotalPrice() != null) {
            price = order.getTotalPrice().toString();
        }

        binding.textOrderDetailPrice.setText(price + " " + safe(order.getCurrency(), "USDT"));
        binding.textOrderDetailStatus.setText(safe(order.getStatus(), "UNKNOWN"));

        binding.textOrderDetailUsers.setText(
                "Покупатель: " + safe(order.getBuyerUsername(), "buyer") + "\n" +
                        "Продавец: " + safe(order.getSellerUsername(), "seller")
        );

        String txInfo =
                "Escrow contract: " + safe(order.getEscrowContract(), "-") + "\n" +
                        "Escrow deal id: " + safe(order.getEscrowDealId(), "-") + "\n" +
                        "Payment tx: " + safe(order.getPaymentTxHash(), "-") + "\n" +
                        "Release tx: " + safe(order.getReleaseTxHash(), "-") + "\n" +
                        "Created: " + safe(order.getCreatedAt(), "-") + "\n" +
                        "Paid: " + safe(order.getPaidAt(), "-") + "\n" +
                        "Seller done: " + safe(order.getSellerMarkedDoneAt(), "-") + "\n" +
                        "Completed: " + safe(order.getCompletedAt(), "-");

        binding.textOrderDetailTx.setText(txInfo);

        updateButtons(order);
    }

    private void updateButtons(OrderResponse order) {
        String status = safe(order.getStatus(), "UNKNOWN");

        boolean isBuyer = isCurrentUser(order.getBuyerId());
        boolean isSeller = isCurrentUser(order.getSellerId());

        binding.buttonOpenPayment.setVisibility(View.VISIBLE);
        binding.buttonOpenChat.setVisibility(View.VISIBLE);
        binding.buttonSellerDone.setVisibility(View.VISIBLE);
        binding.buttonOpenDispute.setVisibility(View.VISIBLE);
        binding.buttonReviewSeller.setVisibility(View.GONE);

        binding.buttonOpenPayment.setEnabled(false);
        binding.buttonSellerDone.setEnabled(false);
        binding.buttonOpenDispute.setEnabled(true);
        binding.buttonOpenDispute.setText("Открыть спор");

        if ("DISPUTED".equals(status)) {
            binding.buttonOpenPayment.setText("Заказ в споре");
            binding.buttonOpenPayment.setEnabled(false);

            binding.buttonSellerDone.setText("Заказ в споре");
            binding.buttonSellerDone.setEnabled(false);

            binding.buttonOpenDispute.setText("Спор уже открыт");
            binding.buttonOpenDispute.setEnabled(false);

            binding.buttonReviewSeller.setVisibility(View.GONE);
            return;
        }

        if (isBuyer) {
            setupBuyerButtons(status);
            return;
        }

        if (isSeller) {
            setupSellerButtons(status);
            return;
        }

        binding.buttonOpenPayment.setText("Вы не участник заказа");
        binding.buttonOpenPayment.setEnabled(false);
        binding.buttonSellerDone.setVisibility(View.GONE);
        binding.buttonOpenDispute.setVisibility(View.GONE);
        binding.buttonReviewSeller.setVisibility(View.GONE);
    }

    private void setupBuyerButtons(String status) {
        binding.buttonSellerDone.setVisibility(View.GONE);

        if ("CREATED".equals(status)) {
            binding.buttonOpenPayment.setText("Оплатить заказ");
            binding.buttonOpenPayment.setEnabled(true);
            return;
        }

        if ("PAID".equals(status)) {
            binding.buttonOpenPayment.setText("Оплата подтверждена");
            binding.buttonOpenPayment.setEnabled(true);
            return;
        }

        if ("SELLER_DONE".equals(status)) {
            binding.buttonOpenPayment.setText("Завершить заказ");
            binding.buttonOpenPayment.setEnabled(true);
            return;
        }

        if ("COMPLETED".equals(status)) {
            binding.buttonOpenPayment.setText("Заказ завершён");
            binding.buttonOpenPayment.setEnabled(false);

            binding.buttonReviewSeller.setVisibility(View.VISIBLE);
            binding.buttonReviewSeller.setEnabled(true);
            binding.buttonReviewSeller.setText("Оценить продавца");
            return;
        }

        if ("CANCELLED".equals(status)) {
            binding.buttonOpenPayment.setText("Заказ отменён");
            binding.buttonOpenPayment.setEnabled(false);
            return;
        }

        binding.buttonOpenPayment.setText("Оплата");
        binding.buttonOpenPayment.setEnabled(true);
    }

    private void setupSellerButtons(String status) {
        binding.buttonOpenPayment.setVisibility(View.GONE);
        binding.buttonReviewSeller.setVisibility(View.GONE);

        if ("CREATED".equals(status)) {
            binding.buttonSellerDone.setText("Ожидается оплата покупателя");
            binding.buttonSellerDone.setEnabled(false);
            return;
        }

        if ("PAID".equals(status)) {
            binding.buttonSellerDone.setText("Я выполнил заказ");
            binding.buttonSellerDone.setEnabled(true);
            return;
        }

        if ("SELLER_DONE".equals(status)) {
            binding.buttonSellerDone.setText("Ожидается подтверждение покупателя");
            binding.buttonSellerDone.setEnabled(false);
            return;
        }

        if ("COMPLETED".equals(status)) {
            binding.buttonSellerDone.setText("Заказ завершён");
            binding.buttonSellerDone.setEnabled(false);
            return;
        }

        if ("CANCELLED".equals(status)) {
            binding.buttonSellerDone.setText("Заказ отменён");
            binding.buttonSellerDone.setEnabled(false);
            return;
        }

        binding.buttonSellerDone.setText("Продавец выполнил");
        binding.buttonSellerDone.setEnabled(false);
    }

    private void showReviewDialog() {
        if (currentOrder == null || currentOrder.getId() == null) {
            Toast.makeText(this, "Заказ ещё не загружен", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!"COMPLETED".equals(currentOrder.getStatus())) {
            Toast.makeText(this, "Оценить можно только завершённый заказ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isCurrentUser(currentOrder.getBuyerId())) {
            Toast.makeText(this, "Оценить продавца может только покупатель", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(22), dp(22), dp(18));
        root.setBackground(makeBg("#121212", 24));

        TextView title = new TextView(this);
        title.setText("Оценить продавца");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);

        TextView subtitle = new TextView(this);
        subtitle.setText("Продавец: " + safe(currentOrder.getSellerUsername(), "seller"));
        subtitle.setTextColor(Color.parseColor("#9E9E9E"));
        subtitle.setTextSize(15);
        subtitle.setPadding(0, dp(8), 0, dp(14));

        TextView stars = new TextView(this);
        stars.setText("★★★★★");
        stars.setTextColor(Color.parseColor("#FF3B30"));
        stars.setTextSize(34);
        stars.setGravity(Gravity.CENTER);
        stars.setPadding(0, dp(6), 0, dp(8));

        final int[] selectedRating = {5};

        stars.setOnClickListener(view -> {
            selectedRating[0]--;

            if (selectedRating[0] < 1) {
                selectedRating[0] = 5;
            }

            stars.setText(buildStars(selectedRating[0]));
        });

        EditText comment = new EditText(this);
        comment.setHint("Комментарий");
        comment.setTextColor(Color.WHITE);
        comment.setHintTextColor(Color.parseColor("#8A8A8A"));
        comment.setTextSize(15);
        comment.setMinLines(3);
        comment.setGravity(Gravity.TOP);
        comment.setBackground(makeBg("#1A1A1A", 16));
        comment.setPadding(dp(14), dp(12), dp(14), dp(12));

        LinearLayout.LayoutParams commentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(110)
        );
        commentParams.setMargins(0, dp(12), 0, dp(14));
        comment.setLayoutParams(commentParams);

        TextView saveButton = new TextView(this);
        saveButton.setText("Отправить оценку");
        saveButton.setTextColor(Color.WHITE);
        saveButton.setTextSize(16);
        saveButton.setTypeface(null, Typeface.BOLD);
        saveButton.setGravity(Gravity.CENTER);
        saveButton.setBackground(makeBg("#FF3B30", 16));

        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52)
        );
        saveParams.setMargins(0, dp(8), 0, 0);
        saveButton.setLayoutParams(saveParams);

        TextView cancelButton = new TextView(this);
        cancelButton.setText("Отмена");
        cancelButton.setTextColor(Color.parseColor("#FF3B30"));
        cancelButton.setTextSize(16);
        cancelButton.setGravity(Gravity.CENTER);
        cancelButton.setBackground(makeStrokeBg("#121212", "#FF3B30", 16));

        LinearLayout.LayoutParams cancelParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(52)
        );
        cancelParams.setMargins(0, dp(10), 0, 0);
        cancelButton.setLayoutParams(cancelParams);

        cancelButton.setOnClickListener(view -> dialog.dismiss());

        saveButton.setOnClickListener(view -> {
            String text = comment.getText().toString().trim();
            sendReview(dialog, selectedRating[0], text);
        });

        root.addView(title);
        root.addView(subtitle);
        root.addView(stars);
        root.addView(comment);
        root.addView(saveButton);
        root.addView(cancelButton);

        dialog.setContentView(root);
        dialog.show();

        Window window = dialog.getWindow();

        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void sendReview(Dialog dialog, int rating, String comment) {
        CreateReviewRequest request = new CreateReviewRequest(
                currentOrder.getId(),
                rating,
                comment
        );

        reviewApi.createReview(request).enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<ReviewResponse> call,
                    @NonNull Response<ReviewResponse> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    String errorMessage = "Не удалось отправить оценку: " + response.code();

                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception ignored) {
                    }

                    DarkDialog.showMessage(
                            OrderDetailActivity.this,
                            "Ошибка оценки",
                            errorMessage
                    );
                    return;
                }

                Toast.makeText(
                        OrderDetailActivity.this,
                        "Спасибо за отзыв",
                        Toast.LENGTH_SHORT
                ).show();

                binding.buttonReviewSeller.setText("Отзыв отправлен");
                binding.buttonReviewSeller.setEnabled(false);

                dialog.dismiss();
            }

            @Override
            public void onFailure(
                    @NonNull Call<ReviewResponse> call,
                    @NonNull Throwable t
            ) {
                Toast.makeText(
                        OrderDetailActivity.this,
                        "Ошибка сети: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private String buildStars(int rating) {
        StringBuilder builder = new StringBuilder();

        for (int i = 1; i <= 5; i++) {
            if (i <= rating) {
                builder.append("★");
            } else {
                builder.append("☆");
            }
        }

        return builder.toString();
    }

    private void openPayment() {
        if (!AuthGate.requireAuth(this, "Войдите, чтобы оплатить заказ.")) {
            return;
        }

        if (currentOrder == null || currentOrder.getId() == null) {
            Toast.makeText(this, "Заказ ещё не загружен", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isCurrentUser(currentOrder.getBuyerId())) {
            Toast.makeText(this, "Оплата доступна только покупателю", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("COMPLETED".equals(currentOrder.getStatus())) {
            Toast.makeText(this, "Заказ уже завершён", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = safe(currentOrder.getListingTitle(), "Заказ #" + currentOrder.getId());

        String totalPrice = "0";

        if (currentOrder.getTotalPrice() != null) {
            totalPrice = currentOrder.getTotalPrice().toString();
        }

        String currency = safe(currentOrder.getCurrency(), "USDT");

        PaymentDialogFragment dialog = PaymentDialogFragment.newInstance(
                currentOrder.getId(),
                title,
                totalPrice + " " + currency
        );

        dialog.show(getSupportFragmentManager(), "PaymentDialog");
    }

    private void openChat() {
        if (!AuthGate.requireAuth(this, "Войдите, чтобы открыть чат.")) {
            return;
        }

        if (currentOrder == null || currentOrder.getId() == null) {
            Toast.makeText(this, "Заказ ещё не загружен", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_ORDER_ID, currentOrder.getId());
        startActivity(intent);
    }

    private void markSellerDone() {
        if (currentOrder == null || currentOrder.getId() == null) {
            Toast.makeText(this, "Заказ ещё не загружен", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isCurrentUser(currentOrder.getSellerId())) {
            Toast.makeText(this, "Это действие доступно только продавцу", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!"PAID".equals(currentOrder.getStatus())) {
            Toast.makeText(this, "Отметить выполнение можно только после оплаты", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        orderRepository.markSellerDone(currentOrder.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(
                    @NonNull Call<Void> call,
                    @NonNull Response<Void> response
            ) {
                setLoading(false);

                if (!response.isSuccessful()) {
                    Toast.makeText(
                            OrderDetailActivity.this,
                            "Не удалось отметить выполнение: " + response.code(),
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                Toast.makeText(
                        OrderDetailActivity.this,
                        "Вы отметили заказ как выполненный",
                        Toast.LENGTH_SHORT
                ).show();

                loadOrder();
            }

            @Override
            public void onFailure(
                    @NonNull Call<Void> call,
                    @NonNull Throwable t
            ) {
                setLoading(false);

                Toast.makeText(
                        OrderDetailActivity.this,
                        "Ошибка сети: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private boolean isCurrentUser(Long userId) {
        if (userId == null) {
            return false;
        }

        Long currentUserId = tokenStorage.getUserId();

        if (currentUserId == null) {
            return false;
        }

        return userId.equals(currentUserId);
    }

    private void setLoading(boolean loading) {
        binding.progressOrderDetail.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String safe(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }

        return value.trim();
    }

    private GradientDrawable makeBg(String color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(color));
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private GradientDrawable makeStrokeBg(String color, String strokeColor, int radiusDp) {
        GradientDrawable drawable = makeBg(color, radiusDp);
        drawable.setStroke(dp(1), Color.parseColor(strokeColor));
        return drawable;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private void showCreateDisputeDialog() {
        if (!AuthGate.requireAuth(this, "Войдите, чтобы открыть спор.")) {
            return;
        }

        if (currentOrder == null || currentOrder.getId() == null) {
            Toast.makeText(this, "Заказ ещё не загружен", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isCurrentUser(currentOrder.getBuyerId()) && !isCurrentUser(currentOrder.getSellerId())) {
            Toast.makeText(this, "Спор могут открыть только участники заказа", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("COMPLETED".equals(currentOrder.getStatus())) {
            Toast.makeText(this, "Нельзя открыть спор по завершённому заказу", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("DISPUTED".equals(currentOrder.getStatus())) {
            Toast.makeText(this, "Спор уже открыт", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        DialogCreateDisputeBinding dialogBinding =
                DialogCreateDisputeBinding.inflate(getLayoutInflater());

        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.buttonCancelCreateDispute.setOnClickListener(view -> dialog.dismiss());

        dialogBinding.buttonConfirmCreateDispute.setOnClickListener(view -> {
            String reason = dialogBinding.editDisputeReason.getText().toString().trim();

            if (reason.isEmpty()) {
                dialogBinding.editDisputeReason.setError("Опишите причину спора");
                return;
            }

            if (reason.length() < 10) {
                dialogBinding.editDisputeReason.setError("Напишите подробнее");
                return;
            }

            createDispute(dialog, dialogBinding, reason);
        });

        dialog.show();
    }

    private void createDispute(
            BottomSheetDialog dialog,
            DialogCreateDisputeBinding dialogBinding,
            String reason
    ) {
        dialogBinding.progressCreateDispute.setVisibility(View.VISIBLE);
        dialogBinding.buttonConfirmCreateDispute.setEnabled(false);
        dialogBinding.buttonCancelCreateDispute.setEnabled(false);

        CreateDisputeRequest request = new CreateDisputeRequest(
                currentOrder.getId(),
                reason
        );

        disputeApi.createDispute(request).enqueue(new Callback<DisputeResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<DisputeResponse> call,
                    @NonNull Response<DisputeResponse> response
            ) {
                dialogBinding.progressCreateDispute.setVisibility(View.GONE);
                dialogBinding.buttonConfirmCreateDispute.setEnabled(true);
                dialogBinding.buttonCancelCreateDispute.setEnabled(true);

                if (!response.isSuccessful() || response.body() == null) {
                    String errorMessage = "Не удалось открыть спор: " + response.code();

                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception ignored) {
                    }

                    DarkDialog.showMessage(
                            OrderDetailActivity.this,
                            "Ошибка спора",
                            errorMessage
                    );
                    return;
                }

                Toast.makeText(
                        OrderDetailActivity.this,
                        "Спор открыт",
                        Toast.LENGTH_SHORT
                ).show();

                dialog.dismiss();
                loadOrder();
            }

            @Override
            public void onFailure(
                    @NonNull Call<DisputeResponse> call,
                    @NonNull Throwable t
            ) {
                dialogBinding.progressCreateDispute.setVisibility(View.GONE);
                dialogBinding.buttonConfirmCreateDispute.setEnabled(true);
                dialogBinding.buttonCancelCreateDispute.setEnabled(true);

                Toast.makeText(
                        OrderDetailActivity.this,
                        "Ошибка сети: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

}