package com.example.foodjournal.authorization;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodjournal.R;

import io.realm.Realm;
import io.realm.RealmResults;

public class AdminActivity extends AppCompatActivity {

    // LIFE CYCLE
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
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
    RecyclerView userList;
    Button addButton, clearButton, backButton;
    Realm realm;

    private void init() {
        initializeViews();
        initializeMemory();
        initializeListeners();
    }

    private void initializeViews() {
        addButton = findViewById(R.id.adminButtonAdd);
        clearButton = findViewById(R.id.adminButtonClear);
        backButton = findViewById(R.id.adminButtonBack);
        userList = findViewById(R.id.adminRecyclerViewUsers);
    }

    private void initializeMemory() {
        realm = Realm.getDefaultInstance();

        RealmResults<User> users = realm.where(User.class).findAll();
        UserAdapter adapter = new UserAdapter(this, users);
        userList.setAdapter(adapter);
    }

    private void initializeListeners() {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegisterActivity();
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearConfirmation();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    // BUTTON OPERATIONS
    private void startRegisterActivity() {
        Intent r = new Intent(this, RegisterActivity.class);
        startActivity(r);
    }

    private void clearConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Clear All Users");
        builder.setMessage("Are you sure you want to clear all user data?");

        builder.setPositiveButton("Clear", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearAllUserData();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void clearAllUserData() {
        RealmResults<User> users = realm.where(User.class).findAll();
        realm.beginTransaction();
        users.deleteAllFromRealm();
        realm.commitTransaction();
    }
}