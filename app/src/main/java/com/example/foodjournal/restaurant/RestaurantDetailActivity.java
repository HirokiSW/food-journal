package com.example.foodjournal.restaurant;

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

import com.example.foodjournal.R;
import com.example.foodjournal.authorization.User;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

import io.realm.Realm;

public class RestaurantDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_detail);
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
    ImageView profilePicture, restaurantPicture;
    TextView username, restaurantName, averageRating, restaurantAddress, lastVisited, restaurantDescription;
    RatingBar starRating;
    Button foodButton, editButton, backButton;
    User user;
    Restaurant restaurant;

    private void init() {
        initializeViews();
        initializeMemory();
        initializeListeners();
        finalizeViews();
    }

    private void initializeViews() {
        profilePicture = findViewById(R.id.restaurantDetailImageViewProfile);
        username = findViewById(R.id.restaurantDetailTextViewUsername);
        restaurantPicture = findViewById(R.id.restaurantDetailImageViewPicture);
        restaurantName = findViewById(R.id.restaurantDetailTextViewName);
        starRating = findViewById(R.id.restaurantDetailRatingBarStars);
        averageRating = findViewById(R.id.restaurantDetailTextViewStars);
        restaurantAddress = findViewById(R.id.restaurantDetailTextViewAddress);
        lastVisited = findViewById(R.id.restaurantDetailTextViewDate);
        restaurantDescription = findViewById(R.id.restaurantDetailTextViewDescription);
        foodButton = findViewById(R.id.restaurantDetailButtonFood);
        editButton = findViewById(R.id.restaurantDetailButtonEdit);
        backButton = findViewById(R.id.restaurantDetailButtonBack);
    }

    private void initializeMemory() {
        realm = Realm.getDefaultInstance();

        Intent i = getIntent();
        user = realm.where(User.class)
                .equalTo("uuid", i.getStringExtra("userUuidExtra"))
                .findFirst();

        restaurant = realm.where(Restaurant.class)
                .equalTo("restaurantUuid", i.getStringExtra("restaurantUuidExtra"))
                .findFirst();
    }

    private void initializeListeners() {
        foodButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                Intent i = new Intent(RestaurantDetailActivity.this, com.example.foodjournal.review.FoodReviewListActivity.class);
                i.putExtra("userUuidExtra", user.getUuid());
                i.putExtra("restaurantUuidExtra", restaurant.getRestaurantUuid());
                startActivity(i);
            }
        });

        editButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                Intent i = new Intent(RestaurantDetailActivity.this, EditRestaurantActivity.class);
                i.putExtra("userUuidExtra", user.getUuid());
                i.putExtra("restaurantUuidExtra", restaurant.getRestaurantUuid());
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

        restaurantName.setText(restaurant.getName());
        restaurantAddress.setText(String.format("Located in %s", restaurant.getAddress()));
        restaurantDescription.setText(restaurant.getDescription());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        lastVisited.setText(String.format("Last visited at %s", sdf.format(restaurant.getLastVisited())));

        File resImage = new File(getImageDir, restaurant.getRestaurantUuid() + ".jpeg");
        if (resImage.exists()) {
            Picasso.get()
                    .load(resImage)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(restaurantPicture);
        } else {
            restaurantPicture.setImageResource(R.mipmap.ic_launcher);
        }

        // Average rating calculation
        Number avg = realm.where(com.example.foodjournal.review.FoodReview.class)
                .equalTo("restaurantUuid", restaurant.getRestaurantUuid())
                .average("rating");

        if (avg == null || avg.floatValue() == 0) {
            starRating.setRating(0);
            averageRating.setText("No ratings yet");
        } else {
            float rating = avg.floatValue();
            starRating.setRating(rating);
            averageRating.setText(String.format(Locale.getDefault(), "%.1f stars", rating));
        }
    }
}