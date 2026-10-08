package com.elvsf.dragondeal.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.adapter.ListingAdapter;
import com.elvsf.dragondeal.adapter.TopMenuAdapter;
import com.elvsf.dragondeal.api.ListingApi;
import com.elvsf.dragondeal.databinding.ActivityMainBinding;
import com.elvsf.dragondeal.dto.ListingResponse;
import com.elvsf.dragondeal.model.MenuItemModel;
import com.elvsf.dragondeal.ui.BottomNavHelper;
import com.elvsf.dragondeal.ui.AuthGate;
import com.elvsf.dragondeal.util.ApiCallback;
import com.elvsf.dragondeal.viewmodel.ListingsViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.koin.java.KoinJavaComponent.get;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    private ListingsViewModel listingsViewModel;

    private ListingAdapter listingAdapter;
    private final List<ListingResponse> listingList = new ArrayList<>();

    private TopMenuAdapter topMenuAdapter;
    private final List<MenuItemModel> topMenuList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ViewModel injection via Koin
        listingsViewModel = get(ListingsViewModel.class);

        setupTopMenu();
        setupListings();
        setupSearch();
        setupAddListingButton();
        setupObservers();
        setupBottomNavigation();

        BottomNavHelper.setup(this, BottomNavHelper.Tab.HOME);
        loadListings();
    }

    private void setupObservers() {
        listingsViewModel.getLoading().observe(this, loading -> {
            // Optional: show/hide a global loading indicator
        });

        listingsViewModel.getListings().observe(this, filtered -> {
            renderListings(filtered);
        });

        listingsViewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTopMenu() {
        topMenuList.clear();

        topMenuList.add(new MenuItemModel("Все", R.drawable.ic_category_grid));
        topMenuList.add(new MenuItemModel("Аккаунты", R.drawable.ic_category_accounts));
        topMenuList.add(new MenuItemModel("Скины", R.drawable.ic_category_skins));
        topMenuList.add(new MenuItemModel("Бустинг", R.drawable.ic_category_boost));
        topMenuList.add(new MenuItemModel("Донат", R.drawable.ic_category_donate));
        topMenuList.add(new MenuItemModel("Подписки", R.drawable.ic_category_subs));
        topMenuList.add(new MenuItemModel("Пополнение", R.drawable.ic_category_topup));
        topMenuList.add(new MenuItemModel("Предметы", R.drawable.ic_category_items));

        topMenuAdapter = new TopMenuAdapter(
                topMenuList,
                item -> {
                    listingsViewModel.setFilter(item.getTitle(), null);
                }
        );

        binding.topMenuRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        binding.topMenuRecycler.setAdapter(topMenuAdapter);
    }

    private void setupSearch() {
        binding.editSearchListings.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listingsViewModel.setFilter(null, s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupBottomNavigation() {
        binding.navOrders.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, OrdersActivity.class);
            intent.putExtra("ordersMode", "active");
            startActivity(intent);
        });

        binding.navHome.setOnClickListener(view -> {
            listingsViewModel.setFilter("Все", "");
            binding.editSearchListings.setText("");
        });

        binding.navSettings.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void setupListings() {
        listingAdapter = new ListingAdapter(listingList, listing -> {
            Intent intent = new Intent(MainActivity.this, ListingDetailActivity.class);
            intent.putExtra("listingId", listing.getId());
            startActivity(intent);
        });

        binding.recyclerListings.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerListings.setAdapter(listingAdapter);
    }

    private void setupAddListingButton() {
        binding.fabAddListing.setOnClickListener(view -> {
            if (!AuthGate.requireAuth(this, "Войдите, чтобы создать объявление.")) {
                return;
            }
            Intent intent = new Intent(MainActivity.this, AddListingActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadListings();
    }

    private void loadListings() {
        ListingApi api = get(ListingApi.class);
        listingsViewModel.loadListings(api);
    }

    private void renderListings(List<ListingResponse> listings) {
        listingList.clear();
        if (listings != null) {
            listingList.addAll(listings);
        }
        listingAdapter.notifyDataSetChanged();

        boolean isEmpty = listingList.isEmpty();

        binding.recyclerListings.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
            String searchQuery = listingsViewModel.getSearchQuery();
            String selectedCategory = listingsViewModel.getSelectedCategory();

            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                binding.textListingsEmpty.setText("Ничего не найдено");
            } else if (!"Все".equals(selectedCategory)) {
                binding.textListingsEmpty.setText("В этой категории пока нет объявлений");
            } else {
                binding.textListingsEmpty.setText("Пока нет активных объявлений");
            }
        }

        binding.textListingsEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }
}
