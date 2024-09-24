package com.toda.todamoon_v2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.toda.todamoon_v2.adapter.LanguageAdapter;
import com.toda.todamoon_v2.model.LanguageItem;
import com.toda.todamoon_v2.utils.LoadingDialogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DriverLanguage extends AppCompatActivity {

    private static final String TAG = "DriverLanguage";
    private static final String PREFS_NAME = "settings";
    private static final String KEY_SELECTED_LANGUAGE = "selected_language";
    private static final String DEFAULT_LANGUAGE = "en"; // Default to English
    private RecyclerView recyclerView;
    private LoadingDialogUtil loadingDialogUtil;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(updateBaseContextLocale(newBase));
    }

    private Context updateBaseContextLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String languageCode = prefs.getString(KEY_SELECTED_LANGUAGE, DEFAULT_LANGUAGE);

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);

        return context.createConfigurationContext(config);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_language);

        // Initialize UI components
        initializeUI();

        // Load saved language preference
        String savedLanguageCode = getCurrentLanguageCode();
        Log.d(TAG, "Saved language code onCreate: " + savedLanguageCode);
        applyLocale(savedLanguageCode);

        // Set up language options
        setupLanguageOptions(savedLanguageCode);
    }

    private void initializeUI() {
        recyclerView = findViewById(R.id.recyclerViewLanguages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ImageButton btnBack = findViewById(R.id.btnBacktoSettings);
        btnBack.setOnClickListener(v -> finish());

        loadingDialogUtil = new LoadingDialogUtil(this);
    }

    private void setupLanguageOptions(String savedLanguageCode) {
        List<LanguageItem> languageList = new ArrayList<>();
        // Add your language items here
        languageList.add(new LanguageItem(R.drawable.ic_flag_uk, "English", "(UK)", "(English)", "en"));
        languageList.add(new LanguageItem(R.drawable.ic_flag_ph, "Filipino", "(Philippines)", "(Tagalog)", "tl"));
        // Add more items...

        LanguageAdapter adapter = new LanguageAdapter(this, languageList, savedLanguageCode, this::changeLanguage);
        recyclerView.setAdapter(adapter);
    }

    private void applyLocale(String languageCode) {
        Log.d(TAG, "Applying locale: " + languageCode);
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void changeLanguage(String languageCode) {
        Log.d(TAG, "Changing language to: " + languageCode);
        loadingDialogUtil.showLoadingDialog();

        // Save the selected language
        saveLanguageCode(languageCode);

        // Apply the new locale
        applyLocale(languageCode);

        // Restart the application to apply changes
        restartApplication();
    }

    private void restartApplication() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Log.e(TAG, "Failed to restart application. Intent is null.");
        }

        // Hide the loading dialog after a short delay
        new android.os.Handler().postDelayed(loadingDialogUtil::hideLoadingDialog, 1000);
    }

    private void saveLanguageCode(String languageCode) {
        Log.d(TAG, "Saving language code: " + languageCode);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_SELECTED_LANGUAGE, languageCode);
        editor.apply();
    }

    private String getCurrentLanguageCode() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String languageCode = prefs.getString(KEY_SELECTED_LANGUAGE, DEFAULT_LANGUAGE);
        Log.d(TAG, "Retrieved language code: " + languageCode);
        return languageCode;
    }
}
