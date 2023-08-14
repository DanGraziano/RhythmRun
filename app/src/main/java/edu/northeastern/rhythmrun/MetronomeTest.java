package edu.northeastern.rhythmrun;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.media.AudioTrack;


public class MetronomeTest extends AppCompatActivity {

    Button startBtn, stopBtn;
    float bpm = 0;

    Boolean metronomeOn = false;

    public Metronome metronome = new Metronome();
    public Metronome.MetronomeThread playMetronomeThread = new Metronome.MetronomeThread(80);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metronome_test);

        startBtn = findViewById(R.id.metronomeBtn);
        stopBtn = findViewById(R.id.stopBtn);

        bpm = 180;

        startBtn.setOnClickListener(v -> playMetronome(bpm));
        stopBtn.setOnClickListener(v -> stopMetronome());

    }

    private void playMetronome(float bpm){
        //If the metronome has been paused before, create a new audio stream and
        //set the metronome on.
        if(metronomeOn == false){
            metronome.on();
            metronomeOn = true;
        }
        //Creates the metronome in a new thread so other UI is accessible
        new Thread(playMetronomeThread).start();

    }

    private void stopMetronome(){
        //Stop sets the boolean metronomeOn off and destroys the audio thread.
        metronome.stop();
        metronomeOn = false;
    }
}

