package edu.northeastern.rhythmrun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PostWorkout extends AppCompatActivity {
	private TextView distanceData;
	private TextView cadenceData;
	private TextView timeData;
	private TextView paceData;
	private TextView dateData;
	private ImageView mapImage;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post_workout);

		// Initialize your TextViews
		distanceData = findViewById(R.id.distanceData);
		cadenceData = findViewById(R.id.cadenceData);
		dateData = findViewById(R.id.dateData);
		paceData = findViewById(R.id.paceData);
		timeData = findViewById(R.id.timeData);
		mapImage = findViewById(R.id.mapImageView);


		FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

		if (currentUser != null) {
			String userUid = currentUser.getUid();
			DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users")
					.child(userUid)
					.child("Runs");

			// Retrieve the run ID from the intent
			String runId = getIntent().getStringExtra("runId");

			// Retrieve the specific run data using the run ID
			DatabaseReference specificRunRef = databaseReference.child(runId);

			specificRunRef.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					if (dataSnapshot.exists()) {
						// Get data from DB
						String date = String.valueOf(dataSnapshot.child("date").getValue());
						String distance = dataSnapshot.child("distance").getValue() + " mi";
						String avgCadence = dataSnapshot.child("avgCadence").getValue() + " spm";
						String time = String.valueOf(dataSnapshot.child("time").getValue());
						String avgPace = dataSnapshot.child("avgPace").getValue() + " /mi";
						String mapImageUrl = String.valueOf(dataSnapshot.child("mapImageUrl").getValue());

						// Add data from DB to TextViews
						distanceData.setText(distance);
						cadenceData.setText(avgCadence);
						timeData.setText(time);
						paceData.setText(avgPace);
						dateData.setText(date);

						// Load map image using Picasso
						Picasso.get()
								.load(mapImageUrl)
								.into(mapImage);
					}

				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {
					// Handle errors if needed
				}
			});
		}


		// KEEP -- Used for floating action button on click
		FloatingActionButton fabButton = findViewById(R.id.startWorkoutFab);
		fabButton.setOnClickListener(v -> {
			// Open the Workout activity when FAB is clicked
			Intent intent = new Intent(PostWorkout.this, StartWorkout.class);
			startActivity(intent);
		});


		// KEEP -- Used for bottom navigation bar on click
		BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
		bottomNavigationView.setOnItemSelectedListener(
				(item) -> {
					// Open Home screen on click
					if (item.getItemId() == R.id.homeNavBar) {
						Intent homeIntent = new Intent(PostWorkout.this, Home.class);
						startActivity(homeIntent);
					}
					// Open Setting screen on click
					else if (item.getItemId() == R.id.settingsNavBar) {
						Intent settingsIntent = new Intent(PostWorkout.this, Settings.class);
						startActivity(settingsIntent);
					}
					return false;
				}
		);
		// KEEP -- Used for bottom navigation bar appearance and functionality
		bottomNavigationView.setBackground(null); // Set background to null to make it transparent
		bottomNavigationView.getMenu().getItem(1).setEnabled(false); // Disable the second menu item
	}
}