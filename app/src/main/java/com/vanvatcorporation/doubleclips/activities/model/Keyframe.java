package com.vanvatcorporation.doubleclips.activities.model;

import com.google.gson.annotations.Expose;
import java.io.Serializable;

public class Keyframe implements Serializable {
    @Expose
    private float time;
    @Expose
    public VideoProperties value;
    @Expose
    public EasingType easing = EasingType.LINEAR;

    public Keyframe(float time, VideoProperties value, EasingType easing) {
        this.time = time;
        this.value = value;
        this.easing = easing;
    }

    public float getLocalTime() {
        return time;
    }

    public float getGlobalTime(Clip clip) {
        return time + clip.startTime;
    }
}



