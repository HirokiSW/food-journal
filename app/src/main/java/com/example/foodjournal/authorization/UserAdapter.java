package com.example.foodjournal.authorization;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import com.example.foodjournal.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class UserAdapter extends RealmRecyclerViewAdapter<User, UserAdapter.ViewHolder> {

    // INITIALIZATION
    AdminActivity activity;

    public UserAdapter(AdminActivity activity, OrderedRealmCollection<User> data) {
        super(data, true);
        this.activity = activity;
    }


    // VIEW HOLDER INITIALIZATION
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView profilePicture;
        TextView username;
        TextView password;
        ImageButton editButton;
        ImageButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
        }

        private void initializeViews() {
            profilePicture = itemView.findViewById(R.id.usersImageViewProfile);
            username = itemView.findViewById(R.id.usersTextViewUsername);
            password = itemView.findViewById(R.id.usersTextViewPassword);
            editButton = itemView.findViewById(R.id.usersImageButtonEdit);
            deleteButton = itemView.findViewById(R.id.usersImageButtonDelete);
        }

        private void initializeListeners(User user) {
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startEditActivity(user);
                }
            });
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteConfirmation(user);
                }
            });
        }

        private void finalizeViews(User user) {
            username.setText(user.getName());
            password.setText(user.getPassword());

            File getImageDir = activity.getExternalCacheDir();
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
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = activity.getLayoutInflater().inflate(R.layout.layout_users, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = getItem(position);
        if (user != null && user.isValid()) {
            holder.initializeViews();
            holder.initializeListeners(user);
            holder.finalizeViews(user);
        }
    }


    // BUTTON OPERATIONS
    private void startEditActivity(User user) {
        Intent e = new Intent(activity, EditActivity.class);
        e.putExtra("userUuidExtra", user.getUuid());
        activity.startActivity(e);
    }

    private void deleteConfirmation(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete this account?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUser(user);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteUser(User user) {
        activity.realm.beginTransaction();
        if (user != null) {
            user.deleteFromRealm();
        }
        activity.realm.commitTransaction();
        Toast.makeText(activity, "Account deleted", Toast.LENGTH_SHORT).show();
    }
}

