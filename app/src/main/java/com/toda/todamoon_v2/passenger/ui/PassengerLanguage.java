package com.toda.todamoon_v2.passenger.ui;

import android.annotation.SuppressLint;
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

import com.toda.todamoon_v2.R;
import com.toda.todamoon_v2.adapter.LanguageAdapter;
import com.toda.todamoon_v2.model.LanguageItem;
import com.toda.todamoon_v2.utils.LoadingDialogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PassengerLanguage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LanguageAdapter adapter;
    private List<LanguageItem> languageList;
    private ImageButton btn_back;
    private LoadingDialogUtil loadingDialogUtil;

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE);
        String languageCode = prefs.getString("selected_language", "en"); // Default to English

        Locale newLocale = new Locale(languageCode);
        Locale.setDefault(newLocale);

        Configuration config = newBase.getResources().getConfiguration();
        config.setLocale(newLocale);

        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply the saved language before setting the content view
        String savedLanguageCode = getCurrentLanguageCode();
        Log.d("DriverLanguage", "Saved language code onCreate: " + savedLanguageCode);
        applyLocale(savedLanguageCode);

        setContentView(R.layout.activity_driver_language);

        recyclerView = findViewById(R.id.recyclerViewLanguages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        languageList = new ArrayList<>();
        // Add your language items here
        languageList.add(new LanguageItem(R.drawable.ic_flag_uk, "English", "(UK)", "(English)", "en"));
        languageList.add(new LanguageItem(R.drawable.ic_flag_ph, "Filipino", "(Philippines)", "(Tagalog)", "tl"));
        // Add more items...

        adapter = new LanguageAdapter(this, languageList, savedLanguageCode, this::changeLanguage);
        recyclerView.setAdapter(adapter);

        btn_back = findViewById(R.id.btnBacktoSettings);
        btn_back.setOnClickListener(v -> finish());

        loadingDialogUtil = new LoadingDialogUtil(this);
    }

    private void applyLocale(String languageCode) {
        Log.d("Locale", "Applying locale: " + languageCode);
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        getApplicationContext().createConfigurationContext(config); // This ensures the context is updated
    }

    private void changeLanguage(String languageCode) {
        Log.d("Locale", "Changing language to: " + languageCode);  // Log the new language code
        loadingDialogUtil.showLoadingDialog();

        // Save the selected language in shared preferences
        saveLanguageCode(languageCode);

        // Update the locale
        applyLocale(languageCode);

        // Restart the application to apply changes
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Log.e("Locale", "Failed to restart application. Intent is null.");
        }

        // Hide the loading dialog after a short delay
        new android.os.Handler().postDelayed(() -> loadingDialogUtil.hideLoadingDialog(), 1000);
    }

    private void saveLanguageCode(String languageCode) {
        Log.d("Preferences", "Saving language code: " + languageCode);
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("selected_language", languageCode);
        editor.apply();
    }

    private String getCurrentLanguageCode() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String languageCode = prefs.getString("selected_language", "en");  // Default to English
        Log.d("Preferences", "Retrieved language code: " + languageCode);
        return languageCode;
    }
}
