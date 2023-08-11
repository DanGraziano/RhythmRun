package edu.northeastern.rhythmrun;

public class Metronome {

    private int bpm;
    private int beat;
    private int frequency;
    private int silence;
    private double sound;
    private final int tick = 1000; // samples of tick

    private boolean metronomeOn = true;
    private  MetronomeAudio sampleRate = new MetronomeAudio(44100);
    
    public Metronome(){
        MetronomeAudio.createAudioTrack();
        
    }
    public void setSilence(int bpm){
        String sampleRateStr = sampleRate.toString();
        int sampleRateInt = Integer.parseInt(sampleRateStr);
        silence = (((60/bpm) *  sampleRateInt) - tick);
    }

    public void play() {
        setSilence(60);
        double[] tick =  MetronomeAudio.createTone(this.tick, 8000, sound);
        double silence = 0;
        double[] sound = new double[8000];
        int t = 0,s = 0,b = 0;
        do {
            for (int i=0;i<sound.length&&metronomeOn;i++) {
                if (t<this.tick) {
                    sound[i] = tick[t];
                    t++;
                } else {
                    sound[i] = silence;
                    s++;
                    if(s >= this.silence) {
                        t = 0;
                        s = 0;
                        b++;
                        if(b > (this.beat-1))
                            b = 0;
                    }
                }
            }
            MetronomeAudio.fillBuffer(sound);
        } while(metronomeOn);
    }

    public void stop() {
        metronomeOn = false;
        MetronomeAudio.destroyAudioTrack();
    }
}
