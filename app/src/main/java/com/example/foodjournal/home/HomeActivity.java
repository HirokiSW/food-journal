package com.example.foodjournal.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodjournal.R;
import com.example.foodjournal.authorization.User;
import com.example.foodjournal.restaurant.Restaurant;
import com.example.foodjournal.review.FoodReview;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class HomeActivity extends AppCompatActivity {

    // LIFE CYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        finalizeViews();
        if (restaurantAdapter != null) {
            restaurantAdapter.notifyDataSetChanged();
        }
        if (reviewsAdapter != null) {
            reviewsAdapter.notifyDataSetChanged();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (!realm.isClosed()) {
            realm.close();
        }
    }


    // INITIALIZATION
    Realm realm;
    ImageView profilePicture;
    TextView username;
    RecyclerView recentlyVisited, recentlyReviewed;
    Button allRestaurantsButton, randomFoodButton, myStatsButton;
    User user;
    RecentRestaurantsAdapter restaurantAdapter;
    RecentReviewsAdapter reviewsAdapter;

    private void init() {
        initializeViews();
        initializeMemory();
        initializeListeners();
        finalizeViews();
    }

    private void initializeViews() {
        profilePicture = findViewById(R.id.homeImageViewProfile);
        username = findViewById(R.id.homeTextViewUsername);
        recentlyVisited = findViewById(R.id.homeRecyclerViewVisited);
        recentlyReviewed = findViewById(R.id.homeRecyclerViewReviewed);
        allRestaurantsButton = findViewById(R.id.homeButtonRestaurants);
        randomFoodButton = findViewById(R.id.homeButtonRandomizer);
        myStatsButton = findViewById(R.id.homeButtonStats);
    }

    private void initializeMemory() {
        realm = Realm.getDefaultInstance();

        Intent i = getIntent();
        user = realm.where(User.class)
                .equalTo("uuid", i.getStringExtra("userUuidExtra"))
                .findFirst();

        RealmResults<Restaurant> restaurants = getRecentRestaurants(user.getUuid());
        restaurantAdapter = new RecentRestaurantsAdapter(this, restaurants);
        recentlyVisited.setAdapter(restaurantAdapter);

        RealmResults<FoodReview> reviews = getRecentReviews(user.getUuid());
        reviewsAdapter = new RecentReviewsAdapter(this, reviews);
        recentlyReviewed.setAdapter(reviewsAdapter);
    }

    private void initializeListeners() {
        allRestaurantsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, com.example.foodjournal.restaurant.RestaurantListActivity.class);
                i.putExtra("userUuidExtra", user.getUuid());
                startActivity(i);
            }
        });

        randomFoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RealmResults<FoodReview> reviews = realm.where(FoodReview.class)
                        .equalTo("userUuid", user.getUuid())
                        .findAll();
                if (reviews.isEmpty()) {
                    android.widget.Toast.makeText(HomeActivity.this, "No food reviews found", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    int randomIndex = (int) (Math.random() * reviews.size());
                    FoodReview randomReview = reviews.get(randomIndex);
                    Intent i = new Intent(HomeActivity.this, com.example.foodjournal.review.FoodReviewDetailActivity.class);
                    i.putExtra("userUuidExtra", user.getUuid());
                    i.putExtra("reviewUuidExtra", randomReview.getReviewUuid());
                    startActivity(i);
                }
            }
        });

        myStatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeActivity.this, StatsActivity.class);
                i.putExtra("userUuidExtra", user.getUuid());
                startActivity(i);
            }
        });
    }

    private void finalizeViews() {
        username.setText(user.getName());

        File getImageDir = getExternalCacheDir();
        File savedImage = new File(getImageDir, user.getUuid() + ".jpeg");
        if (savedImage.exists()) {
            Picasso.get()
                    .load(savedImage)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(profilePicture);
        } else {
            profilePicture.setImageResource(R.mipmap.ic_launcher);
        }
    }


    // HELPER METHODS
    private RealmResults<Restaurant> getRecentRestaurants(String userUuid) {
        return realm.where(Restaurant.class)
                .equalTo("userUuid", userUuid)
                .findAll()
                .sort("lastVisited", Sort.DESCENDING);
    }

    private RealmResults<FoodReview> getRecentReviews(String userUuid) {
        return realm.where(FoodReview.class)
                .equalTo("userUuid", userUuid)
                .findAll()
                .sort("lastEaten", Sort.DESCENDING);
    }
}