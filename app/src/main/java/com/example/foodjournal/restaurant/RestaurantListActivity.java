package com.example.foodjournal.restaurant;

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
import com.example.foodjournal.home.RecentRestaurantsAdapter;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class RestaurantListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_list);
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
    RecyclerView restaurantList;
    Button addButton, backButton;
    RestaurantAdapter restaurantAdapter;
    User user;

    private void init() {
        initializeViews();
        initializeMemory();
        initializeListeners();
        finalizeViews();
    }

    private void initializeViews() {
        profilePicture = findViewById(R.id.restaurantListImageViewProfile);
        username = findViewById(R.id.restaurantListTextViewUsername);
        restaurantList = findViewById(R.id.restaurantListRecyclerViewList);
        addButton = findViewById(R.id.restaurantListButtonAdd);
        backButton = findViewById(R.id.restaurantListButtonBack);
    }

    private void initializeMemory() {
        realm = Realm.getDefaultInstance();

        Intent i = getIntent();
        user = realm.where(User.class)
                .equalTo("uuid", i.getStringExtra("userUuidExtra"))
                .findFirst();

        RealmResults<Restaurant> restaurants = realm.where(Restaurant.class)
                .equalTo("userUuid", user.getUuid())
                .findAll();
        restaurantAdapter = new RestaurantAdapter(this, restaurants);
        restaurantList.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        restaurantList.setAdapter(restaurantAdapter);
    }

    private void initializeListeners() {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RestaurantListActivity.this, AddRestaurantActivity.class);
                i.putExtra("userUuidExtra", user.getUuid());
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