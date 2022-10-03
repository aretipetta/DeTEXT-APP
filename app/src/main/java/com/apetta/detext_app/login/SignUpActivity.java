package com.apetta.detext_app.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.apetta.detext_app.alertDialog.ProgressAlertDialog;
import com.apetta.detext_app.navmenu.NavMenu;
import com.apetta.detext_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    Button signUpBtn;
    EditText email, password, confirmPassword;
    final static int REQ_LOC_CODE = 23;
    ProgressAlertDialog progressAlertDialog;

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

        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { checkTextChange(); }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void checkTextChange() {
        if (email.getText().toString().trim().length() != 0 && password.getText().toString().length() != 0
        && confirmPassword.getText().length() != 0)  signUpBtn.setEnabled(true);
        else signUpBtn.setEnabled(false);
    }

    public void signUp(View view) {
        progressAlertDialog = new ProgressAlertDialog(SignUpActivity.this, getString(R.string.signing_up));
        progressAlertDialog.show();
        if(!password.getText().toString().equals(confirmPassword.getText().toString())) {
            Toast.makeText(this, getApplicationContext().getString(R.string.invalid_pass), Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString())
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) createUser();
                    else {
                        progressAlertDialog.dismiss();
                        String errorMessage;
                        try {
                            errorMessage = task.getException().getLocalizedMessage();
                        }
                        catch (NullPointerException e) {
                            errorMessage = "Error description is not available.";
                        }
                        new AlertDialog.Builder(SignUpActivity.this)
                                .setTitle(getString(R.string.error))
                                .setMessage(errorMessage)
                                .show();
                    }
                });
    }

    private void createUser() {
        User user = new User(email.getText().toString().trim());
        database = FirebaseDatabase.getInstance();
        DatabaseReference dbRef =database.getReference("users/" + mAuth.getCurrentUser().getUid());
        dbRef.setValue(user).addOnSuccessListener(unused -> {
            askForLocationPermission();
        });
    }

    private void askForLocationPermission() {
        if (ActivityCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SignUpActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_LOC_CODE);
        } else {
            setResult(Activity.RESULT_OK);
            SignInActivity.thisActivity.finish();
            startActivity(new Intent(SignUpActivity.this, NavMenu.class));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOC_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            setResult(Activity.RESULT_OK);
            SignInActivity.thisActivity.finish();
            startActivity(new Intent(SignUpActivity.this, NavMenu.class));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuth.getCurrentUser() != null) finish();
    }
}