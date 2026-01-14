package com.vanvatcorporation.doubleclips.activities.model;

import com.google.gson.annotations.Expose;
import java.io.Serializable;
import java.util.Map;

public class EffectTemplate implements Serializable {
    @Expose
    public String type;
    @Expose
    public String style;
    @Expose
    public double duration;
    @Expose
    public double offset;
    @Expose
    public Map<String, Object> params;

    public EffectTemplate(String style, double duration, double offset) {
        this.style = style;
        this.duration = duration;
        this.offset = offset;
    }
}



