package edu.northeastern.rhythmrun;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.widget.Button;
import android.media.AudioTrack;


public class MetronomeTest extends AppCompatActivity {

    Button startBtn, stopBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metronome_test);

        startBtn = findViewById(R.id.metronomeBtn);
        stopBtn = findViewById(R.id.stopBtn);


    }
}