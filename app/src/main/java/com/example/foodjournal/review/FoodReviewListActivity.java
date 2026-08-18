package com.example.foodjournal.review;

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
import com.example.foodjournal.restaurant.RestaurantAdapter;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmResults;

public class FoodReviewListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_food_review_list);
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
    TextView username;
    RecyclerView reviewList;
    Button addButton, backButton;
    User user;
    Restaurant restaurant;

    private void init() {
        initializeViews();
        initializeMemory();
        initializeListeners();
        finalizeViews();
    }

    private void initializeViews() {
        profilePicture = findViewById(R.id.foodListImageViewProfile);
        username = findViewById(R.id.foodListTextViewUsername);
        reviewList = findViewById(R.id.foodListRecyclerViewList);
        addButton = findViewById(R.id.foodListButtonAdd);
        backButton = findViewById(R.id.foodListButtonBack);
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

        RealmResults<FoodReview> foodReviews = realm.where(FoodReview.class)
                .equalTo("userUuid", user.getUuid())
                .and()
                .equalTo("restaurantUuid", restaurant.getRestaurantUuid())
                .findAll();
        FoodReviewAdapter foodReviewAdapter = new FoodReviewAdapter(this, foodReviews);
        reviewList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        reviewList.setAdapter(foodReviewAdapter);
    }

    private void initializeListeners() {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FoodReviewListActivity.this, AddFoodReviewActivity.class);
                i.putExtra("userUuidExtra", user.getUuid());
                i.putExtra("restaurantUuidExtra", restaurant.getRestaurantUuid());
                startActivity(i);
            }
        });

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
    }
}