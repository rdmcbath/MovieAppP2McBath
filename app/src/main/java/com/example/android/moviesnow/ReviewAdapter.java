package com.example.android.moviesnow;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<Review> reviewsList;

    public ReviewAdapter(MovieDetail.FetchReviewTask fetchReviewTask, List<Review> reviewsList) {
        this.reviewsList = reviewsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View reviewView = layoutInflater.inflate(R.layout.review_item, parent, false);
        return new ViewHolder(reviewView);
    }

    @Override
    public void onBindViewHolder(ReviewAdapter.ViewHolder holder, int position) {
        Review review = reviewsList.get(position);

        holder.reviewContentTextView.setText(review.getContent());
        holder.reviewAuthorTextView.setText(review.getAuthor());
    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView reviewContentTextView;
        TextView reviewAuthorTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            reviewContentTextView = (TextView) itemView.findViewById(R.id.review_item_content);
            reviewAuthorTextView = (TextView) itemView.findViewById(R.id.review_item_author);

        }
    }
}
