package edu.northeastern.rhythmrun;

//Dave Centeno
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

public class MetronomeAudio {
    private int sampleRate;
    private static AudioTrack audioTrack;
    private static final int minBufferSize = AudioTrack.getMinBufferSize(44100,AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT);

    public MetronomeAudio(int sampleRate){
        this.sampleRate = sampleRate;
    }

    //Similar to C++ Oscillator sineWave generator
    public static double[] createTone(int samples, int sampleRate, double frequencyOfTone){
        double[] sample = new double[samples];
        for ( int i = 0; i < samples; i ++) {
            sample[i] = Math.sin(2* Math.PI * i / (sampleRate/frequencyOfTone));
        }
        return sample;
    }

    public static byte[] generateSound(double[] samples) {
        byte[] sound = new byte[2 * samples.length];
        int index = 0;
        for (double sample : samples){
            // scale to maximum amplitude
            short maxSample = (short) ((sample * Short.MAX_VALUE));
            // in 16 bit wav PCM, first byte is the low order byte
            sound[index++] = (byte) (maxSample & 0x00ff);
            sound[index++] = (byte) ((maxSample & 0xff00) >>> 8);
        }
        return sound;
    }

    public static void createAudioTrack(){
        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(44100)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(minBufferSize)
                .build();

        audioTrack.play();
    }

    public static void fillBuffer(double[] samples) {
        byte[] buffer = generateSound(samples);
        audioTrack.write(buffer, 0, buffer.length);
    }

    public static void destroyAudioTrack() {
        audioTrack.stop();
        audioTrack.release();
    }}
