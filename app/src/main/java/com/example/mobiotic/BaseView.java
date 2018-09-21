package com.example.mobiotic;

import android.view.View;

public interface BaseView {

    void showError(View view, String message);

    void showProgressDialog();

    void hideProgressDialog();
}
