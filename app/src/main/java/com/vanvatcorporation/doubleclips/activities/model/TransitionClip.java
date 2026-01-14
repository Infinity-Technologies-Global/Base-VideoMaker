package com.vanvatcorporation.doubleclips.activities.model;

import com.google.gson.annotations.Expose;
import java.io.Serializable;

public class TransitionClip implements Serializable {
    @Expose
    public int trackIndex;
    @Expose
    public float startTime;
    @Expose
    public float duration;
    @Expose
    public EffectTemplate effect;
    @Expose
    public TransitionMode mode;

    public enum TransitionMode {
        END_FIRST,
        OVERLAP,
        BEGIN_SECOND
    }
}



