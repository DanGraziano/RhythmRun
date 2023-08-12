package edu.northeastern.rhythmrun;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;

public class RunStats extends AppCompatActivity {

    private TextView totalDistanceTextView;
    private TextView currentPaceNumberTextView;
    private TextView avgCadenceTextView;
    private TextView avgPaceNumberTextView;
    private TextView timeStatsTextView;
    private TextView finalTimeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_stats);

        // Find views by their IDs
        totalDistanceTextView = findViewById(R.id.totalDistance);
        currentPaceNumberTextView = findViewById(R.id.currentPaceNumber);
        avgCadenceTextView = findViewById(R.id.avgCadence);
        avgPaceNumberTextView = findViewById(R.id.avgPaceNumber);
        timeStatsTextView = findViewById(R.id.timeStatsText);
        finalTimeTextView = findViewById(R.id.finalTime);

        // Access other views and fragments if needed

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }
}