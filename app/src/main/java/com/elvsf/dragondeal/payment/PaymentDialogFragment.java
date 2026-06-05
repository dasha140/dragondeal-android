package com.elvsf.dragondeal.payment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.adapter.PaymentMethodAdapter;
import com.elvsf.dragondeal.api.payment.PaymentIntentResponse;
import com.elvsf.dragondeal.api.payment.PaymentMethodResponse;
import com.elvsf.dragondeal.api.payment.PaymentRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentDialogFragment extends DialogFragment {

    private static final String ARG_ORDER_ID = "order_id";
    private static final String ARG_ORDER_TITLE = "order_title";
    private static final String ARG_ORDER_AMOUNT = "order_amount";

    private Long orderId;
    private String orderTitle;
    private String orderAmount;

    private PaymentRepository paymentRepository;
    private PaymentMethodAdapter paymentMethodAdapter;

    private TextView textPaymentOrderInfo;
    private TextView textPaymentIntentInfo;
    private EditText editEscrowDealId;
    private EditText editPaymentTxHash;
    private EditText editReleaseTxHash;
    private ProgressBar progressPayment;
    private Button buttonCreatePaymentIntent;

    private PaymentIntentResponse currentPaymentIntent;

    public static PaymentDialogFragment newInstance(
            Long orderId,
            String orderTitle,
            String orderAmount
    ) {
        PaymentDialogFragment fragment = new PaymentDialogFragment();

        Bundle args = new Bundle();
        args.putLong(ARG_ORDER_ID, orderId);
        args.putString(ARG_ORDER_TITLE, orderTitle);
        args.putString(ARG_ORDER_AMOUNT, orderAmount);

        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        readArguments();

        paymentRepository = new PaymentRepository(requireContext());

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_payment, null);

        bindViews(view);
        setupRecyclerView(view);
        setupButtons(view);
        applyNavBarInsets(view);
        loadPaymentMethods();

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        return dialog;
    }

    private void readArguments() {
        Bundle args = getArguments();

        if (args == null) {
            throw new IllegalArgumentException("Payment args are required");
        }

        orderId = args.getLong(ARG_ORDER_ID);
        orderTitle = args.getString(ARG_ORDER_TITLE, "");
        orderAmount = args.getString(ARG_ORDER_AMOUNT, "");
    }

    private void bindViews(View view) {
        textPaymentOrderInfo = view.findViewById(R.id.textPaymentOrderInfo);
        textPaymentIntentInfo = view.findViewById(R.id.textPaymentIntentInfo);
        editEscrowDealId = view.findViewById(R.id.editEscrowDealId);
        editPaymentTxHash = view.findViewById(R.id.editPaymentTxHash);
        editReleaseTxHash = view.findViewById(R.id.editReleaseTxHash);
        progressPayment = view.findViewById(R.id.progressPayment);
        buttonCreatePaymentIntent = view.findViewById(R.id.buttonCreatePaymentIntent);

        textPaymentOrderInfo.setText(
                "Заказ #" + orderId + "\n" +
                        orderTitle + "\n" +
                        "Сумма: " + orderAmount
        );
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerPaymentMethods = view.findViewById(R.id.recyclerPaymentMethods);

        paymentMethodAdapter = new PaymentMethodAdapter(paymentMethod -> {
            showToast(paymentMethod.getCurrency() + " · " + formatNetwork(paymentMethod.getNetwork()));
        });

        // Enable/disable "Create payment" button based on selection
        paymentMethodAdapter.setSelectionChangeListener(hasSelection -> {
            if (buttonCreatePaymentIntent != null) {
                buttonCreatePaymentIntent.setEnabled(hasSelection);
            }
        });

        recyclerPaymentMethods.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPaymentMethods.setAdapter(paymentMethodAdapter);
    }

    private void setupButtons(View view) {
        Button buttonSubmitPaymentTx = view.findViewById(R.id.buttonSubmitPaymentTx);
        Button buttonVerifyPayment = view.findViewById(R.id.buttonVerifyPayment);
        Button buttonSellerDone = view.findViewById(R.id.buttonSellerDone);
        Button buttonCompleteOrder = view.findViewById(R.id.buttonCompleteOrder);

        buttonCreatePaymentIntent.setOnClickListener(v -> createPaymentIntent());
        buttonSubmitPaymentTx.setOnClickListener(v -> submitPaymentTx());
        buttonVerifyPayment.setOnClickListener(v -> verifyPayment());
        buttonSellerDone.setOnClickListener(v -> markSellerDone());
        buttonCompleteOrder.setOnClickListener(v -> completeOrder());
    }

    /** Apply bottom padding for navigation bar so buttons don't get hidden. */
    private void applyNavBarInsets(View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            int ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    Math.max(systemBars, ime) + 32
            );
            return insets;
        });
    }

    private void loadPaymentMethods() {
        setLoading(true);

        paymentRepository.getEnabledPaymentMethods().enqueue(new Callback<List<PaymentMethodResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<List<PaymentMethodResponse>> call,
                    @NonNull Response<List<PaymentMethodResponse>> response
            ) {
                setLoading(false);

                if (!response.isSuccessful()) {
                    String errorMsg = extractErrorMessage(response);
                    showToast("Не удалось загрузить способы оплаты: " + errorMsg);
                    return;
                }

                List<PaymentMethodResponse> methods = response.body();
                if (methods == null) {
                    showToast("Не удалось загрузить способы оплаты");
                    return;
                }

                if (methods.isEmpty()) {
                    showToast("Нет доступных способов оплаты");
                    return;
                }

                paymentMethodAdapter.setPaymentMethods(methods);
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<PaymentMethodResponse>> call,
                    @NonNull Throwable t
            ) {
                setLoading(false);
                showToast("Нет подключения к серверу. Проверьте интернет и запущен ли backend.");
            }
        });
    }

    private void createPaymentIntent() {
        PaymentMethodResponse selectedPaymentMethod = paymentMethodAdapter.getSelectedPaymentMethod();

        if (selectedPaymentMethod == null) {
            showToast("Выбери способ оплаты");
            return;
        }

        setLoading(true);

        paymentRepository.createPaymentIntent(orderId, selectedPaymentMethod.getId())
                .enqueue(new Callback<PaymentIntentResponse>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<PaymentIntentResponse> call,
                            @NonNull Response<PaymentIntentResponse> response
                    ) {
                        setLoading(false);

                        if (!response.isSuccessful()) {
                            String errorMsg = extractErrorMessage(response);
                            showToast("Не удалось создать оплату: " + errorMsg);
                            return;
                        }

                        PaymentIntentResponse body = response.body();
                        if (body == null) {
                            showToast("Не удалось создать оплату");
                            return;
                        }

                        currentPaymentIntent = body;
                        renderPaymentIntent(currentPaymentIntent);
                        showToast("Оплата создана — выполни транзакцию в кошельке");
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<PaymentIntentResponse> call,
                            @NonNull Throwable t
                    ) {
                        setLoading(false);
                        showToast("Нет подключения к серверу. Проверь интернет.");
                    }
                });
    }

    private void submitPaymentTx() {
        if (currentPaymentIntent == null) {
            showToast("Сначала создай оплату");
            return;
        }

        String escrowDealId = editEscrowDealId.getText().toString().trim();
        String paymentTxHash = editPaymentTxHash.getText().toString().trim();

        if (escrowDealId.isEmpty()) {
            editEscrowDealId.setError("Введи escrow deal id");
            return;
        }

        if (paymentTxHash.isEmpty()) {
            editPaymentTxHash.setError("Введи payment tx hash");
            return;
        }

        if (!paymentTxHash.matches("^0x[a-fA-F0-9]{64}$")) {
            editPaymentTxHash.setError("txHash: 0x + 64 hex-символа");
            return;
        }

        setLoading(true);

        paymentRepository.submitPaymentTx(
                currentPaymentIntent.getId(),
                escrowDealId,
                paymentTxHash
        ).enqueue(new Callback<PaymentIntentResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<PaymentIntentResponse> call,
                    @NonNull Response<PaymentIntentResponse> response
            ) {
                setLoading(false);

                if (!response.isSuccessful()) {
                    String errorMsg = extractErrorMessage(response);
                    showToast("Не удалось сохранить tx: " + errorMsg);
                    return;
                }

                PaymentIntentResponse body = response.body();
                if (body == null) {
                    showToast("Не удалось сохранить tx");
                    return;
                }

                currentPaymentIntent = body;
                renderPaymentIntent(currentPaymentIntent);
                showToast("Tx сохранён");
            }

            @Override
            public void onFailure(
                    @NonNull Call<PaymentIntentResponse> call,
                    @NonNull Throwable t
            ) {
                setLoading(false);
                showToast("Нет подключения к серверу. Проверь интернет.");
            }
        });
    }

    private void verifyPayment() {
        if (currentPaymentIntent == null) {
            showToast("Сначала создай оплату");
            return;
        }

        if ("PAID".equals(currentPaymentIntent.getStatus())) {
            showToast("Оплата уже подтверждена");
            return;
        }

        setLoading(true);

        paymentRepository.verifyPayment(currentPaymentIntent.getId())
                .enqueue(new Callback<PaymentIntentResponse>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<PaymentIntentResponse> call,
                            @NonNull Response<PaymentIntentResponse> response
                    ) {
                        setLoading(false);

                        if (!response.isSuccessful()) {
                            String errorMsg = extractErrorMessage(response);
                            showToast("Оплата не подтверждена: " + errorMsg);
                            return;
                        }

                        PaymentIntentResponse body = response.body();
                        if (body == null) {
                            showToast("Ошибка проверки оплаты");
                            return;
                        }

                        currentPaymentIntent = body;
                        renderPaymentIntent(currentPaymentIntent);

                        if ("PAID".equals(currentPaymentIntent.getStatus())) {
                            showToast("Оплата подтверждена!");
                        } else {
                            showToast("Статус: " + currentPaymentIntent.getStatus());
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<PaymentIntentResponse> call,
                            @NonNull Throwable t
                    ) {
                        setLoading(false);

                        String userMsg;
                        String msg = t.getMessage();
                        if (msg != null && (msg.contains("Connect") || msg.contains("connect") || msg.contains("failed to connect"))) {
                            userMsg = "Локальный escrow недоступен. Запустите Hardhat node и backend.";
                        } else {
                            userMsg = "Ошибка сети: " + (msg != null ? msg : "неизвестная");
                        }
                        showToast(userMsg);
                    }
                });
    }

    private void markSellerDone() {
        setLoading(true);

        paymentRepository.markSellerDone(orderId)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<Void> call,
                            @NonNull Response<Void> response
                    ) {
                        setLoading(false);

                        if (!response.isSuccessful()) {
                            String errorMsg = extractErrorMessage(response);
                            showToast("Не удалось отметить выполнение: " + errorMsg);
                            return;
                        }

                        showToast("Продавец отметил выполнение");
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Void> call,
                            @NonNull Throwable t
                    ) {
                        setLoading(false);
                        showToast("Нет подключения к серверу. Проверь интернет.");
                    }
                });
    }

    private void completeOrder() {
        String releaseTxHash = editReleaseTxHash.getText().toString().trim();

        if (releaseTxHash.isEmpty()) {
            editReleaseTxHash.setError("Введи release tx hash");
            return;
        }

        setLoading(true);

        paymentRepository.completeOrder(orderId, releaseTxHash)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<Void> call,
                            @NonNull Response<Void> response
                    ) {
                        setLoading(false);

                        if (!response.isSuccessful()) {
                            String errorMsg = extractErrorMessage(response);
                            showToast("Не удалось завершить заказ: " + errorMsg);
                            return;
                        }

                        showToast("Заказ завершён");
                        dismiss();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Void> call,
                            @NonNull Throwable t
                    ) {
                        setLoading(false);
                        showToast("Нет подключения к серверу. Проверь интернет.");
                    }
                });
    }

    private void renderPaymentIntent(PaymentIntentResponse paymentIntent) {
        String info =
                "PaymentIntent #" + paymentIntent.getId() + "\n" +
                        "Статус: " + paymentIntent.getStatus() + "\n" +
                        "Сумма: " + paymentIntent.getAmount() + " " + paymentIntent.getCurrency() + "\n" +
                        "Atomic: " + paymentIntent.getAmountAtomic() + "\n" +
                        "Network: " + formatNetwork(paymentIntent.getNetwork()) + "\n" +
                        "Token: " + abbreviate(paymentIntent.getTokenContract()) + "\n" +
                        "Escrow: " + abbreviate(paymentIntent.getEscrowContract());

        textPaymentIntentInfo.setText(info);
    }

    private void setLoading(boolean loading) {
        progressPayment.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    /** Extract human-readable error message from Retrofit response. */
    private String extractErrorMessage(Response<?> response) {
        int code = response.code();
        switch (code) {
            case 400: return "Неверные данные запроса";
            case 401: return "Не авторизован";
            case 403: return "Нет доступа";
            case 404: return "Не найдено";
            case 500: return "Ошибка сервера";
            case 503: return "Сервер недоступен";
            default: return "Код " + code;
        }
    }

    private static String formatNetwork(String network) {
        if (network == null) return "";
        switch (network) {
            case "HARDHAT_LOCAL": return "Hardhat Local";
            case "POLYGON": return "Polygon";
            case "POLYGON_AMOY": return "Polygon Amoy";
            default: return network;
        }
    }

    private static String abbreviate(String s) {
        if (s == null || s.length() < 10) return s;
        return s.substring(0, 6) + "..." + s.substring(s.length() - 4);
    }
}
