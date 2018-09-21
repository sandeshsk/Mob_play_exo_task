package com.example.mobiotic;

import android.app.ProgressDialog;
import android.view.View;

import com.example.mobiotic.util.AppUtil;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity implements BaseView {
    private ProgressDialog progressDialog;

    @Override
    public void showError(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showProgressDialog() {
        hideProgressDialog();
        progressDialog = AppUtil.showLoadingDialog(this);

    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }
}
