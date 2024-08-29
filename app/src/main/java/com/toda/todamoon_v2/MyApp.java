package com.toda.todamoon_v2;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;

import java.util.Locale;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MyApp", "MyApp is created.");
        applySavedLocale();
    }

    private void applySavedLocale() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String languageCode = prefs.getString("selected_language", "en"); // Default to English
        Log.d("Locale", "Applying saved locale: " + languageCode);  // Log the language code

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        createConfigurationContext(config); // Apply locale to the application context
    }
}
