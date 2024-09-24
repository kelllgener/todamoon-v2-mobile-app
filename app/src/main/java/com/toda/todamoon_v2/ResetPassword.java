package com.toda.todamoon_v2;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class ResetPassword extends AppCompatActivity {
    private ImageButton btnBack;
    private EditText currentPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private FirebaseAuth auth;

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
        setContentView(R.layout.activity_reset_password);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Find views
        btnBack = findViewById(R.id.btnBacktoSettings);
        currentPasswordEditText = findViewById(R.id.currentPassword);
        newPasswordEditText = findViewById(R.id.newPassword);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        Button resetPasswordButton = findViewById(R.id.addFromGalleryButton);

        // Set up back button
        btnBack.setOnClickListener(v -> finish());

        // Set up reset password button
        resetPasswordButton.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(currentPassword)) {
            currentPasswordEditText.setError("Current password is required");
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            newPasswordEditText.setError("New password is required");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return;
        }

        // Assuming the user is logged in, you will need to reauthenticate the user first
        String email = auth.getCurrentUser().getEmail();
        if (email != null) {
            auth.signInWithEmailAndPassword(email, currentPassword)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Re-authentication successful, now update the password
                            auth.getCurrentUser().updatePassword(newPassword)
                                    .addOnCompleteListener(this, updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(ResetPassword.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            finish(); // Close the activity
                                        } else {
                                            Toast.makeText(ResetPassword.this, "Error updating password", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(ResetPassword.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(ResetPassword.this, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }
}
