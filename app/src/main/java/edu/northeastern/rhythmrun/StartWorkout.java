package edu.northeastern.rhythmrun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class StartWorkout extends AppCompatActivity implements OnMapReadyCallback {

	//Used for system permissions
	private static final int RECORD_REQUEST_CODE = 101;
	FusedLocationProviderClient fusedLocationProviderClient;
	LocationRequest locationRequest;

	private LocationCallback locationCallBack;

	Location startingLocation = new Location("");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_workout);

		//Prompts user for permissions
		getUserPermissionLocation();

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

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gmap);
		mapFragment.getMapAsync(this);
	}

	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {

	}

	//getUserPermission for location services
	private void getUserPermissionLocation(){
		//Checks current permission level
		int permission = ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION);

		//if permission is denied log and make request to ask for permission
		if (permission != PackageManager.PERMISSION_GRANTED){
			Log.d("getUserPermissionLocation","Permission Denied!");
			permissionRequest();
		}
	}

	//Prompt user for permissions
	private void permissionRequest(){
		ActivityCompat.requestPermissions(this, new String[] {
				Manifest.permission.ACCESS_FINE_LOCATION}, RECORD_REQUEST_CODE);
	}

}

