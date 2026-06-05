package com.elvsf.dragondeal.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.dto.ListingResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingViewHolder> {

    private final List<ListingResponse> listings;
    private final OnListingClickListener listener;

    public ListingAdapter(List<ListingResponse> listings) {
        this(listings, null);
    }

    public ListingAdapter(List<ListingResponse> listings, OnListingClickListener listener) {
        this.listings = listings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_listing, parent, false);

        return new ListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        ListingResponse listing = listings.get(position);

        holder.textDescription.setText(safeText(listing.getTitle(), listing.getDescription()));
        holder.textCategory.setText(buildCategoryText(listing));
        holder.textPrice.setText(formatPrice(listing));

        // Show status badge for INACTIVE (unpublished) listings
        String status = listing.getStatus();
        if ("INACTIVE".equals(status)) {
            holder.textStatus.setText("Снято с публикации");
            holder.textStatus.setVisibility(View.VISIBLE);
        } else {
            holder.textStatus.setVisibility(View.GONE);
        }

        Glide.with(holder.itemView.getContext())
                .load(primaryImageUrl(listing))
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(holder.imageListing);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onListingClick(listing);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    static class ListingViewHolder extends RecyclerView.ViewHolder {

        ImageView imageListing;
        TextView textDescription;
        TextView textCategory;
        TextView textPrice;
        TextView textStatus;

        public ListingViewHolder(@NonNull View itemView) {
            super(itemView);

            imageListing = itemView.findViewById(R.id.imageListing);
            textDescription = itemView.findViewById(R.id.textListingDescription);
            textCategory = itemView.findViewById(R.id.textListingCategory);
            textPrice = itemView.findViewById(R.id.textListingPrice);
            textStatus = itemView.findViewById(R.id.textListingStatus);
        }
    }

    public interface OnListingClickListener {
        void onListingClick(ListingResponse listing);
    }

    private String buildCategoryText(ListingResponse listing) {
        String gameName = listing.getGameName();
        String categoryName = listing.getCategoryName();

        if (gameName != null && !gameName.trim().isEmpty()
                && categoryName != null && !categoryName.trim().isEmpty()) {
            return gameName.trim() + " • " + categoryName.trim();
        }

        if (categoryName != null && !categoryName.trim().isEmpty()) {
            return categoryName.trim();
        }

        if (gameName != null && !gameName.trim().isEmpty()) {
            return gameName.trim();
        }

        return "Без категории";
    }

    private String formatPrice(ListingResponse listing) {
        String currency = safeText(listing.getCurrency(), "USDT");
        BigDecimal price = listing.getPrice();

        if (price == null) {
            return "0 " + currency;
        }

        try {
            double priceDouble = price.doubleValue();

            if (priceDouble == Math.rint(priceDouble)) {
                return String.format(Locale.getDefault(), "%.0f %s", priceDouble, currency);
            }

            return String.format(Locale.getDefault(), "%.2f %s", priceDouble, currency);
        } catch (Exception e) {
            return price + " " + currency;
        }
    }

    private String primaryImageUrl(ListingResponse listing) {
        String imageUrl = null;

        if (listing.getImageUrls() != null && !listing.getImageUrls().isEmpty()) {
            imageUrl = listing.getImageUrls().get(0);
        } else if (listing.getGameImageUrl() != null && !listing.getGameImageUrl().trim().isEmpty()) {
            imageUrl = listing.getGameImageUrl();
        }

        return resolveImageUrl(imageUrl);
    }

    private String resolveImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }

        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
            return imageUrl
                    .replace("http://127.0.0.1:8081", "http://192.168.0.111:8081")
                    .replace("http://localhost:8081", "http://192.168.0.111:8081");
        }

        if (imageUrl.startsWith("/")) {
            return "http://192.168.0.111:8081" + imageUrl;
        }

        return "http://192.168.0.111:8081/" + imageUrl;
    }

    private String safeText(String value, String fallback) {
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }

        return fallback == null ? "" : fallback;
    }
}