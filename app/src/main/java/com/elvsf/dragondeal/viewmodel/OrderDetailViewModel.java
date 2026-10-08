package com.elvsf.dragondeal.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.elvsf.dragondeal.api.OrderRepository;
import com.elvsf.dragondeal.api.ReviewApi;
import com.elvsf.dragondeal.dto.CreateReviewRequest;
import com.elvsf.dragondeal.dto.OrderResponse;
import com.elvsf.dragondeal.dto.ReviewResponse;
import com.elvsf.dragondeal.util.ApiCallback;

import retrofit2.Response;

/**
 * ViewModel for OrderDetailActivity.
 * Holds a single order detail and UI state.
 */
public class OrderDetailViewModel extends ViewModel {

    private final MutableLiveData<OrderResponse> order = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<OrderResponse> getOrder() {
        return order;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadOrder(OrderRepository repo, Long orderId) {
        loading.setValue(true);

        repo.findOrderById(orderId).enqueue(new ApiCallback<OrderResponse>() {
            @Override
            protected void onSuccess(Response<OrderResponse> response) {
                loading.setValue(false);
                order.setValue(response.body());
            }

            @Override
            protected void showError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }

    public void markSellerDone(OrderRepository repo, Long orderId) {
        loading.setValue(true);

        repo.markSellerDone(orderId).enqueue(new ApiCallback<Void>() {
            @Override
            protected void onSuccess(Response<Void> response) {
                loading.setValue(false);
                // Reload order to get updated status
                loadOrder(repo, orderId);
            }

            @Override
            protected void showError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }

    public void createReview(ReviewApi api, CreateReviewRequest request) {
        loading.setValue(true);

        api.createReview(request).enqueue(new ApiCallback<ReviewResponse>() {
            @Override
            protected void onSuccess(Response<ReviewResponse> response) {
                loading.setValue(false);
                // Reload order to reflect review state
            }

            @Override
            protected void showError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }
}
