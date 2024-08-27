package com.toda.todamoon_v2.passenger.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

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
    private String selectedLanguageCode = "en";  // Default language code
    private ImageButton btn_back;
    private LoadingDialogUtil loadingDialogUtil;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_language);

        recyclerView = findViewById(R.id.recyclerViewLanguages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        languageList = new ArrayList<>();
        // Add your language items here
        languageList.add(new LanguageItem(R.drawable.ic_flag_uk, "English", "(UK)", "(English)", "en"));
        languageList.add(new LanguageItem(R.drawable.ic_flag_ph, "Filipino", "(Philippines)", "(Tagalog)", "tl"));
        // Add more items...

        // Get the saved language code
        selectedLanguageCode = getCurrentLanguageCode();

        adapter = new LanguageAdapter(this, languageList, selectedLanguageCode, this::changeLanguage);
        recyclerView.setAdapter(adapter);

        btn_back = findViewById(R.id.btnBacktoSettings);
        btn_back.setOnClickListener(v -> finish());

        loadingDialogUtil = new LoadingDialogUtil(this);
    }

    private void changeLanguage(String languageCode) {
        loadingDialogUtil.showLoadingDialog();

        // Update the locale
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        // Save the selected language in shared preferences
        saveLanguageCode(languageCode);

        // Restart the application to apply changes
        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        // Hide the loading dialog after a short delay
        new android.os.Handler().postDelayed(() -> loadingDialogUtil.hideLoadingDialog(), 1000);
    }

    private void saveLanguageCode(String languageCode) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("selected_language", languageCode);
        editor.apply();
    }

    private String getCurrentLanguageCode() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        return prefs.getString("selected_language", "en");  // Default to English
    }
}