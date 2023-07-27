package edu.northeastern.rhythmrun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;



public class StartWorkout extends AppCompatActivity {

	private static final int RECORD_REQUEST_CODE = 101;
	FusedLocationProviderClient fusedLocationProviderClient;
	LocationRequest locationRequest;

	private LocationCallback locationCallBack;

	Location startingLocation = new Location("");




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_workout);

		//Location Services Client
		fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

		//Create locationRequest settings
		locationRequest = LocationRequest.create()
				.setInterval(10000)
				.setFastestInterval(5000)
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setMaxWaitTime(15000);

		//Callback for requestLocationUpdates
		locationCallBack = new LocationCallback() {
			@Override
			public void onLocationResult(@NonNull LocationResult locationResult) {
				super.onLocationResult(locationResult);

			}
		};

		//Buttons and OnClickListeners
		// KEEP -- Used for floating action button on click
		FloatingActionButton fabButton = findViewById(R.id.startWorkoutFab);
		fabButton.setOnClickListener(v -> {
			// Open the Workout activity when FAB is clicked
			Intent intent = new Intent(StartWorkout.this, StartWorkout.class);
			startActivity(intent);
		});

		// KEEP -- Used for bottom navigation bar on click
		BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
		bottomNavigationView.setOnItemSelectedListener(
				(item) -> {
					// Open Home screen on click
					if (item.getItemId() == R.id.homeNavBar) {
						Intent homeIntent = new Intent(StartWorkout.this, Home.class);
						startActivity(homeIntent);
					}
					// Open Setting screen on click
					else if (item.getItemId() == R.id.settingsNavBar) {
						Intent settingsIntent = new Intent(StartWorkout.this, Settings.class);
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

