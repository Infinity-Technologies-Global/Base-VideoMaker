package com.vanvatcorporation.doubleclips.activities.editing;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.vanvatcorporation.doubleclips.R;

public class ClipsEditSpecificAreaScreen extends BaseEditSpecificAreaScreen {


    public EditText clipsDurationContent;


    public ClipsEditSpecificAreaScreen(Context context) {
        super(context);
    }

    public ClipsEditSpecificAreaScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClipsEditSpecificAreaScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ClipsEditSpecificAreaScreen(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void init()
    {
        super.init();
        clipsDurationContent = findViewById(R.id.durationContent);
    }




    @Override
    public void open(AnimationScreen animationScreen) {
        super.open(animationScreen);
    }

    @Override
    public void close(AnimationScreen animationScreen) {
        super.close(animationScreen);
    }
}
