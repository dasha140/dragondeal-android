package com.elvsf.dragondeal.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elvsf.dragondeal.R;
import com.elvsf.dragondeal.dto.ReviewResponse;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SellerReviewsAdapter extends RecyclerView.Adapter<SellerReviewsAdapter.ReviewViewHolder> {

    private final List<ReviewResponse> reviews;

    public SellerReviewsAdapter(List<ReviewResponse> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);

        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewResponse review = reviews.get(position);

        holder.textAuthor.setText(safeText(review.getReviewerUsername(), "Аноним"));

        holder.textRating.setText(String.valueOf(review.getRating() != null ? review.getRating() : 0));

        String comment = review.getComment();
        if (comment != null && !comment.trim().isEmpty()) {
            holder.textComment.setText(comment.trim());
            holder.textComment.setVisibility(View.VISIBLE);
        } else {
            holder.textComment.setVisibility(View.GONE);
        }

        String date = formatReviewDate(review.getCreatedAt());
        if (date != null) {
            holder.textDate.setText(date);
        } else {
            holder.textDate.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {

        TextView textAuthor;
        TextView textRating;
        TextView textComment;
        TextView textDate;
        ImageView imageStar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            textAuthor = itemView.findViewById(R.id.textReviewAuthor);
            textRating = itemView.findViewById(R.id.textReviewRating);
            textComment = itemView.findViewById(R.id.textReviewComment);
            textDate = itemView.findViewById(R.id.textReviewDate);
            imageStar = itemView.findViewById(R.id.imageReviewStar);
        }
    }

    private String formatReviewDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return null;
        }

        try {
            String datePart = rawDate;
            if (datePart.contains("T")) {
                datePart = datePart.substring(0, datePart.indexOf("T"));
            } else if (datePart.contains(" ")) {
                datePart = datePart.substring(0, datePart.indexOf(" "));
            }

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("ru"));

            java.util.Date date = inputFormat.parse(datePart);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private String safeText(String value, String fallback) {
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        return fallback;
    }
}
