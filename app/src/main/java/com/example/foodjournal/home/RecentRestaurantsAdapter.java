package com.example.foodjournal.home;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodjournal.R;
import com.example.foodjournal.restaurant.Restaurant;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class RecentRestaurantsAdapter extends RealmRecyclerViewAdapter<Restaurant, RecentRestaurantsAdapter.ViewHolder> {

    // INITIALIZATION
    HomeActivity activity;

    public RecentRestaurantsAdapter(HomeActivity activity, OrderedRealmCollection<Restaurant> data) {
        super(data, true);
        this.activity = activity;
    }


    // VIEW HOLDER INITIALIZATION
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView restaurantPicture;
        TextView restaurantName;

        public ViewHolder(View itemView) {
            super(itemView);
        }

        private void initializeViews() {
            restaurantPicture = itemView.findViewById(R.id.recentImageViewPicture);
            restaurantName = itemView.findViewById(R.id.recentTextViewName);
        }

        private void initializeListeners(Restaurant restaurant) {
            restaurantPicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    android.content.Intent i = new android.content.Intent(activity, com.example.foodjournal.restaurant.RestaurantDetailActivity.class);
                    i.putExtra("userUuidExtra", restaurant.getUserUuid());
                    i.putExtra("restaurantUuidExtra", restaurant.getRestaurantUuid());
                    activity.startActivity(i);
                }
            });
        }

        private void finalizeViews(Restaurant restaurant) {
            restaurantName.setText(restaurant.getName());
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

            // Display rating
            Number avg = activity.realm.where(com.example.foodjournal.review.FoodReview.class)
                    .equalTo("restaurantUuid", restaurant.getRestaurantUuid())
                    .average("rating");
            if (avg != null && avg.floatValue() > 0) {
                restaurantName.setText(String.format(java.util.Locale.getDefault(), "%s (%.1f*)", restaurant.getName(), avg.floatValue()));
            } else {
                restaurantName.setText(restaurant.getName() + " (N/A*)");
            }
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(3, super.getItemCount());
    }

    @NonNull
    @Override
    public RecentRestaurantsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = activity.getLayoutInflater().inflate(R.layout.layout_recent, parent, false);
        return new RecentRestaurantsAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentRestaurantsAdapter.ViewHolder holder, int position) {
        Restaurant restaurant = getItem(position);
        if (restaurant != null && restaurant.isValid()) {
            holder.initializeViews();
            holder.initializeListeners(restaurant);
            holder.finalizeViews(restaurant);
        }
    }
}
