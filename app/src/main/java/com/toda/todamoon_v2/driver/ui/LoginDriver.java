package com.toda.todamoon_v2.driver.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.toda.todamoon_v2.ForgotPassword;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.utils.FirebaseUtil;
import com.toda.todamoon_v2.utils.LoadingDialogUtil;

import java.util.Locale;

public class LoginDriver extends AppCompatActivity {
    private LoadingDialogUtil loadingDialogUtil;
    private ImageView back;
    private TextView createAccount, forgotPass;
    private Button buttonLogin;
    private TextInputEditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String SHARED_PREFS = "sharedPrefs";

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
        setContentView(R.layout.activity_login_driver);
        initializeViews();
        setupListeners();
        checkIfLoggedIn();
    }

    private void initializeViews() {
        back = findViewById(R.id.btn_back);
        createAccount = findViewById(R.id.driver_create_account);
        buttonLogin = findViewById(R.id.driver_btn_login);
        emailEditText = findViewById(R.id.txt_driver_email_login);
        passwordEditText = findViewById(R.id.txt_driver_password_login);
        forgotPass = findViewById(R.id.driver_forgot_password_nav);

        mAuth = FirebaseUtil.getFirebaseAuthInstance();
        db = FirebaseUtil.getFirebaseFirestoreInstance();
        loadingDialogUtil = new LoadingDialogUtil(this);
    }

    private void setupListeners() {
        back.setOnClickListener(v -> finish());

        forgotPass.setOnClickListener(v -> startActivity(new Intent(LoginDriver.this, ForgotPassword.class)));
        createAccount.setOnClickListener(v -> startActivity(new Intent(LoginDriver.this, RegisterDriver.class)));

        buttonLogin.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            loginDriver(email, password);
        });
    }

    private void checkIfLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String check = prefs.getString("isLoggedIn", "");
        if (check.equals("true")) {
            autoLogin();
        }
    }

    private void autoLogin() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String isLoggedIn = prefs.getString("isLoggedIn", "");
        if ("true".equals(isLoggedIn)) {
            navigateToMainUI();
        }
    }

    private void loginDriver(String email, String password) {
        // Hash the password before signing in
        loadingDialogUtil.showLoadingDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loadingDialogUtil.hideLoadingDialog();
                    if (task.isSuccessful()) {
                        FirebaseUser loggedInUser = mAuth.getCurrentUser();
                        if (loggedInUser != null) {
                            checkUserRole(loggedInUser);
                            saveToSharedPreferences();
                        }
                    } else {
                        Toast.makeText(LoginDriver.this, "Login failed. Please check your credentials and try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole(FirebaseUser user) {
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("Driver".equals(role)) {
                            navigateToMainUI();  // Navigate only after all data is fetched
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

    private void saveToSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("isLoggedIn", "true");  // Save login status
        editor.apply();
    }

    private void navigateToMainUI() {
        Intent intent = new Intent(LoginDriver.this, DriverMainUI.class);
        startActivity(intent);
        Toast.makeText(LoginDriver.this, "Login successful", Toast.LENGTH_SHORT).show();
        finish();
    }
}
