package com.toda.todamoon_v2.driver.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.databinding.ActivityDriverMainUiBinding;
import com.toda.todamoon_v2.driver.fragments.DriverCategoryFragment;
import com.toda.todamoon_v2.driver.fragments.DriverGenerateQrFragment;
import com.toda.todamoon_v2.driver.fragments.DriverHomeFragment;
import com.toda.todamoon_v2.driver.fragments.DriverSettingsFragment;

import java.util.Locale;

public class DriverMainUI extends AppCompatActivity {
    ActivityDriverMainUiBinding binding;

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
        setContentView(R.layout.activity_driver_main_ui);

        binding = ActivityDriverMainUiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new DriverHomeFragment());

        navigateListener();
    }

    private void navigateListener() {
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_driver_home) {
                replaceFragment(new DriverHomeFragment());
            } else if (itemId == R.id.menu_driver_qrcode) {
                replaceFragment(new DriverGenerateQrFragment());
            } else if (itemId == R.id.menu_driver_category) {
                replaceFragment(new DriverCategoryFragment());
            } else if (itemId == R.id.menu_driver_settings) {
                replaceFragment(new DriverSettingsFragment());
            } else {
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
