package edu.northeastern.rhythmrun;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;

import java.util.Locale;

public class ActiveWorkout extends AppCompatActivity {

	private TextView targetSPM;
	private TextView currentCadence;
	private ImageView soundToggle;
	private TextView currentPace;
	private TextView avgPace;
	private TextView currentDistance;
	private TextView GPS;
	private Button end;
	private Button pause;

	private SensorManager sensorManager;
	private Sensor accelerometer;
	private boolean isRunning = false;
	private boolean isPaused = false;
	private int stepCount = 0;

	private Location previousLocation;
	private double totalDistance = 0.0;
	private double totalTimeInSeconds = 0.0;

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
		pause = findViewById(R.id.pause);
		end = findViewById(R.id.endButton);

		// Initialize sensor manager and accelerometer sensor
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// Initialize fragment
		Fragment fragment = new Fragment();

		// Display map fragment
		getSupportFragmentManager()
				.beginTransaction().replace(R.id.google_map,fragment)
				.commit();

		// Set click listener for the END button
		end.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Perform actions to end the workout, e.g., save data, stop tracking, etc.
				// TODO: add stats activity screen
				//stopTracking();
				Toast.makeText(ActiveWorkout.this, "Workout Ended", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(ActiveWorkout.this, StartWorkout.class);
				startActivity(intent);
			}
		});

		pause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!isPaused) {
					pause.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.resume));
					pause.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(),R.color.gps_status_ready));
					isPaused = true;
					Toast.makeText(ActiveWorkout.this, "Workout Paused", Toast.LENGTH_SHORT).show();
				}else{
					pause.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.pause));
					pause.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(),R.color.pause));
					isPaused = false;
					Toast.makeText(ActiveWorkout.this, "Workout Resumed", Toast.LENGTH_SHORT).show();
				}
			}
		});


		currentDistance.setText(Double.toString(totalDistance));


		// Start GPS tracking fragment
//		GPS.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// Implement logic to start GPS tracking fragment in real-time.
//				// You'll need to replace the placeholder TextView with the GPS tracking fragment.
//				// You can use FragmentTransaction to replace the placeholder with the GPS fragment.
//				// For simplicity, I'm just showing a toast here.
//				// Toast.makeText(ActiveWorkout.this, "GPS Tracking Started", Toast.LENGTH_SHORT).show();
//			}
//		});
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
}