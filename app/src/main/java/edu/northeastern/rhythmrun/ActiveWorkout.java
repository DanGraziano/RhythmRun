package edu.northeastern.rhythmrun;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActiveWorkout extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

	private TextView targetSPM;
	FusedLocationProviderClient fusedLocationProviderClient;

	//Location variables
	private LocationCallback locationCallBack;
	LocationRequest locationRequest;


	private TextView currentCadence;
	private TextView currentTime;
	private ImageView spmGear;
	private Button soundToggle;
	private TextView currentPace;
	private TextView avgPace;
	private TextView currentDistance;
	private Button end;
	private Button pause;
	private Spinner SPMSelector;

	private SensorManager sensorManager;
	private Sensor accelerometer;
	private boolean isRunning = false;
	private boolean isPaused = false;
	private boolean isLoud = true;
	private int stepCount = 0;

	private double totalDistance = 0.0;
	private Handler handler;
	private long startTime;
	private long timePaused = 0;

	private Boolean isFirstPull = true;


	//------------------------------------------------
	private GoogleMap googleMap;
	private FusedLocationProviderClient fusedLocationClient;
	private LocationCallback locationCallback;
	private Polyline routePolyline;
	private static final int REQUEST_LOCATION_PERMISSION = 1001;

	//----------------live location variables
	private LocationManager locationManager;

	//----------trailing mile and avg pace time vars an

	private long lastMileTime = 0;
	private double lastMileDistance = 0.0;
	private long totalElapsedTime = 0; // Total elapsed time in milliseconds
	private double totalPaceDistance = 0.0; // Total distance covered for calculating average pace

	//----------------------------


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_active_workout);


		targetSPM = findViewById(R.id.targetSPMNumber);
		currentCadence = findViewById(R.id.currentCadenceNumber);
		soundToggle = findViewById(R.id.soundToggle);
		currentPace = findViewById(R.id.currentPaceNumber);
		avgPace = findViewById(R.id.avgPaceNumber);
		currentDistance = findViewById(R.id.currentDistance);
		currentTime = findViewById(R.id.currentTime);
		pause = findViewById(R.id.pause);
		end = findViewById(R.id.endButton);
		SPMSelector = findViewById(R.id.spinner);
		spmGear = findViewById(R.id.spmGear);



		SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map));
		if (mapFragment != null) {
			mapFragment.getMapAsync(this);
		}
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(ActiveWorkout.this);

		locationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				if (locationResult != null) {
					// Handle new location updates here
					updateMap(locationResult.getLastLocation());
				}
			}
		};

		// Initialize the location request
		locationRequest = LocationRequest.create()
				.setInterval(5000) // Update interval in milliseconds
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		// Initialize the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


		// Starts the timer for the run
		handler = new Handler();
		startTimer();

		// Initialize sensor manager and accelerometer sensor
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


		// Set click listener for the END button
		end.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Perform actions to end the workout, e.g., save data, stop tracking, etc.
				// TODO: add stats activity screen
				//stopTracking();
				Toast.makeText(ActiveWorkout.this, "Workout Ended", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(ActiveWorkout.this, RunStats.class);
				startActivity(intent);
			}
		});

		// Set click listener for the pause button
		pause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isPaused) {
					pauseTimer();
					pause.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.resume));
					pause.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.gps_status_ready));
					isPaused = true;
					Toast.makeText(ActiveWorkout.this, "Workout Paused", Toast.LENGTH_SHORT).show();
				} else if (isPaused) {
					startTimer();
					pause.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.pause));
					pause.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.pause));
					isPaused = false;
					Toast.makeText(ActiveWorkout.this, "Workout Resumed", Toast.LENGTH_SHORT).show();
				}
			}
		});

		// Set click listener for the metronome sound button
		soundToggle.setOnClickListener(new View.OnClickListener() {
			//TODO: here is where we need to mute the sound when the metronome is integrated
			@Override
			public void onClick(View v) {
				if (isLoud) {
					soundToggle.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.sound_off));
					isLoud = false;
				} else {
					soundToggle.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.sound_on));
					isLoud = true;
				}
			}
		});

		// List of available beats per minute for metronome
		ArrayList<Integer> validSPM = new ArrayList<Integer>();
		for (Integer i = 100; i < 200; i++) {
			validSPM.add(i);
		}

		// Adapter for the spinner that contains all of the metronome frequencies
		ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<Integer>(getApplicationContext(), androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, validSPM);
		SPMSelector.setAdapter(spinnerAdapter);

		// Set click listener for the adjusting metronome sound frequency while activity is running
		spmGear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (SPMSelector != null) {
					try {
						Method method = Spinner.class.getDeclaredMethod("onDetachedFromWindow");
						method.setAccessible(true);
						method.invoke(SPMSelector);
						SPMSelector.setVisibility(View.VISIBLE);
						SPMSelector.performClick();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});


		// Listener for the selection of the new metronome SPM frequency
		SPMSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String newValue = parent.getItemAtPosition(position).toString();
				targetSPM.setText(newValue);
				SPMSelector.setVisibility(View.GONE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}

		});

		currentDistance.setText(Double.toString(totalDistance));

	}

	//GPS functions block
	//------------------------------------------------------------------------------------
	@Override
	public void onMapReady(GoogleMap googleMap) {
		this.googleMap = googleMap;
		googleMap.getUiSettings().setZoomControlsEnabled(true);

		// Start location updates
		startLocationUpdates();
	}
	

	private void updateMap(android.location.Location location) {
		if (googleMap != null) {
			LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

			if (routePolyline == null) {
				routePolyline = googleMap.addPolyline(new PolylineOptions().color(Color.BLUE));
			}

			List<LatLng> points = routePolyline.getPoints();
			points.add(currentLatLng);
			routePolyline.setPoints(points);

			if (isFirstPull) {
				googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18));
				isFirstPull = false;
			}
		}
	}


	// Override onRequestPermissionsResult to handle permission request result
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == REQUEST_LOCATION_PERMISSION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				startLocationUpdates(); // Retry starting location updates
			} else {
				// Handle case where the user denies the permission
			}
		}
	}

//------------------------------------------------------------------------------------

	//-----------------live location updates-------------

	@Override
	public void onLocationChanged(@NonNull Location location) {
		updateMap(location);
		updateDistance(location);
	}


	//	fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);


	private void startLocationUpdates() {

		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// Request the missing permissions.
			ActivityCompat.requestPermissions(this,
					new String[]{
							android.Manifest.permission.ACCESS_FINE_LOCATION,
							android.Manifest.permission.ACCESS_COARSE_LOCATION
					},
					REQUEST_LOCATION_PERMISSION);
			return;
		}

		// Request location updates from the location manager
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0F, this::onLocationChanged);

		currentCadenceNumber = findViewById(R.id.currentCadenceNumber);

		// Get an instance of the SensorManager
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// Get the step detector sensor
		stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);


		// TODO TBD if this should be on a different thread
		Runnable updateCadenceRunnable = new Runnable() {
			@Override
			public void run() {
				// Calculate the cadence
				double cadence = calculateCadence(stepCount, startTime, System.currentTimeMillis() - pauseDuration);
				// Update UI with new cadence
				updateCadence(cadence);
				// Schedule the next update
				handler.postDelayed(this, 1000);
			}
		};

		// TODO add correct start id or if no start button add to onCreate
		Button startButton = findViewById(R.id.startButton);
		startButton.setOnClickListener(v -> {
			// Start step detector listener on start button click
			sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
			// Record start time
			startTime = System.currentTimeMillis();
			// Start updating the cadence
			handler.post(updateCadenceRunnable);
		});

		Button stopButton = findViewById(R.id.endButton);
		stopButton.setOnClickListener(v -> {
			// Stop step detector listener on end button click
			sensorManager.unregisterListener(this, stepDetectorSensor);
			// Stop updating the cadence
			handler.removeCallbacks(updateCadenceRunnable);
			double averageCadence = calculateCadence(stepCount, startTime, System.currentTimeMillis());
			// TODO maybe store this in DB so we can show it on the post-workout screen
		});

		// Pause/resume step dectector listener on pause button press
		Button pauseButton = findViewById(R.id.pause);
		pauseButton.setOnClickListener(v -> {
			togglePause();
		});
	}

	private void updateDistance(Location location) {
		if (location != null) {
			if (routePolyline != null) {
				List<LatLng> points = routePolyline.getPoints();
				LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
				if (!points.isEmpty()) {
					LatLng previousLatLng = points.get(points.size() - 1);
					double distance = calculateDistance(previousLatLng, currentLatLng);
					totalDistance += distance;
					totalPaceDistance += distance; // Update total distance for average pace calculation

					// Calculate trailing mile pace
					if (totalPaceDistance > 0) {
						long currentTime = SystemClock.uptimeMillis();
						long mileTime = currentTime - lastMileTime;
						lastMileTime = currentTime;

						// Update average pace
						long averagePaceTime = totalElapsedTime / (long) totalPaceDistance;
						int paceMinutes = (int) (averagePaceTime / (60 * 1000));
						int paceSeconds = (int) ((averagePaceTime / 1000) % 60);
						String averagePace = String.format(Locale.getDefault(), "%02d:%02d", paceMinutes, paceSeconds);
						avgPace.setText(averagePace);
					}
				}
				points.add(currentLatLng);
				routePolyline.setPoints(points);
				currentDistance.setText(String.format(Locale.getDefault(), "%.2f", totalDistance));
			}
		}
	}


	private float calculateDistance(LatLng start, LatLng end) {
		float[] results = new float[1];
		Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
		return results[0];
	}




	// Start timer from system clock
	private void startTimer() {
		if (startTime == 0) {
			startTime = SystemClock.uptimeMillis();
			currentPace.setText("0.00");
		} else {
			startTime += (SystemClock.uptimeMillis() - timePaused);
		}
		handler.post(updateTimerRunnable);
	}

	// Pauses the timer
	private void pauseTimer() {
		if (!isPaused) {
			handler.removeCallbacks(updateTimerRunnable);
			timePaused = SystemClock.uptimeMillis();
		}
	}

	// Timer thread to update the UI thread display

	private Runnable updateTimerRunnable = new Runnable() {
		public void run() {
			long timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
			int seconds = (int) (timeInMilliseconds / 1000);
			int minutes = seconds / 60;
			seconds %= 60;
			int milliseconds = (int) (timeInMilliseconds % 1000);

			// Update total elapsed time
			totalElapsedTime = timeInMilliseconds;

			String timerText = String.format("%02d:%02d:%03d", minutes, seconds, milliseconds);
			currentTime.setText(timerText);
			handler.postDelayed(this, 10); // Update every 10 milliseconds
		}
	};

	// Destroying the handler
	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(updateTimerRunnable);
	}


	// Starts GPS callback which will consistently update the position of the user.
	@SuppressLint("MissingPermission")
	private void startGPSUpdates(){
		fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallBack,null);
	}



}




	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
			// If step detected, increase step count
			stepCount++;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// No need to overide. Required for interface
	}

	// Calculate overall cadence of run
	private double calculateCadence(int stepCount, long startTime, long endTime) {
		double elapsedTimeInMinutes = (endTime - startTime) / 60000.0;
		return stepCount / elapsedTimeInMinutes;
	}

	private void updateCadence(double cadence) {
		// Update UI with new cadence using full numbers only
		String cadenceText = String.format(Locale.US, "%.0f steps per minute", cadence);
		currentCadenceNumber.setText(cadenceText);
	}

	// Toggle step listener on/off
	private void togglePause() {
		if (isPaused) {
			isPaused = false;
			// Resume the step listener
			sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
			// Calculate paused duration
			pauseDuration += System.currentTimeMillis() - pauseTime;
		} else {
			isPaused = true;
			// Pause the step listener
			sensorManager.unregisterListener(this, stepDetectorSensor);
			// Store the pause start time
			pauseTime = System.currentTimeMillis();
		}
	}
}
