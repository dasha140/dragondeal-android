package com.elvsf.dragondeal.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.elvsf.dragondeal.adapter.ListingAdapter;
import com.elvsf.dragondeal.api.ListingApi;
import com.elvsf.dragondeal.databinding.ActivityMyListingsBinding;
import com.elvsf.dragondeal.dto.ListingResponse;
import com.elvsf.dragondeal.ui.DarkDialog;
import com.elvsf.dragondeal.viewmodel.MyListingsViewModel;

import java.util.ArrayList;
import java.util.List;

import static org.koin.java.KoinJavaComponent.get;

public class MyListingsActivity extends AppCompatActivity {

    private ActivityMyListingsBinding binding;
    private MyListingsViewModel viewModel;
    private ListingAdapter listingAdapter;
    private final List<ListingResponse> listingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMyListingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = get(MyListingsViewModel.class);

        setupRecyclerView();
        setupClicks();
        setupObservers();

        loadMyListings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyListings();
    }

    private void setupObservers() {
        viewModel.getListings().observe(this, listings -> {
            listingList.clear();
            if (listings != null) {
                listingList.addAll(listings);
            }
            listingAdapter.notifyDataSetChanged();

            binding.textMyListingsEmpty.setVisibility(
                    listingList.isEmpty() ? View.VISIBLE : View.GONE
            );
        });

        viewModel.getLoading().observe(this, this::setLoading);
    }

    private void setupRecyclerView() {
        listingAdapter = new ListingAdapter(listingList, this::showListingActions);

        binding.recyclerMyListings.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerMyListings.setAdapter(listingAdapter);
    }

    private void setupClicks() {
        binding.buttonBack.setOnClickListener(v -> finish());

        binding.buttonCreateListing.setOnClickListener(v -> {
            Intent intent = new Intent(MyListingsActivity.this, AddListingActivity.class);
            startActivity(intent);
        });
    }

    private void loadMyListings() {
        ListingApi api = get(ListingApi.class);
        viewModel.loadMyListings(api);
    }

    private void showListingActions(ListingResponse listing) {
        if (listing == null || listing.getId() == null) {
            showToast("Некорректное объявление");
            return;
        }

        openListingDetail(listing);
    }

    private void openListingDetail(ListingResponse listing) {
        Intent intent = new Intent(this, ListingDetailActivity.class);
        intent.putExtra("listingId", listing.getId());
        startActivity(intent);
    }

    private void setLoading(boolean loading) {
        binding.progressMyListings.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
