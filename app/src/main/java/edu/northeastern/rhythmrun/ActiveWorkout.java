package edu.northeastern.rhythmrun;

import android.app.AlertDialog;
import android.content.Context;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Environment;
import android.os.HandlerThread;
import android.provider.Settings;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class ActiveWorkout extends AppCompatActivity implements OnMapReadyCallback, LocationListener, SensorEventListener  {

	private GoogleMap googleMap;
	private FusedLocationProviderClient fusedLocationClient;
	private LocationRequest locationRequest;
	private Location lastLocation;
	private float totalDistance = 0.0f;

	// TextViews
	private TextView currentCadenceNumber, avgPaceNumber, currentDistance, currentTime, targetSPM;
	private Button endButton, pauseButton, soundToggle;
	private Spinner SPMSelector;
	private ImageView spmGear;
	private long timePaused = 0;
	private boolean isPaused = false;
	private boolean isLoud = true;

	private boolean isRunning = false;

	// Variables for Step Detector
	private SensorManager sensorManager;
	private Sensor stepDetectorSensor;
	private int stepCount = 0;
	private long startTime = 0;

	// For runnable

	private long startTrackingTime;
	private Handler handler = new Handler();

	LocationCallback locationCallback;

	// for map tracking
	private PolylineOptions polylineOptions = new PolylineOptions().width(10).color(Color.BLUE);
	private Polyline polyline;

	// for location permission
	private static final int REQUEST_LOCATION_PERMISSION = 1001;

	// for threads
	private HandlerThread locationThread;
	private Handler locationHandler;
	private HandlerThread sensorThread;
	private Handler sensorHandler;
	private HandlerThread timerThread;
	private Handler timerHandler;

	// Metronome variables
	private float threadBPM = 0;
	private boolean metronomeOn = true;
	private Metronome metronome = new Metronome();
	private Metronome.MetronomeThread playMetronomeThread = new Metronome.MetronomeThread(100);


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_active_workout);

		// Init TextViews
		currentCadenceNumber = findViewById(R.id.currentCadenceNumber);
		avgPaceNumber = findViewById(R.id.avgPaceNumber);
		currentDistance = findViewById(R.id.currentDistance);
		currentTime = findViewById(R.id.currentTime);
		targetSPM = findViewById(R.id.targetSPMNumber);
		soundToggle = findViewById(R.id.soundToggle);
		currentDistance = findViewById(R.id.currentDistance);
		currentTime = findViewById(R.id.currentTime);
		pauseButton = findViewById(R.id.pause);
		endButton = findViewById(R.id.endButton);
		SPMSelector = findViewById(R.id.spinner);
		spmGear = findViewById(R.id.spmGear);

		// Initialize the SensorManager and Step Detector
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
		if (mapFragment != null) {
			mapFragment.getMapAsync(this);
		}

		// Init Fused Location
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

		createLocationRequest();
		createLocationCallback();

		// Set click listener for the pause button
		pauseButton.setOnClickListener(v -> {
			if (!isPaused) {
				pauseTimer();
				// Turn mettronome off
				if (metronomeOn) {
					metronomeOff();
				}
				pauseButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.resume));
				pauseButton.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.gps_status_ready));
				isPaused = true;
				soundToggle.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.sound_off));
				isLoud = false;
				Toast.makeText(ActiveWorkout.this, "Workout Paused", Toast.LENGTH_SHORT).show();
			} else {
				resumeTimer();
				setMetronomeOn();
				pauseButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.pause));
				pauseButton.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.pause));
				isPaused = false;
				soundToggle.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.sound_on));
				isLoud = true;
				Toast.makeText(ActiveWorkout.this, "Workout Resumed", Toast.LENGTH_SHORT).show();
			}
		});

		// Set click listener for the END button
		endButton.setOnClickListener(v -> {

			MediaPlayerFragment.mediaPlayer.stop();

			// Turn metronome off
			if (metronomeOn) {
				metronomeOff();
			}

			//  Stop step detector listener on end button click
			sensorManager.unregisterListener(this, stepDetectorSensor);

			// Get total distance
			String endTotalDistance = currentDistance.getText().toString();
			String totalTime = currentTime.getText().toString();

			long currentTime = System.currentTimeMillis();
			String averagePace = getAveragePaceString(Double.parseDouble(endTotalDistance), startTime, currentTime);
			//String totalTime = getTotalTimeString(startTime, currentTime);

			double averageCadence = calculateCadence(stepCount, startTime, currentTime);
			int roundedCadence = (int) Math.round(averageCadence);
			String totalAvgCadence = String.valueOf(roundedCadence);

			// Get bitmap of map
			googleMap.snapshot(bitmap -> {
				try {
					// Create a file to store the map image
					File mapFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "map.png");
					FileOutputStream outputStream = new FileOutputStream(mapFile);
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
					outputStream.flush();
					outputStream.close();

					Intent intent = new Intent(ActiveWorkout.this, RunStats.class);
					intent.putExtra("mapFilePath", mapFile.getAbsolutePath());
					intent.putExtra("distance", endTotalDistance);
					intent.putExtra("avgCadence", totalAvgCadence);
					intent.putExtra("date", getCurrentDate());
					intent.putExtra("avgPace", averagePace);
					intent.putExtra("time", totalTime);
					startActivity(intent);

				} catch (IOException e) {
					e.printStackTrace();
				}
			});

		});

		// Set click listener for the metronome sound button
		soundToggle.setOnClickListener(v -> {
			if (isLoud) {
				soundToggle.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.sound_off));
				if (metronomeOn) {
					metronomeOff();
				}
				isLoud = false;
			} else {
				soundToggle.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.sound_on));
				setMetronomeOn();
				isLoud = true;
			}
		});

		// List of available beats per minute for metronome
		ArrayList<Integer> validSPM = new ArrayList<>();
		for (Integer i = 100; i < 200; i++) {
			validSPM.add(i);
		}

		// Adapter for the spinner that contains all of the metronome frequencies
		ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<Integer>(getApplicationContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, validSPM);
		SPMSelector.setAdapter(spinnerAdapter);

		// Set click listener for the adjusting metronome sound frequency while activity is running
		spmGear.setOnClickListener(v -> {
			if (SPMSelector != null) {
				try {
					Method method = Spinner.class.getDeclaredMethod("onDetachedFromWindow");
					method.setAccessible(true);
					method.invoke(SPMSelector);
					SPMSelector.setVisibility(View.VISIBLE);
					SPMSelector.performClick();
					if (metronomeOn) {
						metronomeOff();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// Listener for the selection of the new metronome SPM frequency
		SPMSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String newValue = parent.getItemAtPosition(position).toString();
				targetSPM.setText(newValue);
				setBPM(newValue);
				SPMSelector.setVisibility(View.GONE);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});


		// Initialize threads for location, sensor, and timer
		locationThread = new HandlerThread("LocationThread");
		locationThread.start();
		locationHandler = new Handler(locationThread.getLooper());

		sensorThread = new HandlerThread("SensorThread");
		sensorThread.start();
		sensorHandler = new Handler(sensorThread.getLooper());

		timerThread = new HandlerThread("TimerThread");
		timerThread.start();
		timerHandler = new Handler(timerThread.getLooper());


		// Media player
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.mediaPlayerFrag, new MediaPlayerFragment())
					.commit();
		}
	}

	//----- END OF ON CREATE-----


	private String formatPace(double paceInMinutesPerMile) {
		int minutes = (int) paceInMinutesPerMile;
		int seconds = (int) ((paceInMinutesPerMile - minutes) * 60);
		return String.format(Locale.getDefault(),"%d:%02d", minutes, seconds);
	}

	public String getAveragePaceString(double distance, long startTime, long endTime) {
		double totalTimeInMinutes = (endTime - startTime) / (1000.0 * 60); // Convert milliseconds to minutes.
		if (distance == 0) return "0:00"; // Avoid division by zero.
		double pace = totalTimeInMinutes / distance;
		return formatPace(pace);
	}

	private double calculateCadence(int stepCount, long startTime, long endTime) {
		double totalTimeInMinutes = (endTime - startTime) / (1000.0 * 60); // Convert milliseconds to minutes.
		if (totalTimeInMinutes == 0) return 0; // Avoid division by zero.
		return stepCount / totalTimeInMinutes;
	}

	// Check for location permission
	private boolean checkLocationPermission() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// If permission is not granted, request it
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
			return false;
		}
		return true;
	}

	// Handle the result of permission requests
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_LOCATION_PERMISSION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// Permission granted, proceed with location updates or whatever you need
				onResume();
			} else {
				// Permission denied, show rationale or redirect
				handlePermissionDenied();
			}
		}
	}

	private void handlePermissionDenied() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
			// Show alert for location requirement
			new AlertDialog.Builder(this)
					.setTitle("Location Permission Required")
					.setMessage("This app requires location permissions to work properly.")
					.setPositiveButton("Grant", (dialog, which) -> {
						ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
					})
					.setNegativeButton("Deny", (dialog, which) -> {
						// Closes activity if rejected
						finish();
					})
					.create()
					.show();
		} else {
			// If user selects "Don't ask again" then send to settings
			new AlertDialog.Builder(this)
					.setTitle("Permission Denied")
					.setMessage("Please enable location permissions from app settings.")
					.setPositiveButton("Go to Settings", (dialog, which) -> {
						// Open app settings
						Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
						Uri uri = Uri.fromParts("package", getPackageName(), null);
						intent.setData(uri);
						startActivity(intent);
					})
					.setNegativeButton("Cancel", (dialog, which) -> {
						// Close activity screen on cancel
						finish();
					})
					.create()
					.show();
		}
	}


	// Get the current date
	private String getCurrentDate() {
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
		Date date = new Date();
		return dateFormat.format(date);
	}

	private void pauseTimer() {
		// Unregister step detector listener on pause
		sensorManager.unregisterListener(this, stepDetectorSensor);
		timePaused = SystemClock.uptimeMillis();
		// Stop the Runnable from updating the elapsed time
		timerHandler.removeCallbacks(updateTimeTask);
	}

	private void resumeTimer() {
		long elapsedTime = SystemClock.uptimeMillis() - timePaused;
		// Adjust the start tracking time for the elapsed time during the pause
		startTrackingTime += elapsedTime;
		// Continue the timer task to update the UI
		timerHandler.postDelayed(updateTimeTask, 1000);
		// Register the step detector listener to resume counting steps
		sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	// Check for location when app starts up and begin time and step detection
	@Override
	protected void onResume() {
		super.onResume();
		if (checkLocationPermission()) {
			fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
			sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
			startTrackingTime = System.currentTimeMillis();
			startTime = System.currentTimeMillis();
			timerHandler.postDelayed(updateTimeTask, 1000);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		fusedLocationClient.removeLocationUpdates(locationCallback);
		timerHandler.removeCallbacks(updateTimeTask);
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		sensorHandler.post(() -> {
			if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR && !isPaused) { // Ensure the workout isn't paused
				stepCount++;
				Log.d("StepDetector", "Step detected. Total steps: " + stepCount);

				// Calculate cadence (steps per minute)
				long elapsedTime = System.currentTimeMillis() - startTime;
				float cadence = (stepCount / (elapsedTime / 1000.0f)) * 60; // Steps per minute

				runOnUiThread(() -> {
							// Update cadence text view
							if (cadence < 200) {
								currentCadenceNumber.setText(String.format(Locale.getDefault(), "%.2f spm", cadence));
							}
						});
				Log.d("StepDetection", "Step Count: " + stepCount);
				Log.d("StepDetection", "Cadence Number: " + cadence);

				// Make sure that the timer task is running
				timerHandler.removeCallbacks(updateTimeTask);
				timerHandler.postDelayed(updateTimeTask, 1000);
			}
		});
	}

	private Runnable updateTimeTask = new Runnable() {
		public void run() {
			long millis = System.currentTimeMillis() - startTrackingTime;
			int totalSeconds = (int) (millis / 1000);
			int minutes = totalSeconds / 60;
			int seconds = totalSeconds % 60;

			// Final variable for lambda
			final int displayMinutes = minutes;
			final int displaySeconds = seconds;

			runOnUiThread(() -> {
				currentTime.setText(String.format(Locale.getDefault(), "%02d:%02d", displayMinutes, displaySeconds));
			});

			timerHandler.postDelayed(this, 1000);
		}
	};

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Included because of the interface
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		this.googleMap = googleMap;

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
				// Set the map's camera position to the current location of the device.
				if (location != null) {
					LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
					this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
					this.googleMap.addMarker(new MarkerOptions().position(currentLatLng).title("Start Location"));
				}
			});
		}
		// Add button to centers the map to current location
		this.googleMap.setMyLocationEnabled(true);

		// Add route tracking
		this.polyline = this.googleMap.addPolyline(polylineOptions);
	}

	// Request location
	protected void createLocationRequest() {
		locationRequest = LocationRequest.create();
		// Update every second
		locationRequest.setInterval(1000);
		// At most every 0.5 seconds
		locationRequest.setFastestInterval(500);
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	// Location call back
	protected void createLocationCallback() {
		locationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				if (locationResult == null) {
					return;
				}
				for (Location location : locationResult.getLocations()) {
					onLocationChanged(location);
				}
			}
		};
	}

	@Override
	public void onLocationChanged(Location location) {
		// Handle data calculation on background thread
		locationHandler.post(() -> {
			if (lastLocation != null) {
				float distanceForThisUpdate = lastLocation.distanceTo(location);
				totalDistance += distanceForThisUpdate;

				// Calculate pace and update avgPaceNumber
				long elapsedMillis = System.currentTimeMillis() - startTrackingTime - timePaused;
				float pace = (elapsedMillis / (1000.0f * 60.0f)) / (totalDistance / 1609.34f);  // Convert pace to miles

				// Run on main thread
				runOnUiThread(() -> {
					// Only update textview if pace is faster than 25 minutes
					if (pace <= 25 && pace >= 0) {
						int paceMinutes = (int) pace;
						int paceSeconds = Math.round((pace - paceMinutes) * 60);
						avgPaceNumber.setText(String.format(Locale.getDefault(), "%d:%02d /mi", paceMinutes, paceSeconds));
					}


					// Update distance and convert to miles
					if (totalDistance >= 0) {
						currentDistance.setText(String.format(Locale.getDefault(), "%.2f", totalDistance / 1609.34));
					}

					// Update the polyline
					LatLng newLocation = new LatLng(location.getLatitude(), location.getLongitude());
					polylineOptions.add(newLocation);
					polyline.setPoints(polylineOptions.getPoints());
				});
			}
			lastLocation = location;
		});
	}


	// Disable back press
	@Override
	public void onBackPressed() {
	}

	// Close threads on destroy
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (locationThread != null) {
			locationThread.quitSafely();
		}
		if (sensorThread != null) {
			sensorThread.quitSafely();
		}
		if (timerThread != null) {
			timerThread.quitSafely();
		}
		metronome.stop();
	}


	private void setBPM (String bpmString){
		//Convert SPM string to a float.
		threadBPM = Float.parseFloat(bpmString);
		//Set the thread bpm.
		playMetronomeThread.threadBpm = threadBPM;
		metronome.on();
		new Thread(playMetronomeThread).start();
		metronomeOn = true;

	}

	private void metronomeOff(){
		metronome.stop();
		handler.removeCallbacks(playMetronomeThread);
		metronomeOn = false;
	}

	private void setMetronomeOn(){
		metronome.on();
		new Thread(playMetronomeThread).start();
		metronomeOn = true;
	}
}


