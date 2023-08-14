package edu.northeastern.rhythmrun;

public class Metronome {

    private int beat;
    private static int silence;
    private static final int tick = 1000; // samples of tick

    private static boolean metronomeOn = true;

    //Creates the initial audio Track
    public Metronome(){
        MetronomeAudio.createAudioTrack();
        
    }

    //Equation that sets the beats perminute.
    //44100 is the sample rate equivelant to 1 second of sound
    //Silence is the equiveleant amount of samples left to create silence.
    public static void setBpm(int bpm){
        //Equation that sets the beats per minute.
        silence = (int) (((60/bpm)*8000)-tick);
    }

    //Plays the metronome
    //Adaptation of https://masterex.github.io/archive/2012/05/28/android-audio-synthesis.html
    //The idea here is to store sound for the sample size of the tick. Once that is filled the rest
    //of the soundBuffer is filled with silence.
    public static void play(int bpm) {
        //Sets the bpm
        setBpm(bpm);

        //Initialize ticks
        int ticks = 0;
        int silentTicks = 0;
        int silence = 0;

        //Generate the sound to be stored.
        double[] tick =  MetronomeAudio.createTone(Metronome.tick, 8000, 440);
        //create sound  buffer
        double[] soundBuffer = new double[192000];

        //Play metronome ticks size of soundBuffer until metronome is turned off.
        while(metronomeOn) {
            for (int i=0;i<soundBuffer.length&&metronomeOn;i++) {
                //Fills the array with sound or silence
                if (ticks < Metronome.tick) {
                    soundBuffer[i] = tick[ticks];
                    ticks++;
                } else {
                    soundBuffer[i] = silence;
                    silentTicks++;
                    if(silentTicks >= Metronome.silence) {
                        ticks = 0;
                        silentTicks = 0;
                    }
                }
            }
            //Once array is complete fill buffer.
            MetronomeAudio.fillBuffer(soundBuffer);
        }
    }

    //creates a new audiotrack and sets the metronome on.
    public void on() {
        metronomeOn = true;
        MetronomeAudio.createAudioTrack();
    }

    //Destroys the audio track so it is not abandoned
    public void stop() {
        metronomeOn = false;
        MetronomeAudio.destroyAudioTrack();
    }


    //Inner class that handles creating a thread for the Metronome play
    public static class MetronomeThread implements Runnable{
        int bpm;
        MetronomeThread(int bpm){
            this.bpm = bpm;
        }
        @Override
        public void run() {
            play(bpm);
        }

    }
}