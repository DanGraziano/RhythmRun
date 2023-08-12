package edu.northeastern.rhythmrun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class CreateAccount extends AppCompatActivity {

	private TextInputEditText inputEmail, inputFirstName, inputLastName, inputAge, weightInput, inputHeight, inputPassword;
	private TextInputLayout emailInputLayout, firstNameInputLayout, lastNameInputLayout, ageInputLayout,heightInputLayout, weightInputLayout, passwordInputLayout;
	private Button createAccountBtn, logInBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_account);

		// input fields
		inputEmail = findViewById(R.id.inputEmail);
		inputFirstName = findViewById(R.id.inputFirstName);
		inputPassword = findViewById(R.id.inputPassword);
		inputLastName = findViewById(R.id.inputLastName);
		inputAge = findViewById(R.id.inputAge);
		weightInput = findViewById(R.id.weightInput);
		inputHeight = findViewById(R.id.inputHeight);
		// input layouts
		emailInputLayout = findViewById(R.id.emailInputLayout);
		firstNameInputLayout = findViewById(R.id.firstNameInputLayout);
		passwordInputLayout = findViewById(R.id.passwordInputLayout);
		lastNameInputLayout = findViewById(R.id.lastNameInputLayout);
		ageInputLayout = findViewById(R.id.ageInputLayout);
		heightInputLayout = findViewById(R.id.heightInputLayout);
		weightInputLayout = findViewById(R.id.weightInputLayout);

		createAccountBtn = findViewById(R.id.createAccountBtn);
		logInBtn = findViewById(R.id.logInBtn);

		logInBtn.setOnClickListener(v-> startActivity(new Intent(CreateAccount.this, Login.class)));
		createAccountBtn.setOnClickListener(v -> checkUserInputs());

		passwordInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				togglePasswordVisibility(inputPassword, passwordInputLayout);
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

		String email = inputEmail.getText().toString();
		String firstName = inputFirstName.getText().toString();
		String password = inputPassword.getText().toString();
		String lastname = inputLastName.getText().toString();
		String age = inputAge.getText().toString();
		String weight = weightInput.getText().toString();
		String height = inputHeight.getText().toString();


		if (TextUtils.isEmpty(email)) {
			// Handle empty email
			Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show();
			emailInputLayout.setError("Email can not be null");
			emailInputLayout.requestFocus();

		} else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
			Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show();
			emailInputLayout.setError( " Valid email is Required");
			emailInputLayout.requestFocus();
		}

		else if (TextUtils.isEmpty(firstName)) {
			// Handle empty first name
			Toast.makeText(this, "Please enter your first name", Toast.LENGTH_SHORT).show();
			firstNameInputLayout.setError( "First Name is Required");
			firstNameInputLayout.requestFocus();
		}

		else if (TextUtils.isEmpty(password)) {
			// Handle empty password
			Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
			passwordInputLayout.setError("Password is Required");
			passwordInputLayout.requestFocus();
		}
		else if (TextUtils.isEmpty(age)) {
			// Handle empty age
			Toast.makeText(this, "Please enter your age", Toast.LENGTH_SHORT).show();
			passwordInputLayout.setError("Age is Required");
			passwordInputLayout.requestFocus();
		}
		else if (TextUtils.isEmpty(weight)) {
			// Handle empty age
			Toast.makeText(this, "Please enter your weight", Toast.LENGTH_SHORT).show();
			passwordInputLayout.setError("Weight is Required");
			passwordInputLayout.requestFocus();
		}

		else if (TextUtils.isEmpty(height)) {
			// Handle empty age
			Toast.makeText(this, "Please enter your height", Toast.LENGTH_SHORT).show();
			passwordInputLayout.setError("Height is Required");
			passwordInputLayout.requestFocus();
		}

		else {
			registerUser(firstName, lastname, age, height, weight, email, password);
		}

	}

	private void registerUser(String firstName, String lastname, String age, String height, String weight, String email, String password) {
		FirebaseAuth auth = FirebaseAuth.getInstance();
		// creates authorization instance in firebase
		auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				if (task.isSuccessful()) {

					// current currentUser
					FirebaseUser currentUser = auth.getCurrentUser();

					UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(firstName).build();
					currentUser.updateProfile(profileChangeRequest);

					// Create currentUser in Realtime DB
					UserProfile userProfile = new UserProfile(firstName,lastname,age,height,weight,email,password);

					DatabaseReference allUsersRegistered = FirebaseDatabase.getInstance().getReference("Users");

					// creates user in DB
					allUsersRegistered.child(currentUser.getUid()).setValue(userProfile).addOnCompleteListener(new OnCompleteListener<Void>() {
						@Override
						public void onComplete(@NonNull Task<Void> task) {

							if(task.isSuccessful()) {

								// For debugging delete after testing
								Toast.makeText(CreateAccount.this, "User created check email ", Toast.LENGTH_LONG).show();

								// Send verify email
								currentUser.sendEmailVerification();

								// Prevent currentUser from clicking back after successful registration
								Intent intent = new Intent(CreateAccount.this, Login.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
										| Intent.FLAG_ACTIVITY_NEW_TASK);

								// start activity
								startActivity(intent);

								// close activity
								finish();

							} else {
								Toast.makeText(CreateAccount.this, "Failed to create user ", Toast.LENGTH_LONG).show();
							}


						}
					});


				} else {
					try {
						throw task.getException();
					} catch (FirebaseAuthInvalidCredentialsException e){
						emailInputLayout.setError("Email is invalid or already in use. Try Again!");
						emailInputLayout.requestFocus();
					} catch (Exception e) {
						Log.e("ERROR", e.getMessage());
					}
				}
			}
		});
	}


}