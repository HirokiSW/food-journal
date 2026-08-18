package com.example.foodjournal.authorization;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.foodjournal.home.HomeActivity;
import com.example.foodjournal.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    // LIFE CYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        checkPermissions();
    }

    public void onDestroy() {
        super.onDestroy();
        if (!realm.isClosed()) {
            realm.close();
        }
    }


    // MANAGE PERMISSIONS
    public void checkPermissions()
    {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                )
                .withListener(new BaseMultiplePermissionsListener()
                {
                    public void onPermissionsChecked(MultiplePermissionsReport report)
                    {
                        if (report.areAllPermissionsGranted()) {
                            init();
                        } else {
                            toastRequirePermissions();
                        }
                    }
                })
                .check();

    }

    public void toastRequirePermissions()
    {
        Toast.makeText(this, "You must provide permissions for app to run", Toast.LENGTH_LONG).show();
        finish();
    }


    // INITIALIZATION
    EditText usernameInput, passwordInput;
    Button signinButton, adminButton, clearButton;
    CheckBox rememberFlag;
    SharedPreferences prefs;
    SharedPreferences.Editor edit;
    Realm realm;

    private void init() {
        initializeViews();
        initializeMemory();
        initializeListeners();
        finalizeViews();
    }

    private void initializeViews() {
        usernameInput = findViewById(R.id.mainEditTextUsername);
        passwordInput = findViewById(R.id.mainEditTextPassword);
        signinButton = findViewById(R.id.mainButtonSignin);
        adminButton = findViewById(R.id.mainButtonAdmin);
        clearButton = findViewById(R.id.mainButtonClear);
        rememberFlag = findViewById(R.id.mainCheckBoxRemember);
    }

    private void initializeMemory() {
        prefs = getSharedPreferences("userPrefs", 0);
        edit = prefs.edit();

        realm = Realm.getDefaultInstance();
    }

    private void initializeListeners() {
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processSignIn();
            }
        });
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAdminActivity();
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearUserPreferences();
            }
        });
    }

    private void finalizeViews() {
        if (prefs.getBoolean("rememberMe", false)) {
            User pastUser = realm.where(User.class)
                    .equalTo("uuid", prefs.getString("uuidRemembered", ""))
                    .findFirst();
            if (pastUser != null) {
                usernameInput.setText(pastUser.getName());
                passwordInput.setText(pastUser.getPassword());
                rememberFlag.setChecked(true);
            }
        }
    }


    // BUTTON OPERATIONS
    private void processSignIn() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        User user = realm.where(User.class)
                .equalTo("name", username)
                .findFirst();

        if (user == null) {
            Toast.makeText(this, "Username does not exist", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(user.getPassword())) {
            Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
        } else {
            edit.putString("uuidRemembered", user.getUuid());
            edit.putBoolean("rememberMe", rememberFlag.isChecked());
            edit.apply();
            Intent w = new Intent(this, HomeActivity.class);
            w.putExtra("userUuidExtra", user.getUuid());
            startActivity(w);
        }
    }

    private void startAdminActivity() {
        Intent a = new Intent(this, AdminActivity.class);
        startActivity(a);
    }

    private void clearUserPreferences() {
        Toast.makeText(this, "Preferences cleared", Toast.LENGTH_SHORT).show();
        edit.clear();
        edit.apply();
    }
}