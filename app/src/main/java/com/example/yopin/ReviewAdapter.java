package com.example.yopin;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private Cursor cursor;

    public ReviewAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        if (cursor.moveToPosition(position)) {
            String bookTitle = cursor.getString(cursor.getColumnIndexOrThrow("book_title"));
            String reviewText = cursor.getString(cursor.getColumnIndexOrThrow("review_text"));
            holder.bookTitleTextView.setText(bookTitle);
            holder.reviewTextView.setText(reviewText);
        }
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }
        cursor = newCursor;
        notifyDataSetChanged();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView bookTitleTextView, reviewTextView;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            bookTitleTextView = itemView.findViewById(R.id.bookTitleTextView);
            reviewTextView = itemView.findViewById(R.id.reviewTextView);
        }
    }
}