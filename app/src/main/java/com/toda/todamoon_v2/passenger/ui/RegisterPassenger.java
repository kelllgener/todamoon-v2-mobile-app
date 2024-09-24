package com.toda.todamoon_v2.passenger.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.utils.FirebaseUtil;
import com.toda.todamoon_v2.utils.LoadingDialogUtil;
import com.google.android.material.textfield.TextInputEditText;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RegisterPassenger extends AppCompatActivity {

    private TextInputEditText firstname, lastname, email, password, confirmPassword;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private LoadingDialogUtil loadingDialogUtil;
    private TextView go_to_login;
    private Uri imageUri;

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
        setContentView(R.layout.activity_register_passenger);

        initializeFirebase();
        initializeView();
        setButtonListeners();
    }

    private void initializeFirebase() {
        mAuth = FirebaseUtil.getFirebaseAuthInstance();
        db = FirebaseUtil.getFirebaseFirestoreInstance();
    }

    private void initializeView() {
        firstname = findViewById(R.id.txt_firstname_passenger);
        lastname = findViewById(R.id.txt_lastname_passenger);
        email = findViewById(R.id.txt_email_passenger);
        password = findViewById(R.id.txt_password_passenger);
        confirmPassword = findViewById(R.id.txt_confirm_password_passenger);
        registerButton = findViewById(R.id.btn_register_passenger);
        go_to_login = findViewById(R.id.txt_go_to_login);
        loadingDialogUtil = new LoadingDialogUtil(this);  // Loading dialog for registration process
    }

    private void setButtonListeners() {
        registerButton.setOnClickListener(v -> registerUser());
        go_to_login.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterPassenger.this, LoginPassenger.class);
            startActivity(intent);
        });
    }

    private void registerUser() {
        // Get user inputs
        String firstName = firstname.getText().toString().trim();
        String lastName = lastname.getText().toString().trim();
        String emailInput = email.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();
        String confirmPasswordInput = confirmPassword.getText().toString().trim();

        // Validate inputs
        if (!validateInputs(firstName, lastName, emailInput, passwordInput, confirmPasswordInput)) {
            return;
        }

        loadingDialogUtil.showLoadingDialog();  // Show loading dialog

        // Register user using Firebase Authentication
        mAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            uploadProfileImage(user, firstName + " " + lastName);
                        }
                    } else {
                        showErrorMessage("Registration failed: " + task.getException().getMessage());
                        loadingDialogUtil.hideLoadingDialog();
                    }
                });
    }

    private boolean validateInputs(String firstName, String lastName, String emailInput, String passwordInput, String confirmPasswordInput) {
        if (firstName.isEmpty() || lastName.isEmpty() || emailInput.isEmpty() || passwordInput.isEmpty() || confirmPasswordInput.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!passwordInput.equals(confirmPasswordInput)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void uploadProfileImage(FirebaseUser user, String fullName) {
        if (imageUri != null) {
            uploadImageToFirebase(user, fullName, imageUri);
        } else {
            // If no image is selected, use a default image
            InputStream defaultImageStream = getImageInputStreamFromDrawable(R.drawable.profile_user);
            uploadDefaultImage(user, fullName, defaultImageStream);
        }
    }

    private void uploadImageToFirebase(FirebaseUser user, String fullName, Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/" + user.getUid() + ".png");

        profileImageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveUserToDatabase(user, fullName, uri.toString()))
                        .addOnFailureListener(e -> handleImageUploadFailure(e))
                ).addOnFailureListener(e -> handleImageUploadFailure(e));
    }

    private void uploadDefaultImage(FirebaseUser user, String fullName, InputStream imageStream) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/" + user.getUid() + ".png");

        profileImageRef.putStream(imageStream)
                .addOnSuccessListener(taskSnapshot -> profileImageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveUserToDatabase(user, fullName, uri.toString()))
                        .addOnFailureListener(e -> handleImageUploadFailure(e))
                ).addOnFailureListener(e -> handleImageUploadFailure(e));
    }

    private void saveUserToDatabase(FirebaseUser user, String fullName, String profileImage) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("name", fullName);
        userData.put("email", user.getEmail());
        userData.put("role", "Passenger");
        userData.put("profileImage", profileImage);

        db.collection("users").document(user.getUid()).set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterPassenger.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    loadingDialogUtil.hideLoadingDialog();
                    redirectToLogin();
                })
                .addOnFailureListener(e -> {
                    showErrorMessage("Failed to save user data: " + e.getMessage());
                    loadingDialogUtil.hideLoadingDialog();
                });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(RegisterPassenger.this, LoginPassenger.class);
        startActivity(intent);
        finish();
    }

    private void handleImageUploadFailure(Exception e) {
        Log.e("RegisterUser", "Error uploading image", e);
        showErrorMessage("Error uploading profile image");
        loadingDialogUtil.hideLoadingDialog();
    }

    private void showErrorMessage(String message) {
        Toast.makeText(RegisterPassenger.this, message, Toast.LENGTH_SHORT).show();
    }

    // Utility method to fetch an image as InputStream from drawable
    public InputStream getImageInputStreamFromDrawable(int drawableId) {
        return getResources().openRawResource(drawableId);
    }
}
