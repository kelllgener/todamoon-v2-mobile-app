package com.toda.todamoon_v2.driver.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.utils.FirebaseUtil;

import java.util.Locale;

public class DriverProfile extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser user;
    private static final String TAG = "DriverProfile";
    private static final int PICK_PROFILE_IMAGE_REQUEST = 1;
    private static final int PICK_PLATE_IMAGE_REQUEST = 2;
    private ImageButton btnChangeProfileProfile;
    private TextView nameTextView, emailTextView, roleTextView, statusTextView, phoneTextView, tricycleNumberTextView, barangayTextView;
    private ImageView profileImageView, plateNumberView;
    private ProgressBar loadingIndicator, loadingPlate;

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
        setContentView(R.layout.activity_driver_profile);

        // Initialize Firebase components
        FirebaseAuth mAuth = FirebaseUtil.getFirebaseAuthInstance();
        db = FirebaseUtil.getFirebaseFirestoreInstance();
        storage = FirebaseUtil.getFirebaseStorageInstance();
        user = mAuth.getCurrentUser();

        // Initialize UI components
        loadingIndicator = findViewById(R.id.loadingIndicator); // Loading indicator initialization
        loadingPlate = findViewById(R.id.loadingIndicatorPlateNumber);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnChangeProfileProfile = findViewById(R.id.btn_change_profile_image_driver);
        ImageButton btnChangePlateNumber = findViewById(R.id.change_plate_number);
        ImageButton btnGoToEditProfile = findViewById(R.id.btn_go_to_edit_profile_driver);
        profileImageView = findViewById(R.id.avatar);
        nameTextView = findViewById(R.id.name);
        emailTextView = findViewById(R.id.email);
        roleTextView = findViewById(R.id.role);
        statusTextView = findViewById(R.id.status);
        phoneTextView = findViewById(R.id.phone);
        tricycleNumberTextView = findViewById(R.id.tricycle_number);
        barangayTextView = findViewById(R.id.barangay);
        plateNumberView = findViewById(R.id.plate_number);

        btnBack.setOnClickListener(v -> finish());
        btnChangeProfileProfile.setOnClickListener(v -> openImagePicker(PICK_PROFILE_IMAGE_REQUEST));
        btnChangePlateNumber.setOnClickListener(v -> openImagePicker(PICK_PLATE_IMAGE_REQUEST));

        btnGoToEditProfile.setOnClickListener(v -> startActivity(new Intent(DriverProfile.this, EditProfileDetails.class)));

        // Load user data with real-time updates
        loadUserData();
    }

    private void loadUserData() {

        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);
            userRef.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    Log.e(TAG, "Error fetching user data: ", e);
                    return;
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    String email = documentSnapshot.getString("email");
                    String role = documentSnapshot.getString("role");
                    Boolean status = documentSnapshot.getBoolean("status");
                    String statusText = (status != null && status) ? "In Queue" : "Not In Queue";
                    String phoneNumber = documentSnapshot.getString("phoneNumber");
                    String tricycleNumber = documentSnapshot.getString("tricycleNumber");
                    String barangay = documentSnapshot.getString("barangay");
                    String profileImagePath = "profile_images/";
                    String plateNumberPath = "plate_images/";

                    // Update UI with user details
                    nameTextView.setText(name);
                    emailTextView.setText(email);
                    roleTextView.setText(role);
                    statusTextView.setText(statusText);
                    phoneTextView.setText(phoneNumber);
                    tricycleNumberTextView.setText(tricycleNumber);
                    barangayTextView.setText(barangay);



                    // Load images
                    loadImage(profileImagePath, userId, profileImageView, R.drawable.profile_user);
                    loadImage(plateNumberPath, userId, plateNumberView, R.drawable.plate_number);

                }
            });
        }
    }

    private void loadImage(String imagePath, String uid, ImageView imageView, int fallbackResId) {
        loadingIndicator.setVisibility(View.VISIBLE);
        loadingPlate.setVisibility(View.VISIBLE);
        profileImageView.setVisibility(View.GONE);
        btnChangeProfileProfile.setVisibility(View.GONE);
        plateNumberView.setVisibility(View.GONE);
        if (uid != null && !uid.isEmpty()) {
            StorageReference imageRef = storage.getReference().child(imagePath + uid + ".png");
            imageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        Glide.with(DriverProfile.this).load(uri).into(imageView);

                        profileImageView.setVisibility(View.VISIBLE);
                        plateNumberView.setVisibility(View.VISIBLE);
                        btnChangeProfileProfile.setVisibility(View.VISIBLE);
                        loadingIndicator.setVisibility(View.GONE);
                        loadingPlate.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load image: ", e);
                        imageView.setImageResource(fallbackResId);
                        loadingIndicator.setVisibility(View.GONE);
                        loadingPlate.setVisibility(View.GONE);
                    });
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
                } else if (requestCode == PICK_PLATE_IMAGE_REQUEST) {
                    uploadImage(imageUri, "plate_images", "plateNumber");
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
