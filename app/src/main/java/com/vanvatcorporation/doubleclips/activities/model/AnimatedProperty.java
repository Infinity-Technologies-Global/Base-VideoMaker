package com.vanvatcorporation.doubleclips.activities.model;

import com.google.gson.annotations.Expose;
import com.vanvatcorporation.doubleclips.helper.MathHelper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AnimatedProperty implements Serializable {
    @Expose
    public List<Keyframe> keyframes = new ArrayList<>();

    public float getValueAtTime(Clip clip, float playheadTime, VideoProperties.ValueType valueType) {
        if (keyframes.isEmpty()) return -1;
        playheadTime -= clip.startTime;
        Keyframe prev = keyframes.get(0);
        for (Keyframe next : keyframes) {
            if (playheadTime < next.getLocalTime()) {
                float t = (playheadTime - prev.getLocalTime()) / (next.getLocalTime() - prev.getLocalTime());
                t = Math.max(0f, Math.min(1f, t));
                float prevValue = prev.value.getValue(valueType);
                float nextValue = next.value.getValue(valueType);
                return lerp(prevValue, nextValue, ease(t, prev.easing));
            }
            prev = next;
        }
        return keyframes.get(keyframes.size() - 1).value.getValue(valueType);
    }

    public float getValueAtPoint(int keyframeIndex, VideoProperties.ValueType valueType) {
        if (keyframes.isEmpty()) return 0f;
        return keyframes.get(keyframeIndex).value.getValue(valueType);
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private float ease(float t, EasingType type) {
        switch (type) {
            case LINEAR: return t;
            case EASE_IN: return t * t;
            case EASE_OUT: return 1 - (1 - t) * (1 - t);
            case EXPONENTIAL: return (float)Math.pow(2, 10 * (t - 1));
            case EASE_IN_OUT:
                return t < 0.5f ? 2 * t * t : 1 - (float)Math.pow(-2 * t + 2, 2) / 2;
            case QUADRATIC:
                return t * t;
            case SPRING:
                MathHelper.spring(t, 1, 12, 0.5f);
            default: return t;
        }
    }
}



