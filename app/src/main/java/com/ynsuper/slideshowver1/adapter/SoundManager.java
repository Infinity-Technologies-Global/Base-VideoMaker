package com.ynsuper.slideshowver1.adapter;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.ynsuper.slideshowver1.util.Constant;

public class SoundManager {
    // Tạo instance cho SoundManager
    private static SoundManager sInstance;

    // Tạo 1 đối tượng MediaPlayer
    private  MediaPlayer mMediaPlayer;
    private String currentUrl;
    private String nameAudio;

    public static SoundManager getInstance(Context context) {
        if (null == sInstance) {
            synchronized (SoundManager.class) {
                sInstance = new SoundManager(context);
            }
        }
        return sInstance;
    }

    /**
     * Constructor
     */
    private SoundManager(Context context) {
        // Khởi tạo đối tượng MediaPlayer bằng cách get sound từ folder raw
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    /**
     * Method dùng để start nhạc nền
     */
    public void startSound() {
        mMediaPlayer.start();
    }

    public MediaPlayer getmMediaPlayer() {
        return mMediaPlayer;
    }

    public void changeSound(String url, String nameAudio) {
        try {
//            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync();
            this.currentUrl = url;
            this.nameAudio = nameAudio;
        } catch (Exception e) {
            Log.e(Constant.Companion.getNDPHH_TAG(), e.getMessage());
        }
    }

    public String getCurrentUrl() { return currentUrl; }

    public void setCurrentUrl(String currentUrl) {
        this.currentUrl = currentUrl;
    }

    public String getNameAudio() { return nameAudio; }

    public void setNameAudio(String nameAudio) { this.nameAudio = nameAudio; }

    /**
     * Method dùng để start nhạc nền
     */
    public void pauseSound() {
        mMediaPlayer.pause();
    }

    /**
     * Method dùng để stop nhạc nền
     */
    public void stopSound() {
        mMediaPlayer.stop();
    }
}
