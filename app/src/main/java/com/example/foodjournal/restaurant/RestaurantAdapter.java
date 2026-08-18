package com.example.foodjournal.restaurant;

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

public class RestaurantAdapter extends RealmRecyclerViewAdapter<Restaurant, RestaurantAdapter.ViewHolder> {

    // INITIALIZATION
    RestaurantListActivity activity;

    public RestaurantAdapter(RestaurantListActivity activity, OrderedRealmCollection<Restaurant> data) {
        super(data, true);
        this.activity = activity;
    }


    // VIEW HOLDER INITIALIZATION
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView restaurantPicture;
        RatingBar starRating;
        TextView restaurantName, lastVisited, averageRating;

        public ViewHolder(View itemView) {
            super(itemView);
        }

        private void initializeViews() {
            restaurantPicture = itemView.findViewById(R.id.itemImageViewPicture);
            restaurantName = itemView.findViewById(R.id.itemTextViewName);
            starRating = itemView.findViewById(R.id.restaurantDetailRatingBarStars);
            lastVisited = itemView.findViewById(R.id.itemTextViewDate);
            averageRating = itemView.findViewById(R.id.restaurantDetailTextViewStars);
        }

        private void initializeListeners(Restaurant restaurant) {
            restaurantPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(activity, RestaurantDetailActivity.class);
                    i.putExtra("userUuidExtra", restaurant.getUserUuid());
                    i.putExtra("restaurantUuidExtra", restaurant.getRestaurantUuid());
                    activity.startActivity(i);
                }
            });
        }

        private void finalizeViews(Restaurant restaurant) {
            restaurantName.setText(restaurant.getName());
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            lastVisited.setText("Last visited at " + sdf.format(restaurant.getLastVisited()));

            File getImageDir = activity.getExternalCacheDir();
            File file = new File(getImageDir, restaurant.getRestaurantUuid() + ".jpeg");
            if (file.exists()) {
                Picasso.get()
                        .load(file)
                        .networkPolicy(NetworkPolicy.NO_CACHE)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .into(restaurantPicture);
            } else {
                restaurantPicture.setImageResource(R.mipmap.ic_launcher);
            }

            // Average rating
            Number avg = activity.realm.where(com.example.foodjournal.review.FoodReview.class)
                    .equalTo("restaurantUuid", restaurant.getRestaurantUuid())
                    .average("rating");

            if (avg == null || avg.floatValue() == 0) {
                starRating.setRating(0);
                averageRating.setText("No ratings yet");
            } else {
                float rating = avg.floatValue();
                starRating.setRating(rating);
                averageRating.setText(String.format(java.util.Locale.getDefault(), "%.1f stars", rating));
            }
        }
    }

    @NonNull
    @Override
    public RestaurantAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = activity.getLayoutInflater().inflate(R.layout.layout_item, parent, false);
        return new RestaurantAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantAdapter.ViewHolder holder, int position) {
        Restaurant restaurant = getItem(position);
        if (restaurant != null && restaurant.isValid()) {
            holder.initializeViews();
            holder.initializeListeners(restaurant);
            holder.finalizeViews(restaurant);
        }
    }
}
