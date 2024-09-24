package com.toda.todamoon_v2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.toda.todamoon_v2.driver.ui.DriverMainUI;
import com.toda.todamoon_v2.driver.ui.LoginDriver;
import com.toda.todamoon_v2.passenger.ui.LoginPassenger;
import com.toda.todamoon_v2.passenger.ui.PassengerMainUI;
import com.toda.todamoon_v2.utils.FirebaseUtil;
import com.toda.todamoon_v2.utils.LoadingDialogUtil;

import java.util.Locale;

public class FrontPage extends AppCompatActivity {

    private Button btn_go_to_pass_login, btn_go_to_driver_login;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String SHARED_PREFS = "sharedPrefs";
    private String isLoggedIn;
    private LoadingDialogUtil loadingDialogUtil;

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
        setContentView(R.layout.activity_front_page);

        mAuth = FirebaseUtil.getFirebaseAuthInstance();
        db = FirebaseUtil.getFirebaseFirestoreInstance();
        loadingDialogUtil = new LoadingDialogUtil(this);

        checkIfLoggedIn();
        initializeView();
        setButtonListeners();
    }

    private void checkIfLoggedIn() {

        if (mAuth.getCurrentUser() != null) {
            loadingDialogUtil.showLoadingDialog();
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    String role = document.getString("role");

                    if ("Driver".equals(role)) {
                        redirectToDriverMainUI();
                    } else if ("Passenger".equals(role)) {
                        redirectToPassengerMainUI();
                    } else {
                        loadingDialogUtil.hideLoadingDialog();
                    }
                }
            });
        } else {
            loadingDialogUtil.hideLoadingDialog();
        }
    }

    private void redirectToDriverMainUI() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        isLoggedIn = prefs.getString("isLoggedIn", "");

        Intent intent = new Intent(FrontPage.this, DriverMainUI.class);
        intent.putExtra("isLoggedIn", isLoggedIn);
        startActivity(intent);
        finish();

        loadingDialogUtil.hideLoadingDialog();
    }

    private void redirectToPassengerMainUI() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        isLoggedIn = prefs.getString("isLoggedIn", "");

        Intent intent = new Intent(FrontPage.this, PassengerMainUI.class);
        intent.putExtra("isLoggedIn", isLoggedIn);
        startActivity(intent);
        finish();

        loadingDialogUtil.hideLoadingDialog();
    }

    private void initializeView() {
        btn_go_to_pass_login = findViewById(R.id.btnGoToPassLogin);
        btn_go_to_driver_login = findViewById(R.id.btnGoToDriverLogin);
    }

    private void setButtonListeners() {
        btn_go_to_pass_login.setOnClickListener(v -> {
            Intent intent = new Intent(FrontPage.this, LoginPassenger.class);
            startActivity(intent);
        });

        btn_go_to_driver_login.setOnClickListener(v -> {
            Intent intent = new Intent(FrontPage.this, LoginDriver.class);
            startActivity(intent);
        });
    }
}
