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

public class UserReviewAdapter extends RecyclerView.Adapter<UserReviewAdapter.UserReviewViewHolder> {
    private List<Review> reviews;
    private Context context;

    public UserReviewAdapter(List<Review> reviews, Context context) {
        this.reviews = reviews;
        this.context = context;
    }

    @NonNull
    @Override
    public UserReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_review, parent, false);
        return new UserReviewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        
        if (review.getBookId() > 0) {
            holder.tvBookInfo.setText(review.getBookTitle() + " - " + review.getBookAuthor());
            holder.tvBookInfo.setVisibility(View.VISIBLE);
        } else {
            holder.tvBookInfo.setVisibility(View.GONE);
        }
        
        holder.tvReviewText.setText(review.getReviewText());
        holder.tvReviewDate.setText(review.getDate());
        holder.ratingBar.setRating(review.getRating());
        
        // Показываем рейтинг только если он установлен
        if (review.getRating() > 0) {
            holder.ratingBar.setVisibility(View.VISIBLE);
        } else {
            holder.ratingBar.setVisibility(View.GONE);
        }
        
        // Добавляем возможность редактирования отзыва
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

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public class UserReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookInfo, tvReviewText, tvReviewDate;
        RatingBar ratingBar;

        public UserReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookInfo = itemView.findViewById(R.id.tvBookInfo);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
} 