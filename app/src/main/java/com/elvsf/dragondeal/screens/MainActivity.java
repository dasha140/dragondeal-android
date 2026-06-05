package com.elvsf.dragondeal.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.adapter.ListingAdapter;
import com.elvsf.dragondeal.adapter.TopMenuAdapter;
import com.elvsf.dragondeal.api.ListingApi;
import com.elvsf.dragondeal.api.RetrofitClient;
import com.elvsf.dragondeal.databinding.ActivityMainBinding;
import com.elvsf.dragondeal.dto.ListingResponse;
import com.elvsf.dragondeal.model.MenuItemModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;import com.elvsf.dragondeal.ui.BottomNavHelper;
import com.elvsf.dragondeal.ui.AuthGate;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;

    private final List<ListingResponse> allListings = new ArrayList<>();

    private String selectedCategory = "Все";
    private String searchQuery = "";

    private ListingApi listingApi;

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

        listingApi = RetrofitClient.getInstance(this).create(ListingApi.class);

        setupTopMenu();
        setupListings();
        setupSearch();
        setupAddListingButton();
        BottomNavHelper.setup(this, BottomNavHelper.Tab.HOME);
        loadListings();
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
                    selectedCategory = item.getTitle();
                    applyFilters();
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
                searchQuery = s.toString().trim();
                applyFilters();
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
            selectedCategory = "Все";
            searchQuery = "";
            binding.editSearchListings.setText("");
            applyFilters();
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
        listingApi.findActiveListings().enqueue(new Callback<List<ListingResponse>>() {
            @Override
            public void onResponse(Call<List<ListingResponse>> call, Response<List<ListingResponse>> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(
                            MainActivity.this,
                            "Ошибка сервера: " + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }

                List<ListingResponse> listings = response.body();

                if (listings == null) {
                    listings = new ArrayList<>();
                }

                allListings.clear();
                allListings.addAll(listings);

                applyFilters();
            }

            @Override
            public void onFailure(Call<List<ListingResponse>> call, Throwable throwable) {
                Log.e(TAG, "Failed to load listings", throwable);

                Toast.makeText(
                        MainActivity.this,
                        "Не удалось загрузить объявления: " + throwable.getMessage(),
                        Toast.LENGTH_LONG
                ).show();

                allListings.clear();
                applyFilters();
            }
        });
    }

    private void applyFilters() {
        List<ListingResponse> filtered = new ArrayList<>();

        String cleanQuery = searchQuery == null ? "" : searchQuery.trim().toLowerCase();
        String cleanCategory = selectedCategory == null ? "Все" : selectedCategory.trim();

        for (ListingResponse listing : allListings) {
            if (listing == null) {
                continue;
            }

            if (!matchesCategory(listing, cleanCategory)) {
                continue;
            }

            if (!matchesSearch(listing, cleanQuery)) {
                continue;
            }

            filtered.add(listing);
        }

        renderListings(filtered);
    }

    private boolean matchesCategory(ListingResponse listing, String category) {
        if ("Все".equalsIgnoreCase(category)) {
            return true;
        }

        String listingCategory = listing.getCategoryName();

        if (listingCategory == null || listingCategory.trim().isEmpty()) {
            return false;
        }

        return listingCategory.trim().equalsIgnoreCase(category);
    }

    private boolean matchesSearch(ListingResponse listing, String query) {
        if (query == null || query.isEmpty()) {
            return true;
        }

        return contains(listing.getTitle(), query)
                || contains(listing.getDescription(), query)
                || contains(listing.getGameName(), query)
                || contains(listing.getCategoryName(), query)
                || contains(listing.getSellerUsername(), query)
                || contains(listing.getCurrency(), query)
                || contains(listing.getNetwork(), query);
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }

    private void renderListings(List<ListingResponse> listings) {
        listingList.clear();
        listingList.addAll(listings);
        listingAdapter.notifyDataSetChanged();

        boolean isEmpty = listingList.isEmpty();

        binding.recyclerListings.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        if (isEmpty) {
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