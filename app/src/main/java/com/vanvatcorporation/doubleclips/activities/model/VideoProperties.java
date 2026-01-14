package com.vanvatcorporation.doubleclips.activities.model;

import com.google.gson.annotations.Expose;
import java.io.Serializable;

public class VideoProperties implements Serializable {
    @Expose
    public float valuePosX;
    @Expose
    public float valuePosY;
    @Expose
    public float valueRot;
    @Expose
    public float valueScaleX;
    @Expose
    public float valueScaleY;
    @Expose
    public float valueOpacity;
    @Expose
    public float valueSpeed;

    public VideoProperties() {
        this.valuePosX = 0;
        this.valuePosY = 0;
        this.valueRot = 0;
        this.valueScaleX = 1;
        this.valueScaleY = 1;
        this.valueOpacity = 1;
        this.valueSpeed = 1;
    }

    public VideoProperties(float valuePosX, float valuePosY,
                           float valueRot,
                           float valueScaleX, float valueScaleY,
                           float valueOpacity, float valueSpeed) {
        this.valuePosX = valuePosX;
        this.valuePosY = valuePosY;
        this.valueRot = valueRot;
        this.valueScaleX = valueScaleX;
        this.valueScaleY = valueScaleY;
        this.valueOpacity = valueOpacity;
        this.valueSpeed = valueSpeed;
    }

    public VideoProperties(VideoProperties properties) {
        this.valuePosX = properties.valuePosX;
        this.valuePosY = properties.valuePosY;
        this.valueRot = properties.valueRot;
        this.valueScaleX = properties.valueScaleX;
        this.valueScaleY = properties.valueScaleY;
        this.valueOpacity = properties.valueOpacity;
        this.valueSpeed = properties.valueSpeed;
    }

    public float getValue(ValueType valueType) {
        switch (valueType) {
            case PosX:
                return valuePosX;
            case PosY:
                return valuePosY;
            case Rot:
                return valueRot;
            case RotInRadians:
                return (float) Math.toRadians(valueRot);
            case ScaleX:
                return valueScaleX;
            case ScaleY:
                return valueScaleY;
            case Opacity:
                return valueOpacity;
            case Speed:
                return valueSpeed;
            default:
                return 1;
        }
    }

    public void setValue(float v, ValueType valueType) {
        switch (valueType) {
            case PosX:
                valuePosX = v;
                break;
            case PosY:
                valuePosY = v;
                break;
            case Rot:
                valueRot = v;
                break;
            case ScaleX:
                valueScaleX = v;
                break;
            case ScaleY:
                valueScaleY = v;
                break;
            case Opacity:
                valueOpacity = v;
                break;
            case Speed:
                valueSpeed = v;
                break;
        }
    }

    public enum ValueType {
        PosX, PosY, Rot, RotInRadians, ScaleX, ScaleY, Opacity, Speed
    }
}



