package com.apetta.detext_app.login;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.apetta.detext_app.navmenu.NavMenu;
import com.apetta.detext_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
        if(mAuth.getCurrentUser() != null) {
            startActivity(new Intent(SignInActivity.this, NavMenu.class));
            return;
        }
        // an einai null
        initResultLauncher();
        signInBtn = findViewById(R.id.signInButton);
        signInBtn.setEnabled(false);
        email = findViewById(R.id.emailSignIn);
        password = findViewById(R.id.passwordSignIn);
        setListeners();
    }

    private void initResultLauncher() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            finish();
                        }
                    }
                }
        );
    }

    private void setListeners() {
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkTextChange();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkTextChange();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }


    private void checkTextChange() {
        if (email.getText().toString().trim().length() != 0 && password.getText().toString().length() != 0) {
            signInBtn.setEnabled(true);
            signInBtn.setBackgroundColor(ContextCompat.getColor(SignInActivity.this, R.color.bg_btn));
        }
        else {
            signInBtn.setEnabled(false);
            signInBtn.setBackgroundColor(ContextCompat.getColor(SignInActivity.this, R.color.bg_btn_disabled));
        }
    }

    public void goToSignUp(View view) {
        startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
    }

    public void signIn(View view) {
        mAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            startActivity(new Intent(SignInActivity.this, NavMenu.class));
                        }
                        else {
                            String errorMessage;
                            try {
                                errorMessage = task.getException().getLocalizedMessage();
                                // tts.setMessage(errorMessage);
                            }
                            catch (NullPointerException e) {
                                errorMessage = "Error description is not available.";
                                //tts.setMessage(getString(R.string.sthWentWrong));
                            }
                            // some alert dialog
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuth.getCurrentUser() != null) finish();
    }
}