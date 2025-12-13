package com.vanvatcorporation.doubleclips.activities.editing;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import com.vanvatcorporation.doubleclips.R;

public class VideoPropertiesEditSpecificAreaScreen extends BaseEditSpecificAreaScreen {


    public EditText resolutionXField, resolutionYField, bitrateField;



    public VideoPropertiesEditSpecificAreaScreen(Context context) {
        super(context);
    }

    public VideoPropertiesEditSpecificAreaScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoPropertiesEditSpecificAreaScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VideoPropertiesEditSpecificAreaScreen(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void init() {
        super.init();

        resolutionXField = findViewById(R.id.resolutionXField);
        resolutionYField = findViewById(R.id.resolutionYField);
        bitrateField = findViewById(R.id.bitrateField);

        animationScreen = AnimationScreen.ToBottom;
    }
}
