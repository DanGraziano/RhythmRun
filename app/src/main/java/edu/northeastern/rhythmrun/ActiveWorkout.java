package edu.northeastern.rhythmrun;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;

public class ActiveWorkout extends AppCompatActivity {

	private TextView targetSPM;

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

		// Starts the timer for the run
		handler = new Handler();
		startTimer();

		// Initialize sensor manager and accelerometer sensor
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// Initialize fragment
		Fragment fragment = new Fragment();

		// Display map fragment
		getSupportFragmentManager()
				.beginTransaction().replace(R.id.google_map, fragment)
				.commit();

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

	// Start timer from system clock
	private void startTimer() {
		if (startTime == 0) {
			startTime = SystemClock.uptimeMillis();
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


}


//	// Register accelerometer listener onResume
//	@Override
//	protected void onResume() {
//		super.onResume();
//		if (accelerometer != null) {
//			sensorManager.registerListener((SensorEventListener) this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//		}
//	}
//
//	// Unregister accelerometer listener onPause
//	@Override
//	protected void onPause() {
//		super.onPause();
//		sensorManager.unregisterListener((SensorEventListener) this, accelerometer);
//	}
//
//
//	public void onAccuracyChanged(Sensor sensor, int accuracy) {
//		// Do nothing for this example
//	}
//
//	public void onSensorChanged(SensorEvent event) {
//		// Calculate cadence based on accelerometer data
//		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//			float[] values = event.values;
//			float x = values[0];
//			float y = values[1];
//			float z = values[2];
//
//			// Assuming cadence is related to step count (could be more complex in real-world scenarios)
//			// You may need to calibrate these values based on your specific device and user behavior.
//			if (Math.abs(x) > 8 || Math.abs(y) > 8 || Math.abs(z) > 8) {
//				if (!isRunning) {
//					isRunning = true;
//					stepCount++;
//					currentCadence.setText(String.valueOf(stepCount) + " SPM");
//				}
//			} else {
//				isRunning = false;
//			}
//		}
//	}
//
//	// Method to stop tracking and calculate average pace
//	private void stopTracking() {
//		calculateAvgPace();
//	}
//
//	// Method to calculate average pace
//	private void calculateAvgPace() {
//		// Calculate average pace based on the total distance and total time taken.
//		// For simplicity, let's assume you have already updated these values during the workout.
//		double averagePaceInMinutes = totalTimeInSeconds / totalDistance;
//
//		// Update the TextView with the average pace in the format of minutes per mile.
//		String avgPaceText = String.format(Locale.getDefault(), "%.2f", averagePaceInMinutes);
//		avgPace.setText(avgPaceText + " min/mile");
//	}
//}