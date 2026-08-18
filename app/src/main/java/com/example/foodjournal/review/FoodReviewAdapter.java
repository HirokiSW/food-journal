package com.example.foodjournal.review;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodjournal.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class FoodReviewAdapter  extends RealmRecyclerViewAdapter<FoodReview, FoodReviewAdapter.ViewHolder> {

    // INITIALIZATION
    FoodReviewListActivity activity;

    public FoodReviewAdapter(FoodReviewListActivity activity, OrderedRealmCollection<FoodReview> data) {
        super(data, true);
        this.activity = activity;
    }


    // VIEW HOLDER INITIALIZATION
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView foodPicture;
        RatingBar starRating;
        TextView foodName, lastEaten, foodRating;

        public ViewHolder(View itemView) {
            super(itemView);
        }

        private void initializeViews() {
            foodPicture = itemView.findViewById(R.id.itemImageViewPicture);
            foodName = itemView.findViewById(R.id.itemTextViewName);
            starRating = itemView.findViewById(R.id.restaurantDetailRatingBarStars);
            lastEaten = itemView.findViewById(R.id.itemTextViewDate);
            foodRating = itemView.findViewById(R.id.restaurantDetailTextViewStars);
        }

        private void initializeListeners(FoodReview foodReview) {
            foodPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(activity, FoodReviewDetailActivity.class);
                    i.putExtra("userUuidExtra", foodReview.getUserUuid());
                    i.putExtra("reviewUuidExtra", foodReview.getReviewUuid());
                    activity.startActivity(i);
                }
            });
        }

        private void finalizeViews(FoodReview foodReview) {
            foodName.setText(foodReview.getName());
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            lastEaten.setText("Eaten on " + sdf.format(foodReview.getLastEaten()));
            starRating.setRating(foodReview.getRating());
            foodRating.setText(String.format(java.util.Locale.getDefault(), "%.1f stars", foodReview.getRating()));

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

    @NonNull
    @Override
    public FoodReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = activity.getLayoutInflater().inflate(R.layout.layout_item, parent, false);
        return new FoodReviewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodReviewAdapter.ViewHolder holder, int position) {
        FoodReview foodReview = getItem(position);
        if (foodReview != null && foodReview.isValid()) {
            holder.initializeViews();
            holder.initializeListeners(foodReview);
            holder.finalizeViews(foodReview);
        }
    }
}
