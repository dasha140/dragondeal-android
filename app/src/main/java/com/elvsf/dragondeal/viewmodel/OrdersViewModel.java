package com.elvsf.dragondeal.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.elvsf.dragondeal.dto.OrderResponse;

import java.util.List;

/**
 * ViewModel for OrdersActivity.
 * Survives configuration changes and holds order lists + UI state.
 *
 * Network calls are still managed from Activity, but this class
 * stores the data so it survives screen rotation.
 */
public class OrdersViewModel extends ViewModel {

    private final MutableLiveData<List<OrderResponse>> buyerOrders = new MutableLiveData<>();
    private final MutableLiveData<List<OrderResponse>> sellerOrders = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<List<OrderResponse>> getBuyerOrders() {
        return buyerOrders;
    }

    public LiveData<List<OrderResponse>> getSellerOrders() {
        return sellerOrders;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void setBuyerOrders(List<OrderResponse> orders) {
        buyerOrders.setValue(orders);
    }

    public void setSellerOrders(List<OrderResponse> orders) {
        sellerOrders.setValue(orders);
    }

    public void setLoading(boolean loading) {
        this.loading.setValue(loading);
    }
}
