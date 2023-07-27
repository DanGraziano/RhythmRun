package edu.northeastern.rhythmrun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Home extends AppCompatActivity {

	ArrayList<RunModel> runsList = new ArrayList<>();
	RecyclerView recyclerView;
	ImageView profileImage;
	FloatingActionButton fabButton;
	BottomNavigationView bottomNavigationView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		RecyclerView recyclerView = findViewById(R.id.runHistoryRecy);
		profileImage = findViewById(R.id.profileImage);

		// set up run history history.
		setupRunModels();

		FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
		if (currentUser != null) {
			// User is signed in
			String uid = currentUser.getUid(); // Get the user's unique ID
			String displayName = currentUser.getDisplayName(); // Get the user's display name
			String email = currentUser.getEmail(); // Get the user's email address
			// You can access other user information as needed

			Log.d("HomeActivity", "User ID: " + uid);
			Log.d("HomeActivity", "Display Name: " + displayName);
			Log.d("HomeActivity", "Email: " + email);
		} else {
			// User is not signed in
			// Redirect the user to the sign-in page or handle the situation accordingly
		}


		// KEEP -- Used for floating action button on click
		fabButton = findViewById(R.id.startWorkoutFab);
		fabButton.setOnClickListener(v -> {
			// Open the Workout activity when FAB is clicked
			Intent intent = new Intent(Home.this, StartWorkout.class);
			startActivity(intent);
		});


		// KEEP -- Used for bottom navigation bar on click
		bottomNavigationView = findViewById(R.id.bottomNavigationView);
		setupNavBar();

	}

	private void setupNavBar(){
		bottomNavigationView.setOnItemSelectedListener(
				(item) -> {
					// Open Home screen on click
					if (item.getItemId() == R.id.homeNavBar) {
						Intent homeIntent = new Intent(Home.this, Home.class);
						startActivity(homeIntent);
					}
					// Open Setting screen on click
					else if (item.getItemId() == R.id.settingsNavBar) {
						Intent settingsIntent = new Intent(Home.this, Settings.class);
						startActivity(settingsIntent);
					}
					return false;
				}
		);
		// KEEP -- Used for bottom navigation bar appearance and functionality
		bottomNavigationView.setBackground(null); // Set background to null to make it transparent
		bottomNavigationView.getMenu().getItem(1).setEnabled(false); // Disable the second menu item
	}

	private void setupRunModels() {
		DatabaseReference runsRef = FirebaseDatabase.getInstance().getReference("Runs");
		runsRef.orderByChild("date") // Order the runs by date
				.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
						if (dataSnapshot.exists()) {
							for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
								Object dateValue = snapshot.child("date").getValue();
								Object distanceValue = snapshot.child("distance").getValue();
								Object cadenceTimeValue = snapshot.child("cadenceTime").getValue();
								Object paceValue = snapshot.child("pace").getValue();
								Object caloriesValue = snapshot.child("calories").getValue();
								Object timeValue = snapshot.child("time").getValue();


								String date = dateValue != null ? dateValue.toString() : "";
								String distance = distanceValue != null ? distanceValue.toString() : "";
								String cadenceTime = cadenceTimeValue != null ? cadenceTimeValue.toString() : "";
								String pace = paceValue != null ? paceValue.toString() : "";
								String calories = caloriesValue != null ? caloriesValue.toString() : "";
								String time = timeValue != null ? timeValue.toString() : "";


								// Add the RunModel to the runsList
								RunModel run = new RunModel(date, distance, cadenceTime, pace, calories, time);
								runsList.add(run);
							}

							// After fetching the data, set up the RecyclerView
							RecyclerView recyclerView = findViewById(R.id.runHistoryRecy);
							recyclerView.setLayoutManager(new LinearLayoutManager(Home.this));
							RunAdapter runAdapter = new RunAdapter(runsList);
							recyclerView.setAdapter(runAdapter);
						} else {
							// Handle the case when there is no data in the "Runs" node
						}
					}

					@Override
					public void onCancelled(@NonNull DatabaseError databaseError) {
						// Handle any errors that occurred during the database operation
					}
				});
	}



}