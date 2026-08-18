package com.example.foodjournal.authorization;

import android.content.Intent;
import android.os.Bundle;
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

public class EditActivity extends AppCompatActivity {

    // LIFE CYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit);
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
    User user;

    private void init() {
        initializeViews();
        initializeMemory();
        initializeListeners();
        finalizeViews();
    }

    private void initializeViews() {
        profilePicture = findViewById(R.id.editImageViewProfile);
        usernameInput = findViewById(R.id.editEditTextUsername);
        passwordInput = findViewById(R.id.editEditTextPassword);
        confirmInput = findViewById(R.id.editEditTextConfirm);
        saveButton = findViewById(R.id.editButtonSave);
        cancelButton = findViewById(R.id.editButtonCancel);
    }

    private void initializeMemory() {
        realm = Realm.getDefaultInstance();

        Intent i = getIntent();
        user = realm.where(User.class)
                .equalTo("uuid", i.getStringExtra("userUuidExtra"))
                .findFirst();
    }

    private void initializeListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processUserEdit();
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

    private void finalizeViews() {
        usernameInput.setText(user.getName());
        passwordInput.setText(user.getPassword());
        File getImageDir = getExternalCacheDir();
        File file = new File(getImageDir, user.getUuid() + ".jpeg");

        if (file.exists()) {
            Picasso.get()
                    .load(file)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(profilePicture);
        } else {
            profilePicture.setImageResource(R.mipmap.ic_launcher);
        }
    }


    // BUTTON OPERATIONS
    private void processUserEdit() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirm = confirmInput.getText().toString().trim();
        User redundantUser = realm.where(User.class)
                .notEqualTo("uuid", user.getUuid())
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
            editUser(username, password);
            finish();
        }
    }


    // HELPER METHODS
    private void editUser(String username, String password) {
        try {
            realm.beginTransaction();
            user.setName(username);
            user.setPassword(password);
            if (savedRawJpeg != null) {
                saveFile(savedRawJpeg, user.getUuid() + ".jpeg");
            }
            realm.commitTransaction();
            Toast.makeText(this, "Edited Successfully", Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            Toast.makeText(this, "Error editing", Toast.LENGTH_SHORT).show();
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
                    File file = saveFile(savedRawJpeg,"temp.jpeg");
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