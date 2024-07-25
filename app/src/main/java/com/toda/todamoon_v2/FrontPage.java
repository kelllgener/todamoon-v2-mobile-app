package com.toda.todamoon_v2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.toda.todamoon_v2.driver.ui.LoginDriver;
import com.toda.todamoon_v2.passenger.ui.LoginPassenger;
import com.toda.todamoon_v2.utils.FirebaseUtil;

public class FrontPage extends AppCompatActivity {

    private Button btn_go_to_pass_login, btn_go_to_driver_login;
    private  FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_front_page);

        initializeView();
        setButtonListeners();

    }

    private void initializeView() {

        mAuth = FirebaseUtil.getFirebaseAuthInstance();

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