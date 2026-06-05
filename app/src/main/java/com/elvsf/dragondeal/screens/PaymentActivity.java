package com.elvsf.dragondeal.screens;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.elvsf.dragondeal.api.OrderApi;
import com.elvsf.dragondeal.api.RetrofitClient;
import com.elvsf.dragondeal.databinding.ActivityPaymentBinding;
import com.elvsf.dragondeal.dto.MarkOrderPaidRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "orderId";
    public static final String EXTRA_LISTING_TITLE = "listingTitle";
    public static final String EXTRA_TOTAL_PRICE = "totalPrice";
    public static final String EXTRA_CURRENCY = "currency";
    public static final String EXTRA_NETWORK = "network";

    private static final String TEST_PAYMENT_ADDRESS = "0x0000000000000000000000000000000000000000";

    private ActivityPaymentBinding binding;
    private OrderApi orderApi;

    private Long orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        orderApi = RetrofitClient.getInstance(this).create(OrderApi.class);

        readIntent();
        setupUi();
        setupClicks();
    }

    private void readIntent() {
        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, 0L);
    }

    private void setupUi() {
        String listingTitle = getIntent().getStringExtra(EXTRA_LISTING_TITLE);
        String totalPrice = getIntent().getStringExtra(EXTRA_TOTAL_PRICE);
        String currency = getIntent().getStringExtra(EXTRA_CURRENCY);
        String network = getIntent().getStringExtra(EXTRA_NETWORK);

        if (listingTitle == null || listingTitle.trim().isEmpty()) {
            listingTitle = "Заказ";
        }

        if (totalPrice == null || totalPrice.trim().isEmpty()) {
            totalPrice = "0";
        }

        if (currency == null || currency.trim().isEmpty()) {
            currency = "USDT";
        }

        if (network == null || network.trim().isEmpty()) {
            network = "POLYGON";
        }

        binding.textOrderInfo.setText("Товар: " + listingTitle);
        binding.textPaymentAmount.setText(totalPrice + " " + currency);
        binding.textPaymentNetwork.setText("Сеть: " + network);
        binding.textPaymentAddress.setText(TEST_PAYMENT_ADDRESS);
    }

    private void setupClicks() {
        binding.buttonCopyAddress.setOnClickListener(view -> copyAddress());
        binding.buttonConfirmPayment.setOnClickListener(view -> confirmPayment());
    }

    private void copyAddress() {
        ClipboardManager clipboardManager =
                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clipData = ClipData.newPlainText("payment_address", TEST_PAYMENT_ADDRESS);
        clipboardManager.setPrimaryClip(clipData);

        Toast.makeText(this, "Адрес скопирован", Toast.LENGTH_SHORT).show();
    }

    private void confirmPayment() {
        String txHash = binding.editTxHash.getText().toString().trim();

        if (txHash.isEmpty()) {
            binding.editTxHash.setError("Вставьте txHash");
            return;
        }

        if (!txHash.matches("^0x[a-fA-F0-9]{64}$")) {
            binding.editTxHash.setError("txHash должен быть 0x + 64 hex-символа");
            return;
        }

        if (orderId == null || orderId == 0L) {
            Toast.makeText(this, "Некорректный orderId", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);

        MarkOrderPaidRequest request = new MarkOrderPaidRequest(txHash);

        orderApi.markOrderAsPaid(orderId, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                setLoading(false);

                if (!response.isSuccessful()) {
                    Toast.makeText(
                            PaymentActivity.this,
                            "Ошибка оплаты: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                Toast.makeText(
                        PaymentActivity.this,
                        "Оплата сохранена",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                setLoading(false);

                Toast.makeText(
                        PaymentActivity.this,
                        "Ошибка подключения: " + throwable.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        binding.progressPayment.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.buttonConfirmPayment.setEnabled(!loading);
        binding.buttonCopyAddress.setEnabled(!loading);

        if (loading) {
            binding.buttonConfirmPayment.setText("Проверяем...");
        } else {
            binding.buttonConfirmPayment.setText("Я оплатил");
        }
    }
}