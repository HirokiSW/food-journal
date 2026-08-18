package com.example.foodjournal.home;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodjournal.R;
import com.example.foodjournal.restaurant.Restaurant;
import com.example.foodjournal.review.FoodReview;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class RecentReviewsAdapter extends RealmRecyclerViewAdapter<FoodReview, RecentReviewsAdapter.ViewHolder> {

    // INITIALIZATION
    HomeActivity activity;

    public RecentReviewsAdapter(HomeActivity activity, OrderedRealmCollection<FoodReview> data) {
        super(data, true);
        this.activity = activity;
    }


    // VIEW HOLDER INITIALIZATION
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView foodPicture;
        TextView foodName;

        public ViewHolder(View itemView) {
            super(itemView);
        }

        private void initializeViews() {
            foodPicture = itemView.findViewById(R.id.recentImageViewPicture);
            foodName = itemView.findViewById(R.id.recentTextViewName);
        }

        private void initializeListeners(FoodReview foodReview) {
            foodPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    android.content.Intent i = new android.content.Intent(activity, com.example.foodjournal.review.FoodReviewDetailActivity.class);
                    i.putExtra("userUuidExtra", foodReview.getUserUuid());
                    i.putExtra("reviewUuidExtra", foodReview.getReviewUuid());
                    activity.startActivity(i);
                }
            });
        }

        private void finalizeViews(FoodReview foodReview) {
            foodName.setText(String.format(java.util.Locale.getDefault(), "%s (%.1f*)", foodReview.getName(), foodReview.getRating()));
            File getImageDir = activity.getExternalCacheDir();
            File file = new File(getImageDir, foodReview.getReviewUuid() + ".jpeg");
            if (file.exists()) {
                Picasso.get()
                        .load(file)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into(foodPicture);
            } else {
                foodPicture.setImageResource(R.mipmap.ic_launcher);
            }
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(3, super.getItemCount());
    }

    @NonNull
    @Override
    public RecentReviewsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = activity.getLayoutInflater().inflate(R.layout.layout_recent, parent, false);
        return new RecentReviewsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentReviewsAdapter.ViewHolder holder, int position) {
        FoodReview foodReview = getItem(position);
        if (foodReview != null && foodReview.isValid()) {
            holder.initializeViews();
            holder.initializeListeners(foodReview);
            holder.finalizeViews(foodReview);
        }
    }
}
