package com.elvsf.dragondeal.screens;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.elvsf.dragondeal.adapter.SellerReviewsAdapter;
import com.elvsf.dragondeal.api.ReviewApi;
import com.elvsf.dragondeal.api.RetrofitClient;
import com.elvsf.dragondeal.databinding.ActivitySellerReviewsBinding;
import com.elvsf.dragondeal.dto.ReviewResponse;
import com.elvsf.dragondeal.ui.DarkDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerReviewsActivity extends AppCompatActivity {

    private ActivitySellerReviewsBinding binding;
    private ReviewApi reviewApi;

    private final List<ReviewResponse> reviews = new ArrayList<>();
    private SellerReviewsAdapter reviewsAdapter;

    private Long sellerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySellerReviewsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reviewApi = RetrofitClient.getInstance(this).create(ReviewApi.class);

        sellerId = getIntent().getLongExtra("sellerId", 0L);
        String sellerName = getIntent().getStringExtra("sellerName");

        if (sellerId == null || sellerId == 0L) {
            showError("Продавец не найден");
            return;
        }

        if (sellerName != null && !sellerName.trim().isEmpty()) {
            binding.textReviewsTitle.setText("Отзывы — " + sellerName);
        }

        setupBack();
        setupReviewsList();
        loadReviews();
    }

    private void setupBack() {
        binding.buttonReviewsBack.setOnClickListener(v -> finish());
    }

    private void setupReviewsList() {
        reviewsAdapter = new SellerReviewsAdapter(reviews);
        binding.recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerReviews.setAdapter(reviewsAdapter);
    }

    private void loadReviews() {
        setLoading(true);

        reviewApi.getSellerReviews(sellerId).enqueue(new Callback<List<ReviewResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReviewResponse>> call, @NonNull Response<List<ReviewResponse>> response) {
                setLoading(false);

                if (!response.isSuccessful() || response.body() == null) {
                    showError("Не удалось загрузить отзывы");
                    return;
                }

                List<ReviewResponse> result = response.body();
                reviews.clear();
                reviews.addAll(result);
                reviewsAdapter.notifyDataSetChanged();

                if (reviews.isEmpty()) {
                    binding.layoutReviewsEmpty.setVisibility(View.VISIBLE);
                    binding.recyclerReviews.setVisibility(View.GONE);
                } else {
                    binding.layoutReviewsEmpty.setVisibility(View.GONE);
                    binding.recyclerReviews.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ReviewResponse>> call, @NonNull Throwable t) {
                setLoading(false);

                String msg = t.getMessage();
                if (msg != null && (msg.contains("Unable to resolve host") || msg.contains("ECONNREFUSED"))) {
                    showError("Нет соединения с сервером");
                } else {
                    showError("Не удалось загрузить отзывы");
                }
            }
        });
    }

    private void setLoading(boolean loading) {
        if (loading) {
            binding.progressReviews.setVisibility(View.VISIBLE);
            binding.layoutReviewsEmpty.setVisibility(View.GONE);
            binding.layoutReviewsError.setVisibility(View.GONE);
            binding.recyclerReviews.setVisibility(View.GONE);
        } else {
            binding.progressReviews.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        setLoading(false);
        binding.layoutReviewsError.setVisibility(View.VISIBLE);
        binding.textReviewsError.setText(message);
        binding.recyclerReviews.setVisibility(View.GONE);
        binding.layoutReviewsEmpty.setVisibility(View.GONE);
    }
}
