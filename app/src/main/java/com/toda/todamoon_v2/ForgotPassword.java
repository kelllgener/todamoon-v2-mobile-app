// ForgotPassword.java
package com.toda.todamoon_v2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

import java.util.Locale;

public class ForgotPassword extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private TextView resultTextView;

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

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.emailEditText);
        Button resetPasswordButton = findViewById(R.id.resetPasswordButton);
        resultTextView = findViewById(R.id.resultTextView);
        ImageButton forgotBackButton = findViewById(R.id.btnBackForgotPass);

        forgotBackButton.setOnClickListener(v -> finish());

        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                resultTextView.setText("Please enter your email.");
                resultTextView.setVisibility(View.VISIBLE);
                return;
            }

            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            resultTextView.setText("Password reset email sent!");
                            resultTextView.setVisibility(View.VISIBLE);
                        } else {
                            String errorMessage;
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                errorMessage = "Email is not registered.";
                            } else if (task.getException() instanceof FirebaseAuthException) {
                                errorMessage = "Failed to send reset email. Try again.";
                            } else {
                                errorMessage = "An unknown error occurred.";
                            }
                            resultTextView.setText(errorMessage);
                            resultTextView.setVisibility(View.VISIBLE);
                        }
                    });
        });
    }
}
