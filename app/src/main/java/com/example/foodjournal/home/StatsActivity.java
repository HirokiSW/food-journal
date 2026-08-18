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

import com.example.foodjournal.R;
import com.example.foodjournal.authorization.User;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmResults;

public class StatsActivity extends AppCompatActivity {

    // LIFE CYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();
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
    TextView username, restaurantsVisited, foodsReviewed, averageRating, mostExpensiveFood;
    TextView highestRatedFood, lowestRatedFood, highestRatedRestaurant, lowestRatedRestaurant;
    Button backButton;
    User user;

    private void init() {
        initializeViews();
        initializeMemory();
        initializeListeners();
        finalizeViews();
    }

    private void initializeViews() {
        profilePicture = findViewById(R.id.statsImageViewProfile);
        username = findViewById(R.id.statsTextViewUsername);
        restaurantsVisited = findViewById(R.id.statsTextViewRestaurantQuantity);
        foodsReviewed = findViewById(R.id.statsTextViewReviewQuantity);
        averageRating = findViewById(R.id.statsTextViewAverageRating);
        mostExpensiveFood = findViewById(R.id.statsTextViewExpensiveFood);
        highestRatedFood = findViewById(R.id.statsTextViewHighestRatedFood);
        lowestRatedFood = findViewById(R.id.statsTextViewLowestRatedFood);
        highestRatedRestaurant = findViewById(R.id.statsTextViewHighestRatedRestaurant);
        lowestRatedRestaurant = findViewById(R.id.statsTextViewLowestRatedRestaurant);
        backButton = findViewById(R.id.statsButtonBack);
    }

    private void initializeMemory() {
        realm = Realm.getDefaultInstance();

        Intent i = getIntent();
        user = realm.where(User.class)
                .equalTo("uuid", i.getStringExtra("userUuidExtra"))
                .findFirst();
    }

    private void initializeListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

        computeStats();
    }

    private void computeStats() {
        long restaurantCount = realm.where(com.example.foodjournal.restaurant.Restaurant.class)
                .equalTo("userUuid", user.getUuid())
                .count();
        restaurantsVisited.setText("Number of restaurants visited: " + restaurantCount);

        RealmResults<com.example.foodjournal.review.FoodReview> reviews = realm.where(com.example.foodjournal.review.FoodReview.class)
                .equalTo("userUuid", user.getUuid())
                .findAll();
        foodsReviewed.setText("Number of food reviewed: " + reviews.size());

        if (reviews.isEmpty()) {
            averageRating.setText("Average rating given: 0.0");
            mostExpensiveFood.setText("Most expensive food reviewed: N/A");
            highestRatedFood.setText("Highest rated food: N/A");
            lowestRatedFood.setText("Lowest rated food: N/A");
            highestRatedRestaurant.setText("Highest rated restaurant: N/A");
            lowestRatedRestaurant.setText("Lowest rated restaurant: N/A");
            return;
        }

        double totalRating = 0;
        com.example.foodjournal.review.FoodReview expensive = reviews.get(0);
        com.example.foodjournal.review.FoodReview highestFood = reviews.get(0);
        com.example.foodjournal.review.FoodReview lowestFood = reviews.get(0);

        for (com.example.foodjournal.review.FoodReview r : reviews) {
            totalRating += r.getRating();
            if (r.getPrice() > expensive.getPrice()) expensive = r;
            if (r.getRating() > highestFood.getRating()) highestFood = r;
            if (r.getRating() < lowestFood.getRating()) lowestFood = r;
        }

        averageRating.setText(String.format("Average rating given: %.1f stars", totalRating / reviews.size()));
        mostExpensiveFood.setText(String.format("Most expensive food reviewed: %s (P%.2f)", expensive.getName(), expensive.getPrice()));
        highestRatedFood.setText(String.format("Highest rated food: %s (%.1f stars)", highestFood.getName(), highestFood.getRating()));
        lowestRatedFood.setText(String.format("Lowest rated food: %s (%.1f stars)", lowestFood.getName(), lowestFood.getRating()));

        // Restaurant ratings calculation
        RealmResults<com.example.foodjournal.restaurant.Restaurant> allUserRestaurants = realm.where(com.example.foodjournal.restaurant.Restaurant.class)
                .equalTo("userUuid", user.getUuid())
                .findAll();

        com.example.foodjournal.restaurant.Restaurant bestRest = null;
        com.example.foodjournal.restaurant.Restaurant worstRest = null;
        float bestRestRating = -1;
        float worstRestRating = 6;

        for (com.example.foodjournal.restaurant.Restaurant res : allUserRestaurants) {
            Number avg = realm.where(com.example.foodjournal.review.FoodReview.class)
                    .equalTo("restaurantUuid", res.getRestaurantUuid())
                    .average("rating");
            if (avg != null) {
                float currentAvg = avg.floatValue();
                if (currentAvg > bestRestRating) {
                    bestRestRating = currentAvg;
                    bestRest = res;
                }
                if (Math.abs(currentAvg) > 0.00001f && currentAvg < worstRestRating) {
                    worstRestRating = currentAvg;
                    worstRest = res;
                }
            }
        }

        if (bestRest != null) {
            highestRatedRestaurant.setText(String.format("Highest rated restaurant: %s (%.1f stars)", bestRest.getName(), bestRestRating));
        } else {
            highestRatedRestaurant.setText("Highest rated restaurant: N/A");
        }

        if (worstRest != null) {
            lowestRatedRestaurant.setText(String.format("Lowest rated restaurant: %s (%.1f stars)", worstRest.getName(), worstRestRating));
        } else {
            lowestRatedRestaurant.setText("Lowest rated restaurant: N/A");
        }
    }
}