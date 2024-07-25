package com.toda.todamoon_v2.driver.ui;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.databinding.ActivityDriverMainUiBinding;
import com.toda.todamoon_v2.driver.fragments.DriverCategoryFragment;
import com.toda.todamoon_v2.driver.fragments.DriverGenerateQrFragment;
import com.toda.todamoon_v2.driver.fragments.DriverHomeFragment;
import com.toda.todamoon_v2.driver.fragments.DriverNotificationFragment;
import com.toda.todamoon_v2.driver.fragments.DriverSettingsFragment;
import com.toda.todamoon_v2.driver.fragments.JoinQueueFragment;

public class DriverMainUI extends AppCompatActivity {

    // VIEWS
    private View homeView;
    private View categoryView;
    private View settingsView;
    private View profileView;

    // PROFILE
    private View SettingsFrag;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ActivityDriverMainUiBinding binding;

    private void initializeView() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_main_ui);

        binding = ActivityDriverMainUiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new DriverHomeFragment());

        initializeView();
        navigateListener();



    }

    private void navigateListener() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();
            if (itemId == R.id.menu_driver_home) {
                replaceFragment(new DriverHomeFragment());
            } else if (itemId == R.id.menu_driver_qrcode) {
                // Retrieve user data from intent
                // Show loading dialog

                String name = getIntent().getStringExtra("name");
                String driverUid = getIntent().getStringExtra("driverUid");
                String tricycleNumber = getIntent().getStringExtra("tricycleNumber");
                String qrCodeUrl = getIntent().getStringExtra("qrCodeUrl");


                // Pass user data to ProfileFragment
                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                bundle.putString("driverUid", driverUid);
                bundle.putString("tricycleNumber", tricycleNumber);
                bundle.putString("qrCodeUrl", qrCodeUrl);


                DriverGenerateQrFragment qrCodeDriverFragment = DriverGenerateQrFragment.newInstance(driverUid, name, tricycleNumber, qrCodeUrl);

                // Replace the current fragment with ProfileFragment
                replaceFragment(qrCodeDriverFragment);

                // Hide loading dialog after fragment transaction (if it takes some time)
                // You might want to hide it within the fragment or after some delay if needed

            } else if (itemId == R.id.menu_driver_category) {
                replaceFragment(new DriverCategoryFragment());
            }
//            else if (itemId == R.id.menu_driver_notification) {
//                replaceFragment(new DriverNotificationFragment());
//            }
            else if (itemId == R.id.menu_driver_settings) {

                // Retrieve user data from intent
                String email = getIntent().getStringExtra("email");
                String name = getIntent().getStringExtra("name");
                String profileUri = getIntent().getStringExtra("profileUri");

                // Pass user data to ProfileFragment
                Bundle bundle = new Bundle();
                bundle.putString("email", email);
                bundle.putString("name", name);
                bundle.putString("profileUri", profileUri);

                DriverSettingsFragment settingsDriverFragment = DriverSettingsFragment.newInstance(email, name, profileUri);

                // Replace the current fragment with ProfileFragment
                replaceFragment(settingsDriverFragment);

            }

            else if (itemId == R.id.menu_driver_join_queue) {
                replaceFragment(new JoinQueueFragment());
            }

            else {
                throw new IllegalStateException("Unexpected value: " + itemId);
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();


    }
}