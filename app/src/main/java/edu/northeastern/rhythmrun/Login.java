package edu.northeastern.rhythmrun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class Login extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private TextInputEditText emailInput, passwordInput;

    private Button forgotPwBtn;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = authProfile.getCurrentUser();

        if(currentUser != null){
            boolean emailVerified = checkIfEmailVerified(currentUser);
            if(emailVerified) {
                Log.d("CURRENT USER ", "logged in on start");
                Intent intent = new Intent(Login.this, Home.class);
                startActivity(intent);
            }
        } else {
            Log.d("CURRENT USER ", " NOT !! logged in on start");
        }
    }

    private boolean checkIfEmailVerified(FirebaseUser user) {
        boolean flag = false;
        if(!user.isEmailVerified()){
            flag = false;
        } else {
            flag = true;
        }
        return flag;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        emailInput = findViewById(R.id.inputEmailText);
        passwordInput = findViewById(R.id.inputPasswordText);
        forgotPwBtn = findViewById(R.id.forgotPwBtn);
        authProfile = FirebaseAuth.getInstance();

        // On sign in button click
        Button signInBtn = findViewById(R.id.signInBtn);
        signInBtn.setOnClickListener(v -> checkUserInputs());

        // forgot pw
        forgotPwBtn.setOnClickListener(v -> startActivity(new Intent(Login.this, ForgotPassword.class)));

        // Open sign up page from button click
        Button signUpBtn = findViewById(R.id.signUpBtn);
        signUpBtn.setOnClickListener(v -> startActivity(new Intent(Login.this, CreateAccount.class)));

        TextInputLayout passwordLayout = findViewById(R.id.inputPasswordLayout);
        passwordLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePasswordVisibility(passwordInput, passwordLayout);
            }
        });
    }

    private void togglePasswordVisibility(TextInputEditText passwordEditText, TextInputLayout passwordLayout) {
        if (passwordEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            // Show password
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordLayout.setEndIconDrawable(R.drawable.ic_hide_password);

        }
        else {
            // Hide password
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordLayout.setEndIconDrawable(R.drawable.ic_show_password);
        }
        // Move the cursor to the end of the password input field
        passwordEditText.setSelection(Objects.requireNonNull(passwordEditText.getText()).length());
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
                if (task.isSuccessful()) {
                    FirebaseUser user = authProfile.getCurrentUser();

                    if (user.isEmailVerified()) {
                        // Get a reference to the "Runs" node for the current user
                        DatabaseReference runsRef = FirebaseDatabase.getInstance().getReference("Users")
                                .child(user.getUid())
                                .child("Runs");

                        // Check if the "Runs" node exists for the current user
                        runsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    // The "Runs" node does not exist, so create it
                                    runsRef.setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // "Runs" node is created successfully
                                                // Go to the home screen
                                                Intent intent = new Intent(Login.this, Home.class);
                                                startActivity(intent);
                                                finish(); // Close the login activity to prevent going back
                                            } else {
                                                // Handle the error
                                                Toast.makeText(Login.this, "Failed to create the 'Runs' node", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    // The "Runs" node already exists, so go to the home screen
                                    Intent intent = new Intent(Login.this, Home.class);
                                    startActivity(intent);
                                    finish(); // Close the login activity to prevent going back
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle the database error if any
                                Toast.makeText(Login.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Email is not verified, show the alert dialog
                        user.sendEmailVerification();
                        FirebaseAuth.getInstance().signOut();
                        showAlertDialog();
                    }
                } else {
                    // Handle login failure
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        emailInput.setError("User does not exist or is no longer valid. Please sign up");
                        emailInput.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        emailInput.setError("Invalid credentials, Try again");
                        emailInput.requestFocus();
                    } catch (Exception e) {
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
