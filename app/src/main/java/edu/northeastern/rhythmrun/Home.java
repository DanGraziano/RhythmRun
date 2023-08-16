package edu.northeastern.rhythmrun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

public class Home extends AppCompatActivity {

	ArrayList<RunModel> runsList = new ArrayList<>();
	RecyclerView recyclerView;
	ImageView profileImage, oneMileRun, halfMarathonRun, fiveKRun, tenKRun, fiftyMileRun, signUpBadge;
	FloatingActionButton fabButton;
	BottomNavigationView bottomNavigationView;
	TextView username;
	FirebaseUser currentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		username = findViewById(R.id.userName);
		recyclerView = findViewById(R.id.runHistoryRecy);
		profileImage = findViewById(R.id.profileImage);
		oneMileRun = findViewById(R.id.oneMileBadge);
		halfMarathonRun = findViewById(R.id.halfMarathonBadge);
		fiveKRun = findViewById(R.id.fiveKBadge);
		tenKRun = findViewById(R.id.tenKBadge);
		fiftyMileRun = findViewById(R.id.fiftyMileBadge);
		signUpBadge = findViewById(R.id.signingUpBadge);

		signUpBadge.setOnClickListener(v -> showTooltip(v, getString(R.string.sign_up_badge_tooltip)));
		oneMileRun.setOnClickListener(v -> showTooltip(v, getString(R.string.one_mile_badge_tooltip)));
		fiveKRun.setOnClickListener(v -> showTooltip(v, getString(R.string.five_k_badge_tooltip)));
		tenKRun.setOnClickListener(v -> showTooltip(v, getString(R.string.ten_k_badge_tooltip)));
		halfMarathonRun.setOnClickListener(v -> showTooltip(v, getString(R.string.half_marathon_badge_tooltip)));
		fiftyMileRun.setOnClickListener(v -> showTooltip(v, getString(R.string.fifty_mile_badge_tooltip)));

		profileImage.setOnClickListener(v -> {
			Intent intent = new Intent(Home.this, ChangeProfilePicture.class);
			startActivity(intent);
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
			Intent intent = new Intent(Home.this, StartWorkout.class);
			startActivity(intent);
		});
		fabButton.setOnClickListener(v -> startActivity(new Intent(Home.this, StartWorkout.class)));

		// KEEP -- Used for bottom navigation bar on click
		bottomNavigationView = findViewById(R.id.bottomNavigationView);
		setupNavBar();

	}

	private void showTooltip(View anchorView, String tooltipText) {
		TooltipCompat.setTooltipText(anchorView, tooltipText);
	}

	private void showUserProfile(FirebaseUser firebaseUser){
		String userId = firebaseUser.getUid();
		DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
		reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot snapshot) {
				UserProfile user = snapshot.getValue(UserProfile.class);

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
			if (distanceInMiles >=1 && distanceInMiles < 3) {
				oneMileRun.setAlpha(1.0f);
			}
		} catch (NumberFormatException e) {
			oneMileRun.setAlpha(0.15f);
		}
	}

	public void checkAndSetBadgeVisibilityHalfMarathon(String distance) {
		try {
			double distanceInMiles = Double.parseDouble(distance);
			if (distanceInMiles >= 13.1 && distanceInMiles < 50) {
				halfMarathonRun.setAlpha(1.0f);
			}
		} catch (NumberFormatException e) {
			halfMarathonRun.setAlpha(0.15f);
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
			if (distanceInMiles >= 6.2 && distanceInMiles < 13.1) {
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
				fiftyMileRun.setAlpha(1.0f);
			}
		} catch (NumberFormatException e) {
			fiftyMileRun.setAlpha(0.15f);
		}
	}


	public void checkBadgeSystem(String distance) {
		checkAndSetBadgeVisibilityOneMile(distance);
		checkAndSetBadgeVisibilityHalfMarathon(distance);
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
					String avgCadence = String.valueOf(runSnapshot.child("avgCadence").getValue());
					String avgPace = String.valueOf(runSnapshot.child("avgPace").getValue());
					String time = String.valueOf(runSnapshot.child("time").getValue());

					// Add the RunModel to the runsList
					RunModel run = new RunModel();
					run.setRunId(runSnapshot.getKey());  // Set the run ID
					run.setDate(date);
					run.setDistance(distance);
					run.setAvgCadence(avgCadence);
					run.setAvgPace(avgPace);
					run.setTime(time);

					runsList.add(run);
				}

				// Reverse the list to display most recent run first (order by date)
				Collections.reverse(runsList);

				// Update the RecyclerView after fetching all runs
				if (runsList.size() == dataSnapshot.getChildrenCount()) {
					RecyclerView recyclerView = findViewById(R.id.runHistoryRecy);
					recyclerView.setLayoutManager(new LinearLayoutManager(Home.this));
					RunAdapter runAdapter = new RunAdapter(runsList, recyclerView);
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