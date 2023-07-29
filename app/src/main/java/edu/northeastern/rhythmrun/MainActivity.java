package edu.northeastern.rhythmrun;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextInputEditText usernameInput = findViewById(R.id.inputSignInText);
        TextInputEditText passwordInput = findViewById(R.id.inputPasswordText);

        // On sign in button click
        Button signInBtn = findViewById(R.id.signInBtn);
        signInBtn.setOnClickListener(v -> {
            String usernameText = Objects.requireNonNull(usernameInput.getText()).toString();
            String passwordText = Objects.requireNonNull(passwordInput.getText()).toString();

            // Check if username or password fields are blank
            if (TextUtils.isEmpty(usernameText) || TextUtils.isEmpty(passwordText)) {
                // Hide the keyboard to show Snackbar
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                // Display Snackbar message
                Snackbar.make(v, "All fields required to sign in", Snackbar.LENGTH_LONG).show();
            }
        });

        // TODO validate username and password from DB to allow login



        // Open sign up page from button click
        Button signUpBtn = findViewById(R.id.signUpBtn);
        signUpBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StartWorkout.class);
            startActivity(intent);
        });
    }
}
