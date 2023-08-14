package edu.northeastern.rhythmrun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Location;
//import android.location.LocationRequest;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class StartWorkout extends AppCompatActivity implements OnMapReadyCallback {

	//Initialize variables
	FusedLocationProviderClient fusedLocationProviderClient;

	//Location variables
	private LocationCallback locationCallBack;
	LocationRequest locationRequest;
	Location startingLocation = new Location("");
	Location currentLocation = new Location("");
	//Stores list of coordinates
	float[] listCoordinates = new float[20];
	//Google Map API related variables
	private static GoogleMap gMap;

	private Button startBtn;

	private double currentLong, currentLat;
	private LatLng currentCoord;
	private Boolean isFirstPull = true;
	//Used for system permissions
	private static final int RECORD_REQUEST_CODE = 101;

	// Used to display GPS status to user
	private TextView gpsStatusTextView;
	private boolean isGpsReady = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start_workout);

		//Prompts user for permissions
		getUserPermissionLocation();

		// Initialize TextView for GPS status
		gpsStatusTextView = findViewById(R.id.gpsStatusTextView);

		//Location Services Client
		fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

		//Start GPS services
		startTracking();

		//Create locationRequest settings
		locationRequest = LocationRequest.create()
				.setInterval(10000)
				.setFastestInterval(5000)
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
				.setMaxWaitTime(15000);

		//Callback for requestLocationUpdates
		locationCallBack = new LocationCallback() {
			//Location callback consistently checks for position and updates the mapFragement once
			@Override
			public void onLocationResult(@NonNull LocationResult locationResult) {
				super.onLocationResult(locationResult);
				getCoordinates(locationResult.getLastLocation());
				if (isFirstPull){
					setStartingLocation(currentCoord);
					isFirstPull = false;

					// Update TextView to show GPS signal is ready and change color from red to green
					gpsStatusTextView.setText("GPS signal ready");
					gpsStatusTextView.setBackgroundColor(ContextCompat.getColor(StartWorkout.this, R.color.gps_status_ready));
					isGpsReady = true;
				}
			}
		};

		//Start Location callback
		createLocationRequest();

		//Google Maps Fragment
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.gmap);
		mapFragment.getMapAsync(this);

		//Buttons and OnClickListeners

		startBtn = findViewById(R.id.startRunBtn);

		startBtn.setOnClickListener(v -> {startActiveWorkout();});

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

	//GoogleMap Override that updates the map
	@Override
	public void onMapReady(@NonNull GoogleMap googleMap) {
		gMap = googleMap;
		LatLng currentCoord = new LatLng(currentLat,currentLong);
		googleMap.getUiSettings().setZoomControlsEnabled(true);
		Log.d("onMapReady",String.valueOf(currentCoord));
		gMap.moveCamera(CameraUpdateFactory.newLatLng(currentCoord));
	}

	//Creates intent for ActiveWorkout
	private void startActiveWorkout(){
		if (isGpsReady) {
			Intent intent = new Intent(this, ActiveWorkout.class);
			startActivity(intent);
		} else {
			// Popup to let user know that starting workout before GPS is locked will result in inaccurate data.
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("GPS Signal Not Ready")
					.setMessage("The GPS signal is not yet locked. Starting the workout without a strong signal may result in less accurate tracking.")
					.setPositiveButton("Start Workout", (dialog, which) -> {
						Intent intent = new Intent(this, ActiveWorkout.class);
						startActivity(intent);
					})
					.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
					.show();
		}
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

	//Starts location and gets last known position of the user
	@SuppressLint("MissingPermission")
	private void startTracking(){
		fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
			@Override
			public void onSuccess(Location location) {
				if (location != null){
					//do something
				}
				else {
					Log.d("StartTracking Function", "Could not retrieve last location");
				}
			}
		});
	}

	//Create location requests
	protected void createLocationRequest() {
		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

		SettingsClient client = LocationServices.getSettingsClient(this);
		Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

		task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
			@Override
			public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
				startGPSUpdates();
			}
		});
	}

	//Starts GPS callback which will consistently update the position of the user.
	@SuppressLint("MissingPermission")
	private void startGPSUpdates(){
		fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallBack,null);
	}

	// attains the coordinates and sets the activity level variables accordingly
	private void getCoordinates(Location location){
		currentLong = location.getLongitude();
		currentLat = location.getLatitude();
		currentCoord = new LatLng(currentLat,currentLong);
	}

	//Updates the map to the current location and adds a marker at set position
	private void setStartingLocation(LatLng coordinates){
		gMap.addMarker(new MarkerOptions().position(coordinates).title("Start"));
		gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates,18));
	}

}

