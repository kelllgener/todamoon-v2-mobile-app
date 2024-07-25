
package com.toda.todamoon_v2.passenger.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.toda.todamoon_v2.FrontPage;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.driver.ui.LoginDriver;
import com.toda.todamoon_v2.model.PassengerGetterSetter;
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

    private static final int RC_SIGN_IN = 20;
    private String clientId = "853318778029-21fdbfsqqaqlvdhjoimnejuq511g8gd6.apps.googleusercontent.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_passenger);

        androidUtil = new AndroidUtil();

        initializeView();
        setButtonListeners();
    }

    private void initializeView() {
        // Back button
        btnBack = findViewById(R.id.btnBack);
        signupPassenger = findViewById(R.id.signUpPassenger);
        googleAuth = findViewById(R.id.btnGoogle);
        emailAuth = findViewById(R.id.btnLogin);

        //FORMS
        editEmail = findViewById(R.id.emailPassenger);
        editPassword = findViewById(R.id.passwordPassenger);

        // Firebase
        mAuth = FirebaseUtil.getFirebaseAuthInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this, FirebaseUtil.getGoogleSignInOptions(clientId));

        // Loading dialog
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
            // Hash the password before signing in
            String hashedPassword = androidUtil.hashPassword(password);

            loginUser(email, hashedPassword);
        });
    }
    private void loginUser(String email, String password) {
        loadingDialogUtil.showLoadingDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        loadingDialogUtil.hideLoadingDialog();
                        // Login success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check user role before proceeding
                            checkUserRole(user);
                        }
                    } else {
                        // Login failed
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
                                // Update Firestore with user credentials
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

        // Create a new user document in Firestore
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("name", user.getDisplayName());
        userData.put("profileUri", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
        userData.put("role", "Passenger");

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    // User added successfully
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

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String userId = user.getUid();
            String email = user.getEmail();
            final String[] profileUri = new String[1];

            // Retrieve user's name from Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(userId);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");

                        // Check if user authenticated with Google (using their provided photo URL)
                        if (user.getPhotoUrl() != null) {
                            profileUri[0] = user.getPhotoUrl().toString();
                            proceedToMainUI(email, name, profileUri[0]);
                        } else {
                            // If not Google authentication, retrieve profile picture from storage
                            StorageReference profileRef = FirebaseStorage.getInstance().getReference()
                                    .child("profile_images/" + userId + ".png");

                            // Download the profile image URL asynchronously
                            profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                String profileImageUrl = uri.toString();
                                proceedToMainUI(email, name, profileImageUrl);
                            }).addOnFailureListener(e -> {
                                Toast.makeText(LoginPassenger.this, "Failed to retrieve profile image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                proceedToMainUI(email, name, "");
                            });
                        }
                    } else {
                        // Handle case where the document does not exist
                        Toast.makeText(LoginPassenger.this, "User document does not exist.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle failure to retrieve document
                    Toast.makeText(LoginPassenger.this, "Failed to retrieve user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Handle sign-out state or authentication failure
        }
    }

    private void proceedToMainUI(String email, String name, String profileUri) {
        Intent intent = new Intent(LoginPassenger.this, PassengerMainUI.class);
        intent.putExtra("email", email);
        intent.putExtra("name", name);
        intent.putExtra("profileUri", profileUri);
        startActivity(intent);
        finish();
    }

}