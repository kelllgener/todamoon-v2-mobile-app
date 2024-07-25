package com.toda.todamoon_v2.driver.ui;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.passenger.ui.LoginPassenger;
import com.toda.todamoon_v2.utils.AndroidUtil;
import com.toda.todamoon_v2.utils.FirebaseUtil;
import com.toda.todamoon_v2.utils.LoadingDialogUtil;


public class LoginDriver extends AppCompatActivity {

    private AndroidUtil androidUtil;
    private LoadingDialogUtil loadingDialogUtil;
    private ImageView back;
    private TextView createAccount;
    private Button buttonLogin;
    private TextInputEditText emailEditText, passwordEditText;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_driver);
        initializeViews();
        setupListeners();

    }

    private void initializeViews() {
        back = findViewById(R.id.btn_back);
        createAccount = findViewById(R.id.create_account);
        buttonLogin = findViewById(R.id.btnLoginDriver);
        emailEditText = findViewById(R.id.editEmailDriver);
        passwordEditText = findViewById(R.id.editPasswordDriver);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseUtil.getFirebaseAuthInstance();

        androidUtil = new AndroidUtil();
        loadingDialogUtil = new LoadingDialogUtil(this);
    }

    private void setupListeners() {
        back.setOnClickListener(v -> finish());

        createAccount.setOnClickListener(v -> startActivity(new Intent(LoginDriver.this, RegisterDriver.class)));

        buttonLogin.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            loginDriver(email, password);
        });
    }

    private void loginDriver(String email, String password) {
        // Hash the password before signing in
        String hashedPassword = androidUtil.hashPassword(password);
        loadingDialogUtil.showLoadingDialog();
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, hashedPassword)
                .addOnCompleteListener(this, task -> {
                    loadingDialogUtil.hideLoadingDialog();
                    if (task.isSuccessful()) {
                        // Login success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check user role before proceeding
                            checkUserRole(user);
                        }
                    } else {
                        Toast.makeText(LoginDriver.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole(FirebaseUser user) {
        FirebaseFirestore db = FirebaseUtil.getFirebaseFirestoreInstance();
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("Driver".equals(role)) {
                            updateUI(user);
                            Toast.makeText(LoginDriver.this, "Login successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginDriver.this, "Only drivers are allowed to log in.", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(LoginDriver.this, "User role not found.", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginDriver.this, "Failed to check user role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String email = document.getString("email");
                                String name = document.getString("name");
                                String driverUid = document.getString("uid");
                                String tricycleNumber = document.getString("tricycleNumber");

                                // Retrieve the QR code URL from Firebase Storage
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference qrCodeRef = storage.getReference().child("qrcodes/" + driverUid + ".png");

                                qrCodeRef.getDownloadUrl().addOnSuccessListener(qrCodeUri -> {
                                    String qrCodeUrl = qrCodeUri.toString();

                                    // Retrieve the profile image URL from Firebase Storage
                                    StorageReference profileRef = storage.getReference().child("profile_images/" + userId + ".png");

                                    profileRef.getDownloadUrl().addOnSuccessListener(profileUri -> {
                                        String profileImageUrl = profileUri.toString();

                                        Intent intent = new Intent(LoginDriver.this, DriverMainUI.class);
                                        intent.putExtra("email", email);
                                        intent.putExtra("name", name);
                                        intent.putExtra("driverUid", driverUid);
                                        intent.putExtra("tricycleNumber", tricycleNumber);
                                        intent.putExtra("qrCodeUrl", qrCodeUrl);
                                        intent.putExtra("profileUri", profileImageUrl); // Add profile image URL to intent
                                        startActivity(intent);
                                        finish();

                                    }).addOnFailureListener(profileException -> {
                                        Toast.makeText(LoginDriver.this, "Failed to retrieve profile image URL", Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "Failed to retrieve profile image URL: ", profileException);
                                    });

                                }).addOnFailureListener(qrCodeException -> {
                                    Toast.makeText(LoginDriver.this, "Failed to retrieve QR code URL", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Failed to retrieve QR code URL: ", qrCodeException);
                                });

                            } else {
                                Toast.makeText(LoginDriver.this, "No such user data exists", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginDriver.this, "Failed to retrieve user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


}