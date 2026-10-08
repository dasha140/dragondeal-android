package com.elvsf.dragondeal.api;

import com.elvsf.dragondeal.api.payment.OrderCompleteRequest;
import com.elvsf.dragondeal.dto.CreateOrderRequest;
import com.elvsf.dragondeal.dto.MarkOrderPaidRequest;
import com.elvsf.dragondeal.dto.OrderResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;

public class OrderRepository {

    private final OrderApi orderApi;

    public OrderRepository(Retrofit retrofit) {
        orderApi = retrofit.create(OrderApi.class);
    }

    public Call<List<OrderResponse>> findMyBuyerOrders() {
        return orderApi.findMyBuyerOrders();
    }

    public Call<List<OrderResponse>> findMySellerOrders() {
        return orderApi.findMySellerOrders();
    }

    public Call<OrderResponse> findOrderById(Long orderId) {
        return orderApi.findOrderById(orderId);
    }

    public Call<OrderResponse> createOrder(Long listingId) {
        return orderApi.createOrder(new CreateOrderRequest(listingId));
    }

    public Call<Void> markOrderAsPaid(Long orderId, String paymentTxHash) {
        return orderApi.markOrderAsPaid(orderId, new MarkOrderPaidRequest(paymentTxHash));
    }

    public Call<Void> markSellerDone(Long orderId) {
        return orderApi.markSellerDone(orderId);
    }

    public Call<Void> completeOrder(Long orderId, String releaseTxHash) {
        return orderApi.completeOrder(orderId, new OrderCompleteRequest(releaseTxHash));
    }

    public Call<Void> cancelOrder(Long orderId) {
        return orderApi.cancelOrder(orderId);
    }
}