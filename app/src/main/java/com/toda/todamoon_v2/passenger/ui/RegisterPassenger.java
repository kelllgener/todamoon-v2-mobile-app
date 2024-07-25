package com.toda.todamoon_v2.passenger.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.toda.todamoon_v2.FrontPage;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.utils.AndroidUtil;
import com.toda.todamoon_v2.utils.FirebaseUtil;

import com.google.android.material.textfield.TextInputEditText;
import com.toda.todamoon_v2.utils.LoadingDialogUtil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class RegisterPassenger extends AppCompatActivity {

    private TextInputEditText firstname, lastname, email, password, confirmPassword;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private AndroidUtil androidUtil;
    private String hashedPassword;
    private LoadingDialogUtil loadingDialogUtil;
    private TextView go_to_login;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_passenger);

        initializeView();
        mAuth = FirebaseUtil.getFirebaseAuthInstance();
        androidUtil = new AndroidUtil();

        registerButton.setOnClickListener(v -> registerUser());

        go_to_login.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterPassenger.this, LoginPassenger.class);
            startActivity(intent);
        });
    }

    private void initializeView() {
        firstname = findViewById(R.id.editFirstNamePassenger);
        lastname = findViewById(R.id.editLastNamePassenger);
        email = findViewById(R.id.editEmailPassenger);
        password = findViewById(R.id.editPasswordPassenger);
        confirmPassword = findViewById(R.id.editConfirmPassPassenger);
        registerButton = findViewById(R.id.btnRegister);  // Assuming you have a register button in your XML
        // Loading dialog
        loadingDialogUtil = new LoadingDialogUtil(this);
        go_to_login = findViewById(R.id.txtLogin);
    }

    private void registerUser() {
        String firstName = firstname.getText().toString().trim();
        String lastName = lastname.getText().toString().trim();
        String name = firstName + " " + lastName;
        String emailInput = email.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();
        String confirmPasswordInput = confirmPassword.getText().toString().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || emailInput.isEmpty() || passwordInput.isEmpty() || confirmPasswordInput.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!passwordInput.equals(confirmPasswordInput)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        hashedPassword = androidUtil.hashPassword(passwordInput);
        if (hashedPassword == null) {
            Toast.makeText(this, "Failed to hash password", Toast.LENGTH_SHORT).show();
            return;
        }
        loadingDialogUtil.showLoadingDialog();
        mAuth.createUserWithEmailAndPassword(emailInput, hashedPassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            uploadProfileImage(user, name, hashedPassword);
                        }
                    } else {
                        Toast.makeText(RegisterPassenger.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialogUtil.hideLoadingDialog();
                    }
                });
    }

    private void uploadProfileImage(FirebaseUser user, String name, String hashedPassword) {
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference profileImageRef = storageRef.child("profile_images/" + user.getUid() + ".png");

            profileImageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String profileImageUrl = uri.toString();
                            saveUserToDatabase(user, name, hashedPassword, profileImageUrl);
                        }).addOnFailureListener(e -> {
                            Log.e("RegisterUser", "Error getting profile image download URL", e);
                            loadingDialogUtil.hideLoadingDialog();
                        });
                    }).addOnFailureListener(e -> {
                        Log.e("RegisterUser", "Error uploading profile image", e);
                        loadingDialogUtil.hideLoadingDialog();
                    });
        } else {
            // If no image is selected, proceed with a default image
            InputStream profileImageStream = getImageInputStreamFromDrawable(R.drawable.profile_user);
            uploadDefaultProfileImage(profileImageStream, user, name, hashedPassword);
        }
    }

    private void uploadDefaultProfileImage(InputStream imageStream, FirebaseUser user, String name, String hashedPassword) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/" + user.getUid() + ".png");

        profileImageRef.putStream(imageStream)
                .addOnSuccessListener(taskSnapshot -> {
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String profileImageUrl = uri.toString();
                        saveUserToDatabase(user, name, hashedPassword, profileImageUrl);
                    }).addOnFailureListener(e -> {
                        Log.e("RegisterUser", "Error getting profile image download URL", e);
                        loadingDialogUtil.hideLoadingDialog();
                    });
                }).addOnFailureListener(e -> {
                    Log.e("RegisterUser", "Error uploading profile image", e);
                    loadingDialogUtil.hideLoadingDialog();
                });
    }

    private void saveUserToDatabase(FirebaseUser user, String name, String hashedPassword, String profileImageUrl) {
        String userId = user.getUid();
        String email = user.getEmail();
        String role = "Passenger";

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("password", hashedPassword);
        userData.put("role", role);
        userData.put("profileImageUrl", profileImageUrl);

        FirebaseUtil.getFirebaseFirestoreInstance().collection("users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterPassenger.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    loadingDialogUtil.hideLoadingDialog();
                    Intent intent = new Intent(RegisterPassenger.this, LoginPassenger.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegisterPassenger.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingDialogUtil.hideLoadingDialog();
                });
    }

    public InputStream getImageInputStreamFromDrawable(int drawableId) {
        return getResources().openRawResource(drawableId);
    }

}
