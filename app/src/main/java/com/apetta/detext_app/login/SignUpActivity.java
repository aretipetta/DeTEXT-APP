package com.apetta.detext_app.login;

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
import android.widget.Toast;

import com.apetta.detext_app.navmenu.NavMenu;
import com.apetta.detext_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    Button signUpBtn;
    EditText email, password, confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        signUpBtn = findViewById(R.id.signUpButton);
        email = findViewById(R.id.emailSignUp);
        password = findViewById(R.id.passwordSignUp);
        confirmPassword = findViewById(R.id.confPasswordSignUp);
        signUpBtn.setEnabled(false);
        setListeners();
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

        confirmPassword.addTextChangedListener(new TextWatcher() {
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
        if (email.getText().toString().trim().length() != 0 && password.getText().toString().length() != 0
        && confirmPassword.getText().length() != 0) {
            signUpBtn.setEnabled(true);
            signUpBtn.setBackgroundColor(ContextCompat.getColor(SignUpActivity.this, R.color.bg_btn));
        }
        else {
            signUpBtn.setEnabled(false);
            signUpBtn.setBackgroundColor(ContextCompat.getColor(SignUpActivity.this, R.color.bg_btn_disabled));
        }
    }

    public void signUp(View view) {
        if(!password.getText().toString().equals(confirmPassword.getText().toString())) {
            Toast.makeText(this, "Not valid passwords.", Toast.LENGTH_SHORT).show();
            return;
        }
        // an einai idia ta passwords
        mAuth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            // dhmioyrgia user kai apothikeush sth vash kai meta metavash se neo activity
                            createUser();
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
                        }
                    }
                });
    }

    private void createUser() {
        // apothikeush sth vash kai meta metavash se neo activity
        User user = new User(email.getText().toString().trim());
        database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef =database.getReference("users/" + mAuth.getCurrentUser().getUid());
        dbRef.setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                setResult(Activity.RESULT_OK);
                SignInActivity.thisActivity.finish();
                startActivity(new Intent(SignUpActivity.this, NavMenu.class));
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuth.getCurrentUser() != null) finish();
    }

}