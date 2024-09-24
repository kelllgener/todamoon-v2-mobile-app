package com.toda.todamoon_v2.passenger.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.utils.FirebaseUtil;

import java.util.Locale;

public class PassengerProfile extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser user;
    private static final int PICK_PROFILE_IMAGE_REQUEST = 1;
    private TextView nameTextView, emailTextView, roleTextView;
    private ImageView profileImageView;
    private static final String TAG = "PassengerProfile";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(updateBaseContextLocale(newBase));
    }

    private Context updateBaseContextLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("settings", MODE_PRIVATE);
        String languageCode = prefs.getString("selected_language", "en"); // Default to English

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_profile);

        // Initialize Firebase components
        FirebaseAuth mAuth = FirebaseUtil.getFirebaseAuthInstance();
        db = FirebaseUtil.getFirebaseFirestoreInstance();
        storage = FirebaseUtil.getFirebaseStorageInstance();
        user = mAuth.getCurrentUser();

        // Initialize UI components
        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageButton btnChangeProfileProfile = findViewById(R.id.btn_change_profile_image_passenger);
        profileImageView = findViewById(R.id.avatar);
        nameTextView = findViewById(R.id.name);
        emailTextView = findViewById(R.id.email);
        roleTextView = findViewById(R.id.role);

        btnBack.setOnClickListener(v -> finish());
        btnChangeProfileProfile.setOnClickListener(v -> openImagePicker(PICK_PROFILE_IMAGE_REQUEST));

        // Load user data with real-time updates
        loadUserData();
    }

    private void loadUserData() {
        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    String email = documentSnapshot.getString("email");
                    String role = documentSnapshot.getString("role");
                    String profileImagePath = "profile_images/";
                    String profileImageUrl = documentSnapshot.getString("profileImage");

                    // Update UI with user details
                    nameTextView.setText(name);
                    emailTextView.setText(email);
                    roleTextView.setText(role);
                    for (UserInfo profile : user.getProviderData()) {
                        String providerId = profile.getProviderId();

                        if (providerId.equals("password")) {
                            loadImage(profileImagePath, userId, profileImageView, R.drawable.profile_user);
                        } else if (providerId.equals("google.com")) {
                            Glide.with(PassengerProfile.this).load(profileImageUrl).into(profileImageView);
                        }
                    }
                }
            });
        }
    }

    private void loadImage(String imagePath, String uid, ImageView imageView, int fallbackResId) {
        if (uid != null && !uid.isEmpty()) {
            StorageReference imageRef = storage.getReference().child(imagePath + uid + ".png");
            imageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> Glide.with(PassengerProfile.this).load(uri).into(imageView))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to load image: ", e));
        } else {
            imageView.setImageResource(fallbackResId);
        }
    }

    private void openImagePicker(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                if (requestCode == PICK_PROFILE_IMAGE_REQUEST) {
                    uploadImage(imageUri, "profile_images", "profileImage");
                }
            }
        }
    }

    private void uploadImage(Uri imageUri, String folder, String field) {
        if (user != null) {
            String userId = user.getUid();
            StorageReference imageRef = storage.getReference().child(folder + "/" + userId + ".png");
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> updateImageInFirestore(uri.toString(), field))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to get download URL: ", e)))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to upload image: ", e));
        }
    }

    private void updateImageInFirestore(String imageUrl, String field) {
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId)
                    .update(field, imageUrl)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, field + " URL updated in Firestore"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update " + field + " URL: ", e));
        }
    }
}