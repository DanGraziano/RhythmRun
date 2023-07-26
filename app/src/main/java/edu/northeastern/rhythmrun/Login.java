package edu.northeastern.rhythmrun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class Login extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private TextInputEditText emailInput, passwordInput;



    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = authProfile.getCurrentUser();
        if(currentUser != null){
            Log.d("CURRENT USER ", "logged in on start");
            Intent intent = new Intent(Login.this, Home.class);
            startActivity(intent);
        } else {
            Log.d("CURRENT USER ", " NOT !! logged in on start");
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

         emailInput = findViewById(R.id.inputEmailText);
         passwordInput = findViewById(R.id.inputPasswordText);

        authProfile = FirebaseAuth.getInstance();

        // On sign in button click
        Button signInBtn = findViewById(R.id.signInBtn);
        signInBtn.setOnClickListener(v -> {
            checkUserInputs();
            }
        );


        // Open sign up page from button click
        Button signUpBtn = findViewById(R.id.signUpBtn);
        signUpBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, CreateAccount.class);
            startActivity(intent);
        });
    }

    private void checkUserInputs() {

        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (TextUtils.isEmpty(email)) {
            // Handle empty email
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show();
            emailInput.setError("Email can not be null");
            emailInput.requestFocus();

        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show();
            emailInput.setError( " Valid email is Required");
            emailInput.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            // Handle empty password
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            passwordInput.setError("Password is Required");
            passwordInput.requestFocus();
        } else {
            Log.d("PASSWORD", password);
            Log.d("email", email);
            loginInUser(email, password);
        }

    }

    private void loginInUser(String email, String password) {
        authProfile.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d("CHECKING", "CHECKING");

                    // get user
                    FirebaseUser user = authProfile.getCurrentUser();

                    // check if they have verfied email

                    if(user.isEmailVerified()){
                        Toast.makeText(Login.this, "User Logged In", Toast.LENGTH_SHORT).show();
                    } else {
                        user.sendEmailVerification();
                        FirebaseAuth.getInstance().signOut();
                        showAlertDialog();
                    }

                } else {

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        emailInput.setError("User does not exist or is no longer valid. Please sign up");
                        emailInput.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        emailInput.setError("Invalid credentials, Try again");
                        emailInput.requestFocus();
                    } catch (Exception e){
                        Log.e("ERROR", e.getMessage());
                        Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }


                }
            }
        });
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
        builder.setTitle("Email not verified");
        builder.setMessage("Please verify email. You can't login without email verification");
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}

// TODO figure our how to make password visable or hidden
// TODO forgot pw link ?