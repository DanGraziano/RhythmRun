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
import java.util.Collections;
import java.util.HashMap;

public class Home extends AppCompatActivity {

	ArrayList<RunModel> runsList = new ArrayList<>();
	RecyclerView recyclerView;
	ImageView profileImage, oneMileRun, threeMileRun, fiveKRun, tenKRun, fityMileRun;
	FloatingActionButton fabButton;
	BottomNavigationView bottomNavigationView;
	TextView username;
	FirebaseUser currentUser;

	// TODO Make Recycler clickable and new activity
	// TODO Make nicer color and text the group will need to decide that
	// TODO maybe a on hoover listener for badges to give user context
	// TODO maybe add a loading spinner to make transitions nicer

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		username = findViewById(R.id.userName);
		recyclerView = findViewById(R.id.runHistoryRecy);
		profileImage = findViewById(R.id.profileImage);
		oneMileRun = findViewById(R.id.oneMileBadge);
		threeMileRun = findViewById(R.id.threeMileBadge);
		fiveKRun = findViewById(R.id.fiveKBadge);
		tenKRun = findViewById(R.id.tenKBadge);
		fityMileRun = findViewById(R.id.fiftyMileBadge);


		profileImage.setOnClickListener(v-> new Intent(Home.this, ChangeProfilePicture.class));

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
		fabButton.setOnClickListener(v -> new Intent(Home.this, StartWorkout.class));

		// KEEP -- Used for bottom navigation bar on click
		bottomNavigationView = findViewById(R.id.bottomNavigationView);
		setupNavBar();

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

	public void checkAndSetBadgeVisibilityOneMile(String distance) {
		try {
			double distanceInMiles = Double.parseDouble(distance);
			if (distanceInMiles >=1 && distanceInMiles < 3 ) {
				oneMileRun.setAlpha(1.0f);
			}
		} catch (NumberFormatException e) {
			oneMileRun.setAlpha(0.15f);
		}
	}

	public void checkAndSetBadgeVisibilityThreeMile(String distance) {
		try {
			double distanceInMiles = Double.parseDouble(distance);
			if (distanceInMiles >= 3.0 && distanceInMiles < 3.1) {
				threeMileRun.setAlpha(1.0f);
			}
		} catch (NumberFormatException e) {
			threeMileRun.setAlpha(0.15f);
		}
	}

	public void checkAndSetBadgeVisibility5K(String distance) {
		try {
			double distanceInMiles = Double.parseDouble(distance);
			if (distanceInMiles >= 3.1 && distanceInMiles < 6.2) {
				fiveKRun.setAlpha(1.0f);
			}
		} catch (NumberFormatException e) {
			fiveKRun.setAlpha(0.15f);
		}
	}

	public void checkAndSetBadgeVisibility10K(String distance) {
		try {
			double distanceInMiles = Double.parseDouble(distance);
			if (distanceInMiles >= 6.2 && distanceInMiles < 50) {
				tenKRun.setAlpha(1.0f);
			}
		} catch (NumberFormatException e) {
			tenKRun.setAlpha(0.15f);
		}
	}

	public void checkAndSetBadgeVisibility50K(String distance) {
		try {
			double distanceInMiles = Double.parseDouble(distance);
			if (distanceInMiles >= 50) {
				fityMileRun.setAlpha(1.0f);
			}
		} catch (NumberFormatException e) {
			fityMileRun.setAlpha(0.15f);
		}
	}


	public void checkBadgeSystem(String distance) {
		checkAndSetBadgeVisibilityOneMile(distance);
		checkAndSetBadgeVisibilityThreeMile(distance);
		checkAndSetBadgeVisibility5K(distance);
		checkAndSetBadgeVisibility10K(distance);
		checkAndSetBadgeVisibility50K(distance);
	}

	private void setupRunModels() {
		if (currentUser == null) {
			// User is not logged in, handle the case appropriately
			return;
		}

		String userUid = currentUser.getUid();
		DatabaseReference userRunsRef = FirebaseDatabase.getInstance().getReference("Users")
				.child(userUid)
				.child("Runs");

		// Order runs by date
		Query userRunsQuery = userRunsRef.orderByChild("date");

		userRunsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				// Clear the list before adding new items to avoid duplication
				runsList.clear();

				for (DataSnapshot runSnapshot : dataSnapshot.getChildren()) {
					// Fetch the run details from the snapshot
					String date = String.valueOf(runSnapshot.child("date").getValue());
					String distance = String.valueOf(runSnapshot.child("distance").getValue());
					checkBadgeSystem(distance);
					String cadenceTime = String.valueOf(runSnapshot.child("cadenceTime").getValue());
					String pace = String.valueOf(runSnapshot.child("pace").getValue());
					String time = String.valueOf(runSnapshot.child("time").getValue());

					// Add the RunModel to the runsList
					RunModel run = new RunModel(date, distance, cadenceTime, pace, time);
					runsList.add(run);
				}

				// Reverse the list to display most recent run is first (by date)
				Collections.reverse(runsList);

				// Update the RecyclerView after fetching all runs
				if (runsList.size() == dataSnapshot.getChildrenCount()) {
					RecyclerView recyclerView = findViewById(R.id.runHistoryRecy);
					recyclerView.setLayoutManager(new LinearLayoutManager(Home.this));
					RunAdapter runAdapter = new RunAdapter(runsList);
					recyclerView.setAdapter(runAdapter);
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				// Handle errors if needed
			}
		});
	}
}