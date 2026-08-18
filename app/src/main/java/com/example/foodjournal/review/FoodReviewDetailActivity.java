package com.example.foodjournal.review;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodjournal.restaurant.Restaurant;
import com.example.foodjournal.R;
import com.example.foodjournal.authorization.User;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.realm.Realm;

public class FoodReviewDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_food_review_detail);
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
    }

    public void onDestroy() {
        super.onDestroy();
        if (!realm.isClosed()) {
            realm.close();
        }
    }


    // INITIALIZATION
    Realm realm;
    ImageView profilePicture, foodPicture;
    TextView username, foodNameAndPrice, foodRating, restaurantName, lastEaten, foodDescription;
    RatingBar starRating;
    Button editButton, backButton;
    User user;
    FoodReview review;
    com.example.foodjournal.restaurant.Restaurant restaurant;

    private void init() {
        initializeViews();
        initializeMemory();
        initializeListeners();
        finalizeViews();
    }

    private void initializeViews() {
        profilePicture = findViewById(R.id.foodDetailImageViewProfile);
        username = findViewById(R.id.foodDetailTextViewUsername);
        foodPicture = findViewById(R.id.foodDetailImageViewPicture);
        foodNameAndPrice = findViewById(R.id.foodDetailTextViewNameAndPrice);
        starRating = findViewById(R.id.foodDetailRatingBarStars);
        foodRating = findViewById(R.id.foodDetailTextViewStars);
        restaurantName = findViewById(R.id.foodDetailTextViewRestaurant);
        lastEaten = findViewById(R.id.foodDetailTextViewDate);
        foodDescription = findViewById(R.id.foodDetailTextViewDescription);
        editButton = findViewById(R.id.foodDetailButtonEdit);
        backButton = findViewById(R.id.foodDetailButtonBack);
    }

    private void initializeMemory() {
        realm = Realm.getDefaultInstance();

        Intent i = getIntent();
        user = realm.where(User.class)
                .equalTo("uuid", i.getStringExtra("userUuidExtra"))
                .findFirst();

        review = realm.where(FoodReview.class)
                .equalTo("reviewUuid", i.getStringExtra("reviewUuidExtra"))
                .findFirst();

        restaurant = realm.where(com.example.foodjournal.restaurant.Restaurant.class)
                .equalTo("restaurantUuid", review.getRestaurantUuid())
                .findFirst();
    }

    private void initializeListeners() {
        editButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                Intent i = new Intent(FoodReviewDetailActivity.this, EditFoodReviewActivity.class);
                i.putExtra("userUuidExtra", user.getUuid());
                i.putExtra("reviewUuidExtra", review.getReviewUuid());
                startActivity(i);
            }
        });

        backButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
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

        foodNameAndPrice.setText(String.format(Locale.getDefault(), "%s (P%.2f)", review.getName(), review.getPrice()));
        starRating.setRating(review.getRating());
        foodRating.setText(String.format(Locale.getDefault(), "%.1f stars", review.getRating()));
        restaurantName.setText(String.format("At %s", restaurant.getName()));
        foodDescription.setText(review.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        lastEaten.setText(String.format("Last eaten on %s", sdf.format(review.getLastEaten())));

        File resImage = new File(getImageDir, review.getReviewUuid() + ".jpeg");
        if (resImage.exists()) {
            Picasso.get()
                    .load(resImage)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(foodPicture);
        } else {
            foodPicture.setImageResource(R.mipmap.ic_launcher);
        }
    }
}