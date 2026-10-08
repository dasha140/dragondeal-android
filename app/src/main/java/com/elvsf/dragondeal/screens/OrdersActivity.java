package com.elvsf.dragondeal.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.adapter.OrderAdapter;
import com.elvsf.dragondeal.api.OrderRepository;
import com.elvsf.dragondeal.databinding.ActivityOrdersBinding;
import com.elvsf.dragondeal.dto.OrderResponse;
import com.elvsf.dragondeal.util.ApiCallback;
import com.elvsf.dragondeal.viewmodel.OrdersViewModel;

import java.util.List;

import retrofit2.Response;
import com.elvsf.dragondeal.ui.BottomNavHelper;

import static org.koin.java.KoinJavaComponent.get;

public class OrdersActivity extends AppCompatActivity {

    private ActivityOrdersBinding binding;
    private OrderRepository orderRepository;
    private OrderAdapter orderAdapter;
    private OrdersViewModel viewModel;

    private boolean showingBuyerOrders = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BottomNavHelper.setup(this, BottomNavHelper.Tab.ORDERS);

        viewModel = new ViewModelProvider(this).get(OrdersViewModel.class);
        orderRepository = get(OrderRepository.class);

        observeViewModel();
        setupRecyclerView();
        setupButtons();

        // Restore data after rotation if available
        if (viewModel.getBuyerOrders().getValue() != null && showingBuyerOrders) {
            renderOrders(viewModel.getBuyerOrders().getValue());
        } else if (viewModel.getSellerOrders().getValue() != null) {
            renderOrders(viewModel.getSellerOrders().getValue());
        } else {
            loadBuyerOrders();
        }
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(this, this::setLoading);
        viewModel.getBuyerOrders().observe(this, orders -> {
            if (showingBuyerOrders) {
                renderOrders(orders);
            }
        });
        viewModel.getSellerOrders().observe(this, orders -> {
            if (!showingBuyerOrders) {
                renderOrders(orders);
            }
        });
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(order -> openOrderDetail(order));

        binding.recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerOrders.setAdapter(orderAdapter);
    }

    private void setupButtons() {
        binding.buttonBuyerOrders.setOnClickListener(v -> loadBuyerOrders());
        binding.buttonSellerOrders.setOnClickListener(v -> loadSellerOrders());
        binding.buttonBrowseListings.setOnClickListener(v -> {
            Intent intent = new Intent(OrdersActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadBuyerOrders() {
        showingBuyerOrders = true;
        updateTabs();
        viewModel.setLoading(true);

        orderRepository.findMyBuyerOrders().enqueue(new ApiCallback<List<OrderResponse>>(this) {
            @Override
            protected void onSuccess(Response<List<OrderResponse>> response) {
                viewModel.setLoading(false);
                viewModel.setBuyerOrders(response.body());
                renderOrders(response.body());
            }
        });
    }

    private void loadSellerOrders() {
        showingBuyerOrders = false;
        updateTabs();
        viewModel.setLoading(true);

        orderRepository.findMySellerOrders().enqueue(new ApiCallback<List<OrderResponse>>(this) {
            @Override
            protected void onSuccess(Response<List<OrderResponse>> response) {
                viewModel.setLoading(false);
                viewModel.setSellerOrders(response.body());
                renderOrders(response.body());
            }
        });
    }

    private void renderOrders(List<OrderResponse> orders) {
        orderAdapter.setOrders(orders);

        if (orders == null || orders.isEmpty()) {
            binding.recyclerOrders.setVisibility(View.GONE);
            binding.layoutOrdersEmpty.setVisibility(View.VISIBLE);

            if (showingBuyerOrders) {
                binding.textOrdersEmpty.setText("Покупок пока нет");
                binding.textOrdersEmptySubtitle.setText("Когда вы оформите заказ, он появится здесь.");
                binding.buttonBrowseListings.setVisibility(View.VISIBLE);
            } else {
                binding.textOrdersEmpty.setText("Продаж пока нет");
                binding.textOrdersEmptySubtitle.setText("Когда кто-то купит ваш товар, заказ появится здесь.");
                binding.buttonBrowseListings.setVisibility(View.GONE);
            }
        } else {
            binding.recyclerOrders.setVisibility(View.VISIBLE);
            binding.layoutOrdersEmpty.setVisibility(View.GONE);
        }
    }

    private void openOrderDetail(OrderResponse order) {
        if (order == null || order.getId() == null) {
            showToast("Некорректный заказ");
            return;
        }

        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.getId());
        startActivity(intent);
    }

    private void updateTabs() {
        if (showingBuyerOrders) {
            binding.buttonBuyerOrders.setBackgroundResource(R.drawable.bg_tab_orders_selected);
            binding.buttonSellerOrders.setBackgroundResource(R.drawable.bg_tab_orders_unselected);
        } else {
            binding.buttonBuyerOrders.setBackgroundResource(R.drawable.bg_tab_orders_unselected);
            binding.buttonSellerOrders.setBackgroundResource(R.drawable.bg_tab_orders_selected);
        }
    }

    private void setLoading(boolean loading) {
        binding.progressOrders.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.recyclerOrders.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
