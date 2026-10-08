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
 * ViewModel for MainActivity listings screen.
 * Holds all active listings, applies category/search filters in-memory.
 */
public class ListingsViewModel extends ViewModel {

    private final MutableLiveData<List<ListingResponse>> listings = new MutableLiveData<>();
    private final MutableLiveData<List<ListingResponse>> allListings = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private String selectedCategory = "Все";
    private String searchQuery = "";

    public LiveData<List<ListingResponse>> getListings() {
        return listings;
    }

    public LiveData<List<ListingResponse>> getAllListings() {
        return allListings;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadListings(ListingApi api) {
        loading.setValue(true);

        api.findActiveListings().enqueue(new ApiCallback<List<ListingResponse>>() {
            @Override
            protected void onSuccess(Response<List<ListingResponse>> response) {
                loading.setValue(false);
                List<ListingResponse> result = response.body();
                if (result == null) {
                    result = new ArrayList<>();
                }
                allListings.setValue(new ArrayList<>(result));
                applyFilters();
            }

            @Override
            protected void showError(String message) {
                loading.setValue(false);
                error.setValue(message);
            }
        });
    }

    public void setFilter(String category, String query) {
        if (category != null) {
            this.selectedCategory = category;
        }
        if (query != null) {
            this.searchQuery = query;
        }
        applyFilters();
    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    /**
     * Returns the currently filtered list based on selectedCategory and searchQuery.
     */
    public List<ListingResponse> getFilteredListings() {
        List<ListingResponse> source = allListings.getValue();
        if (source == null) {
            return new ArrayList<>();
        }

        String cleanQuery = searchQuery == null ? "" : searchQuery.trim().toLowerCase();
        String cleanCategory = selectedCategory == null ? "Все" : selectedCategory.trim();

        List<ListingResponse> filtered = new ArrayList<>();
        for (ListingResponse listing : source) {
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
        return filtered;
    }

    private void applyFilters() {
        listings.setValue(getFilteredListings());
    }

    private boolean matchesCategory(ListingResponse listing, String category) {
        if ("Все".equalsIgnoreCase(category)) {
            return true;
        }
        String listingCategory = listing.getCategoryName();
        return listingCategory != null && !listingCategory.trim().isEmpty()
                && listingCategory.trim().equalsIgnoreCase(category);
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
}
