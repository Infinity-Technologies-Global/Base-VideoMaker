package com.vanvatcorporation.doubleclips.activities.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.google.android.material.imageview.ShapeableImageView;
import com.vanvatcorporation.doubleclips.R;

import java.util.ArrayList;

public class BaseAreaScreen extends RelativeLayout {


    public BaseAreaScreen(Context context) {
        super(context);
    }

    public BaseAreaScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseAreaScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BaseAreaScreen(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    public void init() {
    }
}
