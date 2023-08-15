package edu.northeastern.rhythmrun;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.ImageButton;
import java.io.IOException;
import java.util.ArrayList;
import edu.northeastern.rhythmrun.R;

public class MediaPlayerFragment extends Fragment implements Runnable {

    private ImageButton btnPlayPause, btnPrev, btnNext;
    ArrayList<AudioModel> songsList = new ArrayList<>();
    static MediaPlayer mediaPlayer = new MediaPlayer();

    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_media_player, container, false);

        btnPrev = rootView.findViewById(R.id.btn_prev);
        btnPlayPause = rootView.findViewById(R.id.btn_play);
        btnNext = rootView.findViewById(R.id.btn_next);

        initializeSongsFromRaw();

        handler.post(this);

        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnNext.setOnClickListener(v -> playNextSong());
        btnPrev.setOnClickListener(v -> playPreviousSong());

        return rootView;
    }

    private void togglePlayPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            playMusic();
        }
        updatePlayPauseButtonIcon();
    }

    void initializeSongsFromRaw() {

        AudioModel song1 = new AudioModel(R.raw.music1);
        //song1.setPath(R.raw.music1);
        songsList.add(song1);

        AudioModel song2 = new AudioModel(R.raw.music2);
        //song2.setPath(R.raw.music2);
        songsList.add(song2);

    }

    private void updatePlayPauseButtonIcon() {
        if (mediaPlayer.isPlaying()) {
            btnPlayPause.setImageResource(R.drawable.baseline_pause_24);
        } else {
            btnPlayPause.setImageResource(R.drawable.baseline_play_arrow_24);
        }
    }

    private void playMusic() {
        if (songsList == null || songsList.isEmpty() ||
                MyMediaPlayer.currentIndex < 0 ||
                MyMediaPlayer.currentIndex >= songsList.size()) {
            return;
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        if (!mediaPlayer.isPlaying()) {
            try {
                AudioModel currentSong = songsList.get(MyMediaPlayer.currentIndex);
                mediaPlayer.reset();
                Uri songUri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + currentSong.getPath());
                mediaPlayer.setDataSource(getActivity(), songUri);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void playNextSong() {
        MyMediaPlayer.currentIndex = (MyMediaPlayer.currentIndex + 1) % songsList.size();
        playMusic();
        updatePlayPauseButtonIcon();
    }

    private void playPreviousSong() {
        MyMediaPlayer.currentIndex = (MyMediaPlayer.currentIndex - 1 + songsList.size()) % songsList.size();
        playMusic();
        updatePlayPauseButtonIcon();
    }

    @Override
    public void run() {
        updatePlayPauseButtonIcon();
        handler.postDelayed(this, 1000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(this);
    }
}
