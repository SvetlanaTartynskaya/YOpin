package com.example.yopin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private List<Review> reviews;
    private Context context;
    private int currentUserId;

    public ReviewAdapter(List<Review> reviews, Context context, int currentUserId) {
        this.reviews = reviews;
        this.context = context;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.tvUsername.setText(review.getUsername());
        holder.tvReviewText.setText(review.getReviewText());
        holder.tvReviewDate.setText(review.getDate());
        holder.ratingBar.setRating(review.getRating());
        
        // Показываем рейтинг, только если он установлен
        if (review.getRating() > 0) {
            holder.ratingBar.setVisibility(View.VISIBLE);
        } else {
            holder.ratingBar.setVisibility(View.GONE);
        }
        
        // Если это отзыв текущего пользователя, позволяем его редактировать
        if (review.getUserId() == currentUserId) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent intent = new Intent(context, EditReviewActivity.class);
                    intent.putExtra("review_id", review.getId());
                    context.startActivity(intent);
                    return true;
                }
            });
            
            // Добавляем подсказку, что отзыв можно редактировать
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "Удерживайте для редактирования", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvReviewText, tvReviewDate;
        RatingBar ratingBar;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
} 