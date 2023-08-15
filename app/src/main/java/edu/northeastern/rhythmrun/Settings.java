package edu.northeastern.rhythmrun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class Settings extends AppCompatActivity {


	private Button logout, editProfileBtn;
	private BottomNavigationView bottomNavigationView;

	private TextView userName,textEmail,textFirstName,textLastName,textAge,textWeight,
			textHeight,textCadence;

	FirebaseUser currentUser;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		// views
		logout = findViewById(R.id.logoutBtn);
		editProfileBtn = findViewById(R.id.editProfileBtn);
		FloatingActionButton fabButton = findViewById(R.id.startWorkoutFab);
		bottomNavigationView = findViewById(R.id.bottomNavigationView);
		userName = findViewById(R.id.userName);
		textEmail = findViewById(R.id.textEmail);
		textFirstName = findViewById(R.id.textFirstName);
		textLastName = findViewById(R.id.textLastName);
		textAge = findViewById(R.id.textAge);
		textWeight = findViewById(R.id.textWeight);
		textHeight = findViewById(R.id.textHeight);
		textCadence = findViewById(R.id.textCadence);

		// logged in user
		currentUser = FirebaseAuth.getInstance().getCurrentUser();

		if (currentUser != null) {
			// User is signed in
			String uid = currentUser.getUid(); // Get the user's unique ID
			String displayName = currentUser.getDisplayName(); // Get the user's display name
			userName.setText(displayName);
			// for when info changes regarding a user
			displayUsersData(currentUser);
		}

		logout.setOnClickListener(v -> {
			FirebaseAuth.getInstance().signOut();
			startActivity(new Intent(Settings.this, Login.class));
		});

		editProfileBtn.setOnClickListener(v-> startActivity(new Intent(Settings.this,EditProfile.class)));


		fabButton.setOnClickListener(v -> {startActivity(new Intent(Settings.this, StartWorkout.class));
		});

		setupNavBar();
	}

	private void setupNavBar(){
		bottomNavigationView.setOnItemSelectedListener(
				(item) -> {
					// Open Home screen on click
					if (item.getItemId() == R.id.homeNavBar) {
						Intent homeIntent = new Intent(Settings.this, Home.class);
						startActivity(homeIntent);
					}
//					// Open Setting screen on click
//					else if (item.getItemId() == R.id.settingsNavBar) {
//						Intent settingsIntent = new Intent(Settings.this, Settings.class);
//						startActivity(settingsIntent);
//					}
					return false;
				}
		);
		// KEEP -- Used for bottom navigation bar appearance and functionality
		bottomNavigationView.setBackground(null); // Set background to null to make it transparent
		bottomNavigationView.getMenu().getItem(1).setEnabled(false); // Disable the second menu item
	}


	private void displayUsersData(FirebaseUser user) {

		String userId = user.getUid();
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
		reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				Log.d("Inside run snapshot", String.valueOf(snapshot));
				String age = String.valueOf(snapshot.child("age").getValue());
				String goal = String.valueOf(snapshot.child("cadenceGoal").getValue());
				String email = String.valueOf(snapshot.child("email").getValue());
				String firstName = String.valueOf(snapshot.child("firstname").getValue());
				String lastName = String.valueOf(snapshot.child("lastname").getValue());
				String height = String.valueOf(snapshot.child("height").getValue());
				String weight = String.valueOf(snapshot.child("weight").getValue());
				textAge.setText(age);
				textCadence.setText(goal);
				textEmail.setText(email);
				textHeight.setText(height);
				textWeight.setText(weight);
				textFirstName.setText(firstName);
				textLastName.setText(lastName);

			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {

			}
		});
	}


}