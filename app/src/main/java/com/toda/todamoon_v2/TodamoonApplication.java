package com.toda.todamoon_v2;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class TodamoonApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        setLocale();
    }

    private void setLocale() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String languageCode = prefs.getString("selected_language", "en"); // Default to English

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}
