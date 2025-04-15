package com.example.yopin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {
    private List<Review> reviews;
    private OnReviewClickListener listener;

    public interface OnReviewClickListener {
        void onReviewClick(int reviewId);
    }

    public ReviewsAdapter(List<Review> reviews, OnReviewClickListener listener) {
        this.reviews = reviews;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review, listener);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private TextView tvUsername;
        private TextView tvDate;
        private RatingBar ratingBar;
        private TextView tvReviewText;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvDate = itemView.findViewById(R.id.tvReviewDate);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
        }

        public void bind(final Review review, final OnReviewClickListener listener) {
            tvUsername.setText(review.getUsername());
            tvDate.setText(review.getDate());
            ratingBar.setRating(review.getRating());
            tvReviewText.setText(review.getReviewText());

            itemView.setOnClickListener(v -> listener.onReviewClick(review.getId()));
        }
    }
}