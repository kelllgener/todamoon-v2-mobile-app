package com.toda.todamoon_v2.passenger.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.toda.todamoon_v2.ForgotPassword;
import com.toda.todamoon_v2.FrontPage;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.utils.FirebaseUtil;
import com.toda.todamoon_v2.utils.LoadingDialogUtil;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LoginPassenger extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageButton btnBack;
    private TextView signupPassenger, forgotPass;
    private Button googleAuth, emailAuth;
    private TextInputEditText editEmail, editPassword;
    private GoogleSignInClient mGoogleSignInClient;
    private LoadingDialogUtil loadingDialogUtil;

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final int RC_SIGN_IN = 20;

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
        setContentView(R.layout.activity_login_passenger);

        initializeView();
        setButtonListeners();
        checkIfLoggedIn();
    }

    private void initializeView() {
        btnBack = findViewById(R.id.btn_back);
        signupPassenger = findViewById(R.id.passenger_create_account);
        googleAuth = findViewById(R.id.btn_google);
        emailAuth = findViewById(R.id.passenger_btn_login);

        editEmail = findViewById(R.id.txt_passenger_email_login);
        editPassword = findViewById(R.id.txt_passenger_password_login);
        forgotPass = findViewById(R.id.passenger_forgot_pass_nav);

        mAuth = FirebaseUtil.getFirebaseAuthInstance();
        db = FirebaseUtil.getFirebaseFirestoreInstance();
        String clientId = "853318778029-21fdbfsqqaqlvdhjoimnejuq511g8gd6.apps.googleusercontent.com";
        mGoogleSignInClient = GoogleSignIn.getClient(this, FirebaseUtil.getGoogleSignInOptions(clientId));

        loadingDialogUtil = new LoadingDialogUtil(this);
    }

    private void setButtonListeners() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPassenger.this, FrontPage.class);
            startActivity(intent);
        });

        forgotPass.setOnClickListener(v -> {
            startActivity(new Intent(LoginPassenger.this, ForgotPassword.class));
        });

        signupPassenger.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPassenger.this, RegisterPassenger.class);
            startActivity(intent);
        });

        googleAuth.setOnClickListener(v -> signInWithGoogle());
        emailAuth.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            loginUser(email, password);
        });
    }

    private void checkIfLoggedIn() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String isLoggedIn = prefs.getString("isLoggedIn", "");
        if ("true".equals(isLoggedIn)) {
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

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        loadingDialogUtil.showLoadingDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loadingDialogUtil.hideLoadingDialog();
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                addUserToFirestore(user);
                            } else {
                                Toast.makeText(LoginPassenger.this, "Failed to retrieve user information.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginPassenger.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addUserToFirestore(FirebaseUser user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("email", user.getEmail());
        userData.put("name", user.getDisplayName());
        userData.put("profileImage", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        userData.put("role", "Passenger");

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    saveToSharedPreferences();
                    navigateToMainUI();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginPassenger.this, "Failed to add user to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                });
    }

    private void loginUser(String email, String password) {
        loadingDialogUtil.showLoadingDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    loadingDialogUtil.hideLoadingDialog();
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkUserRole(user);
                            saveToSharedPreferences();
                        }
                    } else {
                        Toast.makeText(LoginPassenger.this, "Login failed. Please check your credentials and try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole(FirebaseUser user) {
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("Passenger".equals(role)) {
                            navigateToMainUI();
                        } else {
                            Toast.makeText(LoginPassenger.this, "Only passengers are allowed to log in.", Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(LoginPassenger.this, "User role not found.", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginPassenger.this, "Failed to check user role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(LoginPassenger.this, PassengerMainUI.class);
        startActivity(intent);
        Toast.makeText(LoginPassenger.this, "Login successful", Toast.LENGTH_SHORT).show();
        finish();
    }
}
