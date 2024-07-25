package com.toda.todamoon_v2.utils;

import android.app.Dialog;
import android.content.Context;

import com.toda.todamoon_v2.R;

public class LoadingDialogUtil {

    private Dialog loadingDialog;

    public LoadingDialogUtil(Context context) {
        setupLoadingDialog(context);
    }

    private void setupLoadingDialog(Context context) {
        loadingDialog = new Dialog(context);
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