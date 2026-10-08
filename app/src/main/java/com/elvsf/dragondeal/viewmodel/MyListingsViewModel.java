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
 * ViewModel for MyListingsActivity.
 * Holds the current user's listings and UI state.
 */
public class MyListingsViewModel extends ViewModel {

    private final MutableLiveData<List<ListingResponse>> listings = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<List<ListingResponse>> getListings() {
        return listings;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void loadMyListings(ListingApi api) {
        loading.setValue(true);

        api.findMyListings().enqueue(new ApiCallback<List<ListingResponse>>() {
            @Override
            protected void onSuccess(Response<List<ListingResponse>> response) {
                loading.setValue(false);
                List<ListingResponse> result = response.body();
                if (result == null) {
                    result = new ArrayList<>();
                }
                listings.setValue(result);
            }

            @Override
            protected void showError(String message) {
                loading.setValue(false);
            }
        });
    }

    public void deactivateListing(ListingApi api, Long listingId) {
        loading.setValue(true);

        api.deactivateListing(listingId).enqueue(new ApiCallback<Void>() {
            @Override
            protected void onSuccess(Response<Void> response) {
                loading.setValue(false);
                // Reload to reflect the change
                loadMyListings(api);
            }

            @Override
            protected void showError(String message) {
                loading.setValue(false);
            }
        });
    }
}
