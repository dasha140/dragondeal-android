package com.elvsf.dragondeal.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.elvsf.dragondeal.dto.ListingResponse;
import com.elvsf.dragondeal.api.ListingApi;
import com.elvsf.dragondeal.util.ApiCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * ViewModel for ListingDetailActivity.
 * Holds a single listing detail, seller's other listings, and UI state.
 */
public class ListingDetailViewModel extends ViewModel {

    private final MutableLiveData<ListingResponse> listing = new MutableLiveData<>();
    private final MutableLiveData<List<ListingResponse>> sellerListings = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LiveData<ListingResponse> getListing() {
        return listing;
    }

    public LiveData<List<ListingResponse>> getSellerListings() {
        return sellerListings;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadListing(ListingApi api, Long listingId) {
        loading.setValue(true);

        api.findListingById(listingId).enqueue(new ApiCallback<ListingResponse>() {
            @Override
            protected void onSuccess(Response<ListingResponse> response) {
                loading.setValue(false);
                listing.setValue(response.body());
            }

            @Override
            protected void showError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }

    public void loadSellerListings(ListingApi api, Long sellerId, Long currentListingId) {
        if (sellerId == null) {
            sellerListings.setValue(new ArrayList<>());
            return;
        }

        api.findListingsBySellerId(sellerId).enqueue(new ApiCallback<List<ListingResponse>>() {
            @Override
            protected void onSuccess(Response<List<ListingResponse>> response) {
                List<ListingResponse> result = response.body();
                List<ListingResponse> filtered = new ArrayList<>();

                if (result != null) {
                    for (ListingResponse item : result) {
                        if (item != null && item.getId() != null && !item.getId().equals(currentListingId)) {
                            filtered.add(item);
                        }
                    }
                }
                sellerListings.setValue(filtered);
            }

            @Override
            protected void showError(String message) {
                sellerListings.setValue(new ArrayList<>());
            }
        });
    }

    public void deactivateListing(ListingApi api, Long listingId) {
        loading.setValue(true);

        api.deactivateListing(listingId).enqueue(new ApiCallback<Void>() {
            @Override
            protected void onSuccess(Response<Void> response) {
                loading.setValue(false);
                // Reload the listing after deactivation
                loadListing(api, listingId);
            }

            @Override
            protected void showError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }
}
