package com.example.mobiotic.signIn;

import com.example.mobiotic.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

public class SignInPresenter {
    private final SignInActivity activity;
    private SignInView signInView;
    private FirebaseAuth firebaseAuth;
    private ConstraintLayout rootLayout;

    public SignInPresenter(SignInActivity activity, SignInView signInView, FirebaseAuth firebaseAuth, ConstraintLayout rootLayout) {
        this.activity = activity;
        this.signInView = signInView;
        this.firebaseAuth = firebaseAuth;
        this.rootLayout = rootLayout;
    }

    private boolean isViewPresent() {
        return signInView != null;
    }


    public void verifyUserAlreadyLoggedIn() {
        if (firebaseAuth.getCurrentUser() != null) {
            signInView.goToListingPage();
        }
    }

    public boolean verifyGogglePlayNotPresent() {
        //todo check for playservice
        return false;
    }

    public void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        signInView.showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (isViewPresent()) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                signInView.goToListingPage();
                            } else {
                                signInView.showError(rootLayout, activity.getString(R.string.unable_to_sign));
                            }
                            signInView.hideProgressDialog();
                        }
                    }
                });
    }

    public void destroyView() {
        if (signInView != null) {
            signInView = null;
        }
    }
}
