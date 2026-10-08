package com.elvsf.dragondeal.screens;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.elvsf.dragondeal.adapter.SellerReviewsAdapter;
import com.elvsf.dragondeal.api.ReviewApi;
import com.elvsf.dragondeal.databinding.ActivitySellerReviewsBinding;
import com.elvsf.dragondeal.dto.ReviewResponse;
import com.elvsf.dragondeal.ui.DarkDialog;
import com.elvsf.dragondeal.util.ApiCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

import static org.koin.java.KoinJavaComponent.get;

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

        reviewApi = get(ReviewApi.class);

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

        reviewApi.getSellerReviews(sellerId).enqueue(new ApiCallback<List<ReviewResponse>>(this) {
            @Override
            protected void onSuccess(Response<List<ReviewResponse>> response) {
                setLoading(false);

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
            protected void showError(String message) {
                setLoading(false);
                showError("Не удалось загрузить отзывы");
                super.showError(message);
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
