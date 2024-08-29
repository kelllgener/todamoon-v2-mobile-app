package com.toda.todamoon_v2.passenger.ui;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.toda.todamoon_v2.FrontPage;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.driver.ui.LoginDriver;
import com.toda.todamoon_v2.utils.AndroidUtil;
import com.toda.todamoon_v2.utils.FirebaseUtil;
import com.toda.todamoon_v2.utils.LoadingDialogUtil;

import java.util.HashMap;
import java.util.Map;

public class LoginPassenger extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView signupPassenger;
    private Button googleAuth, emailAuth;
    private TextInputEditText editEmail, editPassword;
    private AndroidUtil androidUtil;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private LoadingDialogUtil loadingDialogUtil;

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final int RC_SIGN_IN = 20;
    private String clientId = "853318778029-21fdbfsqqaqlvdhjoimnejuq511g8gd6.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_passenger);

        androidUtil = new AndroidUtil();

        initializeView();
        setButtonListeners();
        checkAutoLogin();
    }

    private void initializeView() {
        btnBack = findViewById(R.id.btnBack);
        signupPassenger = findViewById(R.id.signUpPassenger);
        googleAuth = findViewById(R.id.btnGoogle);
        emailAuth = findViewById(R.id.btnLogin);

        editEmail = findViewById(R.id.emailPassenger);
        editPassword = findViewById(R.id.passwordPassenger);

        mAuth = FirebaseUtil.getFirebaseAuthInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this, FirebaseUtil.getGoogleSignInOptions(clientId));

        loadingDialogUtil = new LoadingDialogUtil(this);
    }

    private void setButtonListeners() {
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPassenger.this, FrontPage.class);
            startActivity(intent);
        });

        signupPassenger.setOnClickListener(v -> {
            Intent intent = new Intent(LoginPassenger.this, RegisterPassenger.class);
            startActivity(intent);
        });

        googleAuth.setOnClickListener(v -> signInWithGoogle());
        emailAuth.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            String hashedPassword = androidUtil.hashPassword(password);

            loginUser(email, hashedPassword);
        });
    }

    private void checkAutoLogin() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String isLoggedIn = prefs.getString("isLoggedIn", "");
        if ("true".equals(isLoggedIn)) {
            autoLogin();
        }
    }

    private void autoLogin() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String email = prefs.getString("email", "");
        String name = prefs.getString("name", "");
        String passengerUid = prefs.getString("passengerUid", "");
        String profileUri = prefs.getString("profileUri", "");

        proceedToMainUI(email, name, passengerUid, profileUri);
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
                        }
                    } else {
                        Toast.makeText(LoginPassenger.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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
        FirebaseFirestore db = FirebaseUtil.getFirebaseFirestoreInstance();

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("email", user.getEmail());
        userData.put("name", user.getDisplayName());
        userData.put("profileUri", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        userData.put("role", "Passenger");

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    saveToSharedPreferences(user);
                    updateUI(user);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginPassenger.this, "Failed to add user to Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                });
    }

    private void checkUserRole(FirebaseUser user) {
        FirebaseFirestore db = FirebaseUtil.getFirebaseFirestoreInstance();
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("Passenger".equals(role)) {
                            saveToSharedPreferences(user);
                            updateUI(user);
                            Toast.makeText(LoginPassenger.this, "Login successful", Toast.LENGTH_SHORT).show();
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

    private void saveToSharedPreferences(FirebaseUser user) {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Save email
        editor.putString("email", user.getEmail());

        // Save name and profile image based on sign-in method
        if (user.getDisplayName() != null) {
            // Google sign-in
            editor.putString("name", user.getDisplayName());
            editor.putString("profileUri", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");

        } else {
            // Email/password sign-in
            // Assume that user data from Firestore should be used
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    String name = document.getString("name");
                    String profileUri = document.getString("profileUri");

                    // Save name and profile image URL from Firestore
                    editor.putString("name", name != null ? name : "");
                    editor.putString("profileUri", profileUri != null ? profileUri : "");
                    editor.apply();  // Apply changes after setting values
                } else {
                    // Handle failure case
                    Toast.makeText(this, "Failed to retrieve user data from Firestore", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Save UID and login status
        editor.putString("passengerUid", user.getUid());
        editor.putString("isLoggedIn", "true");
        editor.apply();
    }


    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String userId = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(userId).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {

                            DocumentSnapshot document = task.getResult();

                            // Extract all required fields
                            String email = document.getString("email");
                            String name = document.getString("name");
                            String passengerUid = document.getString("uid");
                            final String[] profileUri = {document.getString("profileUri")};

                            // If profileUri is not available, fetch it from Firebase Storage
                            if (profileUri[0] == null || profileUri[0].isEmpty()) {
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference profileRef = storage.getReference().child("profile_images/" + userId + ".png");

                                profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    profileUri[0] = uri.toString();
                                    proceedToMainUI(email, name, passengerUid, profileUri[0]);
                                }).addOnFailureListener(exception -> {
                                    Toast.makeText(LoginPassenger.this, "Failed to retrieve profile image URL", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Failed to retrieve profile image URL: ", exception);
                                });
                            } else {
                                // If profileUri is available directly
                                proceedToMainUI(email, name, passengerUid, profileUri[0]);
                            }
                        } else {
                            Toast.makeText(LoginPassenger.this, "Failed to retrieve user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void proceedToMainUI(String email, String name, String passengerUid, String profileUri) {
        Intent intent = new Intent(LoginPassenger.this, PassengerMainUI.class);
        intent.putExtra("uid", passengerUid);
        intent.putExtra("email", email);
        intent.putExtra("name", name);
        intent.putExtra("profileUri", profileUri);
        startActivity(intent);
        finish();
    }
}
