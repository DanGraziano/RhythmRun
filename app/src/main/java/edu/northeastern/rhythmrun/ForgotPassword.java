 package edu.northeastern.rhythmrun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

 public class ForgotPassword extends AppCompatActivity {

    private Button sendPwEmailBtn;
    private EditText editTextInput;

    private FirebaseAuth userAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        sendPwEmailBtn = findViewById(R.id.sendPwEmailBtn);
        editTextInput = findViewById(R.id.editTextInput);

        sendPwEmailBtn.setOnClickListener(v -> {
            String email = editTextInput.getText().toString();

            if (TextUtils.isEmpty(email)) {
                editTextInput.setError("Email is Required");
                editTextInput.requestFocus();
            } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                editTextInput.setError("Valid email is Required");
                editTextInput.requestFocus();
            } else {
                resetPassword(email);
            }

        });

    }

     private void resetPassword(String email) {
        userAuth = FirebaseAuth.getInstance();
        userAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgotPassword.this, "Please check your email", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ForgotPassword.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });
     }
 }