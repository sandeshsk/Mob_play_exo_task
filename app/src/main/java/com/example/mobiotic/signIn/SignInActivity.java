package com.example.mobiotic.signIn;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.mobiotic.BaseActivity;
import com.example.mobiotic.R;
import com.example.mobiotic.listingPage.ListingActivity;
import com.example.mobiotic.util.AppUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import androidx.constraintlayout.widget.ConstraintLayout;

public class SignInActivity extends BaseActivity implements SignInView {


    private static final int RC_SIGN_IN = 1001;
    private GoogleSignInClient googleSignInClient;
    private SignInPresenter presenter;
    ConstraintLayout signInLayout;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        signInLayout = findViewById(R.id.sign_in_layout);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        presenter = new SignInPresenter(this, this, firebaseAuth, signInLayout);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (presenter.verifyGogglePlayNotPresent()) {
            showError(signInLayout, getString(R.string.play_service_error));
            finish();
        } else {
            presenter.verifyUserAlreadyLoggedIn();
        }

    }

    @Override
    public void goToListingPage() {
        startActivity(new Intent(this, ListingActivity.class));
        finish();
    }

    public void signIn(View view) {
        if (AppUtil.isOnline(getApplicationContext())) {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } else {
            showError(signInLayout, getString(R.string.no_internet_connection));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                presenter.firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                showError(signInLayout, getString(R.string.sign_in_error));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.destroyView();
    }
}
