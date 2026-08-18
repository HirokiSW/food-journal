package com.example.foodjournal.review;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodjournal.ImageActivity;
import com.example.foodjournal.R;
import com.example.foodjournal.authorization.User;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

public class AddFoodReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_food_review);
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
    ImageView profilePicture, foodPicture;
    TextView username;
    EditText foodName, foodPrice, lastEaten, foodDescription;
    RatingBar starRating;
    Button saveButton, cancelButton;
    User user;
    String restaurantUuid;

    private void init() {
        initializeViews();
        initializeMemory();
        initializeListeners();
        finalizeViews();
    }

    private void initializeViews() {
        profilePicture = findViewById(R.id.addFoodImageViewProfile);
        username = findViewById(R.id.addFoodTextViewUsername);
        foodPicture = findViewById(R.id.addFoodImageViewPicture);
        foodName = findViewById(R.id.addFoodEditTextName);
        foodPrice = findViewById(R.id.addFoodEditTextPrice);
        lastEaten = findViewById(R.id.addFoodEditTextDate);
        foodDescription = findViewById(R.id.addFoodEditTextDescription);
        starRating = findViewById(R.id.addFoodRatingBarStars);
        saveButton = findViewById(R.id.addFoodButtonSave);
        cancelButton = findViewById(R.id.addFoodButtonCancel);
    }

    private void initializeMemory() {
        realm = Realm.getDefaultInstance();

        Intent i = getIntent();
        user = realm.where(User.class)
                .equalTo("uuid", i.getStringExtra("userUuidExtra"))
                .findFirst();
        restaurantUuid = i.getStringExtra("restaurantUuidExtra");
    }

    private void initializeListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processSave();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        foodPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
    }

    private void processSave() {
        String name = foodName.getText().toString().trim();
        String priceStr = foodPrice.getText().toString().trim();
        String dateStr = lastEaten.getText().toString().trim();
        String description = foodDescription.getText().toString().trim();
        float rating = starRating.getRating();

        if (name.isEmpty() || priceStr.isEmpty() || dateStr.isEmpty()) {
            android.widget.Toast.makeText(this, "Please fill in required fields", android.widget.Toast.LENGTH_SHORT).show();
            return;
        } else if (Math.abs(rating) < 0.00001f) {
            android.widget.Toast.makeText(this, "Rating cannot be 0", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "Invalid price", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date date;
        try {
            date = sdf.parse(dateStr);
        } catch (Exception e) {
            android.widget.Toast.makeText(this, "Invalid date format (dd/MM/yyyy)", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        FoodReview review = new FoodReview();
        review.setName(name);
        review.setPrice(price);
        review.setLastEaten(date);
        review.setDescription(description);
        review.setRating(rating);
        review.setUserUuid(user.getUuid());
        review.setRestaurantUuid(restaurantUuid);

        try {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(review);
            if (savedRawJpeg != null) {
                saveFile(savedRawJpeg, review.getReviewUuid() + ".jpeg");
            }
            realm.commitTransaction();
            android.widget.Toast.makeText(this, "Food review saved", android.widget.Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            realm.cancelTransaction();
            android.widget.Toast.makeText(this, "Error saving review", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private void finalizeViews() {
        username.setText(user.getName());
        foodPicture.setImageResource(R.mipmap.ic_launcher);

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

    // IMAGE CAPTURE
    public static int REQUEST_CODE_IMAGE_SCREEN = 0;
    byte[] savedRawJpeg;

    public void takePhoto() {
        Intent i = new Intent(this, ImageActivity.class);
        startActivityForResult(i, REQUEST_CODE_IMAGE_SCREEN);
    }

    public void onActivityResult(int requestCode, int responseCode, Intent data) {
        super.onActivityResult(requestCode, responseCode, data);

        if (requestCode == REQUEST_CODE_IMAGE_SCREEN) {
            if (responseCode == ImageActivity.RESULT_CODE_IMAGE_TAKEN) {
                savedRawJpeg = data.getByteArrayExtra("rawJpeg");

                try {
                    File file = saveFile(savedRawJpeg, "temp.jpeg");
                    if (file.exists()) {
                        Picasso.get()
                                .load(file)
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .into(foodPicture);
                    } else {
                        foodPicture.setImageResource(R.mipmap.ic_launcher);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private File saveFile(byte[] jpeg, String filename) throws IOException {
        File getImageDir = getExternalCacheDir();
        File file = new File(getImageDir, filename);

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(jpeg);
        fos.close();
        return file;
    }
}