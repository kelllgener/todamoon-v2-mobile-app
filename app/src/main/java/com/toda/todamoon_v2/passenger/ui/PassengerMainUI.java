package com.toda.todamoon_v2.passenger.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.databinding.ActivityPassengerMainUiBinding;
import com.toda.todamoon_v2.passenger.fragments.PassengerHomeFragment;
import com.toda.todamoon_v2.passenger.fragments.PassengerSettingsFragment;

import java.util.Locale;

public class PassengerMainUI extends AppCompatActivity {
    ActivityPassengerMainUiBinding binding;

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
        setContentView(R.layout.activity_passenger_main_ui);

        binding = ActivityPassengerMainUiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new PassengerHomeFragment());

        navigateListener();

    }

    private void navigateListener() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.menu_passenger_home) {
                replaceFragment(new PassengerHomeFragment());
            }
            else if (itemId == R.id.menu_passenger_settings) {
                replaceFragment(new PassengerSettingsFragment());
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