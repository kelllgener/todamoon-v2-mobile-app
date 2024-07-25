package com.toda.todamoon_v2.utils;

import android.app.Dialog;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.toda.todamoon_v2.R;

public class ProgressBar extends AppCompatActivity {

    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_progress_bar);

    }

    public void setupLoadingDialog() {
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.activity_progress_bar);
        loadingDialog.setCancelable(false);
    }

    public void showLoadingDialog() {
        if (loadingDialog != null && !loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    public void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}