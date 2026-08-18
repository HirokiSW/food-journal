package com.example.foodjournal.restaurant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class EditRestaurantActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_restaurant);
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
    ImageView profilePicture, restaurantPicture;
    TextView username;
    EditText restaurantName, restaurantAddress, lastVisited, restaurantDescription;
    Button saveButton, cancelButton;
    User user;
    Restaurant restaurant;

    private void init() {
        initializeViews();
        initializeMemory();
        initializeListeners();
        finalizeViews();
    }

    private void initializeViews() {
        profilePicture = findViewById(R.id.editRestaurantImageViewProfile);
        username = findViewById(R.id.editRestaurantTextViewUsername);
        restaurantPicture = findViewById(R.id.editRestaurantImageViewPicture);
        restaurantName = findViewById(R.id.editRestaurantEditTextName);
        restaurantAddress = findViewById(R.id.editRestaurantEditTextAddress);
        lastVisited = findViewById(R.id.editRestaurantEditTextDate);
        restaurantDescription = findViewById(R.id.editRestaurantEditTextDescription);
        saveButton = findViewById(R.id.editRestaurantButtonSave);
        cancelButton = findViewById(R.id.editRestaurantButtonCancel);
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
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processUpdate();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        restaurantPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
    }

    private void processUpdate() {
        String name = restaurantName.getText().toString().trim();
        String address = restaurantAddress.getText().toString().trim();
        String dateStr = lastVisited.getText().toString().trim();
        String description = restaurantDescription.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || dateStr.isEmpty()) {
            android.widget.Toast.makeText(this, "Please fill in required fields", android.widget.Toast.LENGTH_SHORT).show();
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

        try {
            realm.beginTransaction();
            restaurant.setName(name);
            restaurant.setAddress(address);
            restaurant.setLastVisited(date);
            restaurant.setDescription(description);
            if (savedRawJpeg != null) {
                saveFile(savedRawJpeg, restaurant.getRestaurantUuid() + ".jpeg");
            }
            realm.commitTransaction();
            android.widget.Toast.makeText(this, "Restaurant updated", android.widget.Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            realm.cancelTransaction();
            android.widget.Toast.makeText(this, "Error updating restaurant", android.widget.Toast.LENGTH_SHORT).show();
        }
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

        // Pre-fill fields
        restaurantName.setText(restaurant.getName());
        restaurantAddress.setText(restaurant.getAddress());
        restaurantDescription.setText(restaurant.getDescription());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        lastVisited.setText(sdf.format(restaurant.getLastVisited()));

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
                                .into(restaurantPicture);
                    } else {
                        restaurantPicture.setImageResource(R.mipmap.ic_launcher);
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