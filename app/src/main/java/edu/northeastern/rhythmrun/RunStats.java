package edu.northeastern.rhythmrun;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class RunStats extends AppCompatActivity {

    private TextView currentPaceNumberTextView;
    private TextView avgPaceNumberTextView;
    private TextView finalTimeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_stats);

        // Find views by their IDs
        currentPaceNumberTextView = findViewById(R.id.currentPaceNumber);
        avgPaceNumberTextView = findViewById(R.id.avgPaceNumber);
        finalTimeTextView = findViewById(R.id.finalTime);

        readDb(new DataReceivedListener() {
            @Override
            public void onDataReceived(TestRun data) {
                currentPaceNumberTextView.setText(String.valueOf(data.getPace()));
                avgPaceNumberTextView.setText(String.valueOf(data.getDistance()));
                finalTimeTextView.setText(String.valueOf(data.getTime()));
            }
        });

    }

    interface DataReceivedListener{
        void onDataReceived(TestRun data);
    }

    private void readDb(DataReceivedListener listener) {
        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference runsRef = database.getReference("Runs");
        String runKey = "B8rYp2ZjN6sDxTq9GvHcLm4JkE7w"; // Replace with the actual run key

        // Create a query to retrieve the specific run data
        Query runQuery = runsRef.child(runKey);

        runQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            TestRun run;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Parse and process the run data
                    // Parse and process the run data
                    String cadenceTime = dataSnapshot.child("cadenceTime").getValue(String.class);
                    int calories = dataSnapshot.child("calories").getValue(Integer.class);
                    String date = dataSnapshot.child("date").getValue(String.class);
                    int distance = dataSnapshot.child("distance").getValue(Integer.class);
                    int pace = dataSnapshot.child("pace").getValue(Integer.class);
                    String time = dataSnapshot.child("time").getValue(String.class);

                    Log.d("fuckingWork", date);


                    // Set the retrieved values to the TextViews
                    run = new TestRun(cadenceTime, calories, date, distance, pace, time);
                } else {
                    Log.d("RunStats", "No run data found for the specified key.");
                }
                listener.onDataReceived(run);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("RunStats", "Error reading run data: " + databaseError.getMessage());
            }
        });
    }



    public void onBackPressed() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }
}