package com.example.yopin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private List<Review> reviews;
    private Context context;
    private int currentUserId;
    private OnReviewEditClickListener onReviewEditClickListener;

    public ReviewAdapter(List<Review> reviews, Context context, int currentUserId) {
        this.reviews = reviews;
        this.context = context;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);
        
        holder.tvUsername.setText(review.getUsername());
        holder.tvReviewDate.setText(review.getDate());
        holder.tvReviewText.setText(review.getReviewText());
        
        // Установка рейтинга
        int rating = review.getRating();
        if (rating > 0) {
            holder.ratingBar.setVisibility(View.VISIBLE);
            holder.ratingBar.setRating(rating);
        } else {
            holder.ratingBar.setVisibility(View.GONE);
        }
        
        // Показать кнопку редактирования, только если это отзыв текущего пользователя
        if (currentUserId == review.getUserId()) {
            holder.btnEditReview.setVisibility(View.VISIBLE);
            holder.btnEditReview.setOnClickListener(v -> {
                if (onReviewEditClickListener != null) {
                    onReviewEditClickListener.onReviewEditClick(review);
                }
            });
        } else {
            holder.btnEditReview.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void setOnReviewEditClickListener(OnReviewEditClickListener listener) {
        this.onReviewEditClickListener = listener;
    }

    public interface OnReviewEditClickListener {
        void onReviewEditClick(Review review);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvReviewDate, tvReviewText;
        RatingBar ratingBar;
        Button btnEditReview;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnEditReview = itemView.findViewById(R.id.btnEditReview);
        }
    }
} 