package com.apetta.detext_app.login;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.apetta.detext_app.alertDialog.ProgressAlertDialog;
import com.apetta.detext_app.navmenu.NavMenu;
import com.apetta.detext_app.R;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    ActivityResultLauncher<Intent> resultLauncher;
    Button signInBtn;
    EditText email, password;
    static Activity thisActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        thisActivity = this;
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {  // in case user has been already signed in
            new ProgressAlertDialog(this, getString(R.string.signing_in)).show();
            startActivity(new Intent(SignInActivity.this, NavMenu.class));
            return;
        }
        // in case user hasn't been signed in
        initResultLauncher();
        signInBtn = findViewById(R.id.signInButton);
        signInBtn.setEnabled(false);
        email = findViewById(R.id.emailSignIn);
        password = findViewById(R.id.passwordSignIn);
        setListeners();
    }

    /**
     * This method initializes the registerForActivityResult launcher
     */
    private void initResultLauncher() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        finish();
                    }
                }
        );
    }

    private void setListeners() {
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { checkTextChange(); }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { checkTextChange(); }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    /* user can't sign in if the required fields are empty */
    private void checkTextChange() {
        if (email.getText().toString().trim().length() != 0 && password.getText().toString().length() != 0) {
            signInBtn.setEnabled(true);
//            signInBtn.setBackgroundColor(ContextCompat.getColor(SignInActivity.this, R.color.bg_btn));
        }
        else {
            signInBtn.setEnabled(false);
//            signInBtn.setBackgroundColor(ContextCompat.getColor(SignInActivity.this, R.color.bg_btn_disabled));
        }
    }

    /* Move to another activity to sign up */
    public void goToSignUp(View view) {
        startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
    }

    public void signIn(View view) {
        ProgressAlertDialog progressAlertDialog = new ProgressAlertDialog(SignInActivity.this, getString(R.string.signing_in));
        progressAlertDialog.show();
        mAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString())
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        startActivity(new Intent(SignInActivity.this, NavMenu.class));
                    }
                    else {
                        progressAlertDialog.dismiss();
                        String errorMessage;
                        try {
                            errorMessage = task.getException().getLocalizedMessage();
                            // tts.setMessage(errorMessage);
                        }
                        catch (NullPointerException e) {
                            errorMessage = "Error description is not available.";
                            //tts.setMessage(getString(R.string.sthWentWrong));
                        }
                        new AlertDialog.Builder(SignInActivity.this)
                                .setTitle(getString(R.string.error))
                                .setMessage(errorMessage)
                                .show();
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuth.getCurrentUser() != null) finish();
    }
}