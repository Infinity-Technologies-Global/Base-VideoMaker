package com.vanvatcorporation.doubleclips.activities.model;

import android.content.Context;
import com.google.gson.Gson;
import com.vanvatcorporation.doubleclips.activities.main.MainAreaScreen;
import com.vanvatcorporation.doubleclips.constants.Constants;
import com.vanvatcorporation.doubleclips.helper.IOHelper;
import java.io.Serializable;

public class VideoSettings implements Serializable {
    public int videoWidth;
    public int videoHeight;
    public int frameRate;
    public int crf;
    public int clipCap;
    public String preset;
    public String tune;

    public VideoSettings(int videoWidth, int videoHeight, int frameRate, int crf, int clipCap, String preset, String tune) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.frameRate = frameRate;
        this.crf = crf;
        this.clipCap = clipCap;
        this.preset = preset;
        this.tune = tune;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public int getCRF() {
        return crf;
    }

    public int getClipCap() {
        return clipCap;
    }

    public String getPreset() {
        return preset;
    }

    public String getTune() {
        return tune;
    }

    public void saveSettings(Context context, MainAreaScreen.ProjectData data) {
        IOHelper.writeToFile(context, IOHelper.CombinePath(data.getProjectPath(), Constants.DEFAULT_VIDEO_SETTINGS_FILENAME), new Gson().toJson(this));
    }

    public void loadSettingsFromProject(Context context, MainAreaScreen.ProjectData data) {
        VideoSettings loadSettings = loadSettings(context, data);
        this.videoWidth = loadSettings.videoWidth;
        this.videoHeight = loadSettings.videoHeight;
        this.frameRate = loadSettings.frameRate;
        this.crf = loadSettings.crf;
        this.clipCap = loadSettings.clipCap;
        this.preset = loadSettings.preset;
        this.tune = loadSettings.tune;
    }

    public static VideoSettings loadSettings(Context context, MainAreaScreen.ProjectData data) {
        return new Gson().fromJson(IOHelper.readFromFile(context, IOHelper.CombinePath(data.getProjectPath(), Constants.DEFAULT_VIDEO_SETTINGS_FILENAME)), VideoSettings.class);
    }

    public static class FfmpegPreset {
        public static final String PLACEBO = "placebo";
        public static final String VERYSLOW = "veryslow";
        public static final String SLOWER = "slower";
        public static final String SLOW = "slow";
        public static final String MEDIUM = "medium";
        public static final String FAST = "fast";
        public static final String FASTER = "faster";
        public static final String VERYFAST = "veryfast";
        public static final String SUPERFAST = "superfast";
        public static final String ULTRAFAST = "ultrafast";
    }

    public static class FfmpegTune {
        public static final String FILM = "film";
        public static final String ANIMATION = "animation";
        public static final String GRAIN = "grain";
        public static final String STILLIMAGE = "stillimage";
        public static final String FASTDECODE = "fastdecode";
        public static final String ZEROLATENCY = "zerolatency";
    }
}



