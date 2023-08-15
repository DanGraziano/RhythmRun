package edu.northeastern.rhythmrun;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RunStats extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_stats);

        TextView distanceData = findViewById(R.id.distanceData);
        TextView cadenceData = findViewById(R.id.cadenceData);
        TextView dateData = findViewById(R.id.dateData);
        TextView paceData = findViewById(R.id.paceData);
        TextView timeData = findViewById(R.id.timeData);
        Button saveButton = findViewById(R.id.saveButton);
        Button discardButton = findViewById(R.id.discardButton);

        // Retrieve data from the intent's extras
        Intent intentData = getIntent();
        String totalDistance = intentData.getStringExtra("distance");
        String avgCadence = intentData.getStringExtra("avgCadence");
        String avgPace = intentData.getStringExtra("avgPace");
        String totalTime = intentData.getStringExtra("time");
        String currentDate = intentData.getStringExtra("date");

        // Show data in text views
        distanceData.setText(totalDistance  + " mi");
        cadenceData.setText(avgCadence + " spm");
        timeData.setText(totalTime  + " mins");
        paceData.setText(avgPace  + " /mi");
        dateData.setText(currentDate);

        // On save button press, save run data to database
        saveButton.setOnClickListener(v -> {
            // Get the current user ID
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            assert currentUser != null;
            String userUid = currentUser.getUid();

            // Get a reference to the "Runs" node for the current user
            DatabaseReference userRunsRef = FirebaseDatabase.getInstance().getReference("Users")
                    .child(userUid)
                    .child("Runs");

            // Generate a unique ID for the new run entry
            String runId = userRunsRef.push().getKey();

            // Create a HashMap to store the run data
            HashMap<String, Object> runData = new HashMap<>();
            runData.put("date", currentDate);
            runData.put("distance", totalDistance);
            runData.put("avgCadence", avgCadence);
            runData.put("avgPace", avgPace);
            runData.put("time", totalTime);

            // Add the run data to the database
            assert runId != null;
            userRunsRef.child(runId).setValue(runData)
                    .addOnSuccessListener(aVoid -> {
                        // Data successfully added to the database
                        Toast.makeText(RunStats.this, "Run data saved", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Error occurred while adding data to the database
                        Toast.makeText(RunStats.this, "Failed to save run data", Toast.LENGTH_SHORT).show();
                    });
            Intent intent = new Intent(RunStats.this, Home.class);
            startActivity(intent);
        });

        discardButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(RunStats.this);
            builder.setTitle("Discard Run Data");
            builder.setMessage("Are you sure you want to discard this run? This action cannot be undone.");

            builder.setPositiveButton("Yes, Discard", (dialog, which) -> {
                // If yes, go to home screen
                Intent intent = new Intent(RunStats.this, Home.class);
                startActivity(intent);
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> {
                // If no, stay on the RunStats screen
                dialog.dismiss();
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

    }

    // Disable back press
    @Override
    public void onBackPressed() {
    }
}