package edu.northeastern.rhythmrun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class Home extends AppCompatActivity {

	ArrayList<RunModel> runsList = new ArrayList<>();
	RecyclerView recyclerView;
	ImageView profileImage;
	FloatingActionButton fabButton;
	BottomNavigationView bottomNavigationView;
	TextView username;
	FirebaseUser currentUser;

	// TODO Make Recycler clickable and new activity
	// TODO Change Runs DB and create another collection mapping table
	// TODO Achievements? OR Filters for run

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		username = findViewById(R.id.userName);
		RecyclerView recyclerView = findViewById(R.id.runHistoryRecy);
		profileImage = findViewById(R.id.profileImage);

		profileImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Home.this, ChangeProfilePicture.class);
				startActivity(intent);
			}
		});


		 currentUser = FirebaseAuth.getInstance().getCurrentUser();
		if (currentUser != null) {
			// User is signed in
			String uid = currentUser.getUid(); // Get the user's unique ID
			String displayName = currentUser.getDisplayName(); // Get the user's display name
			username.setText(displayName);
			// for when info changes regarding a user
			showUserProfile(currentUser);
		}

		// set up run history history.
		setupRunModels();

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

		// testing the db
		//printUserKeys();


	}

	private void showUserProfile(FirebaseUser firebaseUser){
		String userId = firebaseUser.getUid();

		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
		reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				CreateUserInDB user = snapshot.getValue(CreateUserInDB.class);

				if(user != null){
					// external picture uses Picasso
					Uri uri = currentUser.getPhotoUrl();
					Picasso picasso = Picasso.get();
					picasso.load(uri)
							.placeholder(R.drawable.person_outline) // Use your placeholder image drawable here
							.error(R.drawable.person_outline) // Use your error image drawable here
							.into(profileImage);

					//picasso.load(uri).into(profileImage);



				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError error) {

			}
		});

	}


	private void printUserKeys() {

		String userUid = currentUser.getUid();

		DatabaseReference userRunsRef = FirebaseDatabase.getInstance().getReference("UserRunsMapping");

		Query userRunsQuery = userRunsRef.orderByChild("user").equalTo(userUid);

		userRunsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if (dataSnapshot.exists()) {
					for (DataSnapshot userRunSnapshot : dataSnapshot.getChildren()) {
						// Get the HashMap representing the user run data
						HashMap<String, Object> userRunData = (HashMap<String, Object>) userRunSnapshot.getValue();

						// Get the value of the "user" field (assuming "user" is a key in the HashMap)
						String userKey = (String) userRunData.get("run");
						Log.d("RUN IN PRINT ", userKey);
					}
				} else {
					Log.d("USER_KEY", "No users found in UserRunsMapping");
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				// Handle errors if needed
			}
		});
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

		if (currentUser == null) {
			// User is not logged in, handle the case appropriately
			return;
		}

		String userUid = currentUser.getUid();
		DatabaseReference userRunsRef = FirebaseDatabase.getInstance().getReference("UserRunsMapping");
		Query userRunsQuery = userRunsRef.orderByChild("user").equalTo(userUid);

		userRunsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				// Clear the list before adding new items to avoid duplication
				runsList.clear();

				if (dataSnapshot.exists()) {
					for (DataSnapshot userRunSnapshot : dataSnapshot.getChildren()) {
						HashMap<String, Object> userRunData = (HashMap<String, Object>) userRunSnapshot.getValue();

						// Get the value of the "user" field (assuming "user" is a key in the HashMap)
						String runId = (String) userRunData.get("run");

						// Query the "Runs" table to get details of the current run
						DatabaseReference runsRef = FirebaseDatabase.getInstance().getReference("Runs")
								.child(runId);
						runsRef.addListenerForSingleValueEvent(new ValueEventListener() {
							@Override
							public void onDataChange(@NonNull DataSnapshot runSnapshot) {
								if (runSnapshot.exists()) {
									// Fetch the run details from the snapshot
									Log.d("Inside run snapshot", String.valueOf(runSnapshot));
									String date = String.valueOf(runSnapshot.child("date").getValue());
									String distance = String.valueOf(runSnapshot.child("distance").getValue());
									String cadenceTime = String.valueOf(runSnapshot.child("cadenceTime").getValue());
									String pace = String.valueOf(runSnapshot.child("pace").getValue());
									String calories = String.valueOf(runSnapshot.child("calories").getValue());
									String time = String.valueOf(runSnapshot.child("time").getValue());

									// Add the RunModel to the runsList
									RunModel run = new RunModel(date, distance, cadenceTime, pace, calories, time);
									runsList.add(run);

									// Update the RecyclerView after fetching all runs
									if (runsList.size() == dataSnapshot.getChildrenCount()) {
										RecyclerView recyclerView = findViewById(R.id.runHistoryRecy);
										recyclerView.setLayoutManager(new LinearLayoutManager(Home.this));
										RunAdapter runAdapter = new RunAdapter(runsList);
										recyclerView.setAdapter(runAdapter);
									}
								}
							}

							@Override
							public void onCancelled(@NonNull DatabaseError databaseError) {
								// Handle errors if needed
							}
						});
					}
				} else {
					// Handle the case when there are no runs associated with the user
					// For example, you can show a message to the user or hide the RecyclerView
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				// Handle errors if needed
			}
		});
	}




}