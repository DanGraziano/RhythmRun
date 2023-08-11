package edu.northeastern.rhythmrun;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

public class ActiveWorkout extends AppCompatActivity implements SensorEventListener {

	private SensorManager sensorManager;
	private Sensor stepDetectorSensor;
	private int stepCount = 0;
	private long startTime = 0;
	private TextView currentCadenceNumber;
	private long pauseTime = 0;
	private long pauseDuration = 0;

	private Handler handler = new Handler();
	private boolean isPaused = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_active_workout);

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
