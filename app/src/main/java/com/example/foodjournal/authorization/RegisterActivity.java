package com.example.foodjournal.authorization;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodjournal.ImageActivity;
import com.example.foodjournal.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.realm.Realm;

public class RegisterActivity extends AppCompatActivity {

    // LIFE CYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
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
    ImageView profilePicture;
    EditText usernameInput, passwordInput, confirmInput;
    Button saveButton, cancelButton;
    Realm realm;

    private void init() {
        initializeViews();
        initializeMemory();
        initializeListeners();
        finalizeViews();
    }

    private void initializeViews() {
        profilePicture = findViewById(R.id.registerImageViewProfile);
        usernameInput = findViewById(R.id.registerEditTextUsername);
        passwordInput = findViewById(R.id.registerEditTextPassword);
        confirmInput = findViewById(R.id.registerEditTextConfirm);
        saveButton = findViewById(R.id.registerButtonSave);
        cancelButton = findViewById(R.id.registerButtonCancel);
    }

    private void initializeMemory() {
        realm = Realm.getDefaultInstance();
    }

    private void finalizeViews() {
        profilePicture.setImageResource(R.mipmap.ic_launcher);
    }

    private void initializeListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processRegistration();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });
    }


    // BUTTON OPERATIONS
    private void processRegistration() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirm = confirmInput.getText().toString().trim();
        User redundantUser = realm.where(User.class)
                .equalTo("name", username)
                .findFirst();

        if (redundantUser != null) {
            Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
        } else if (username.isEmpty()) {
            Toast.makeText(this, "Name must not be blank", Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Password must not be blank", Toast.LENGTH_SHORT).show();
        } else if (!confirm.equals(password)) {
            Toast.makeText(this, "Confirm password does not match", Toast.LENGTH_SHORT).show();
        } else {
            addUser(username, password);
            finish();
        }
    }


    // HELPER METHODS
    private void addUser(String username, String password) {
        User newUser = new User();
        newUser.setName(username);
        newUser.setPassword(password);

        try {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(newUser);
            realm.commitTransaction();
            if (savedRawJpeg != null) {
                saveFile(savedRawJpeg, newUser.getUuid() + ".jpeg");
            }
            long numUsers = realm.where(User.class).count();
            Toast.makeText(this, "New User saved. Total: " + numUsers, Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            Toast.makeText(this, "Error saving", Toast.LENGTH_SHORT).show();
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
                                .into(profilePicture);
                    } else {
                        profilePicture.setImageResource(R.mipmap.ic_launcher);
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