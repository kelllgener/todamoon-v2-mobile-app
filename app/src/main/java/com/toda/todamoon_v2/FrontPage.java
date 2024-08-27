package com.toda.todamoon_v2;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class FrontPage extends AppCompatActivity {

    private Button btn_go_to_pass_login, btn_go_to_driver_login;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private static final String SHARED_PREFS = "sharedPrefs";
    private String email, name, driverUid, tricycleNumber, qrCodeUrl, profileImageUrl;
    private LoadingDialogUtil loadingDialogUtil;

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
                    }
                }
            });
        }
    }

    private void redirectToDriverMainUI() {

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        email = prefs.getString("email", "");
        name = prefs.getString("name", "");
        driverUid = prefs.getString("driverUid", "");
        tricycleNumber = prefs.getString("tricycleNumber", "");
        qrCodeUrl = prefs.getString("qrCodeUrl", "");
        profileImageUrl = prefs.getString("profileImageUrl", "");

        Intent intent = new Intent(FrontPage.this, DriverMainUI.class);
        intent.putExtra("email", email);

        intent.putExtra("name", name);
        intent.putExtra("driverUid", driverUid);
        intent.putExtra("tricycleNumber", tricycleNumber);
        intent.putExtra("qrCodeUrl", qrCodeUrl);
        intent.putExtra("profileUri", profileImageUrl); // Add profile image URL to intent
        startActivity(intent);

        finish();  // Finish the FrontPage activity so the user can't go back to it
        loadingDialogUtil.hideLoadingDialog();
    }

    private void redirectToPassengerMainUI() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String email = prefs.getString("email", "");
        String passengerUid = prefs.getString("passengerUid", "");

        Intent intent = new Intent(FrontPage.this, PassengerMainUI.class);
        intent.putExtra("email", email);
        intent.putExtra("passengerUid", passengerUid);
        startActivity(intent);
        finish();  // Finish the FrontPage activity so the user can't go back to it
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
