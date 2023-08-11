package edu.northeastern.rhythmrun;

import android.media.MediaPlayer;

public class MyMediaPlayer {

    private static MyMediaPlayer instance;
    private MediaPlayer mediaPlayer;
    public static int currentIndex = 0;

    private MyMediaPlayer() {
        mediaPlayer = new MediaPlayer();
    }

    public static MyMediaPlayer getInstance() {
        if (instance == null) {
            instance = new MyMediaPlayer();
        }
        return instance;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}
