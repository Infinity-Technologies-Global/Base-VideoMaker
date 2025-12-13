package com.vanvatcorporation.doubleclips.activities.editing;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.vanvatcorporation.doubleclips.FXCommandEmitter;
import com.vanvatcorporation.doubleclips.R;

public class EffectEditSpecificAreaScreen extends BaseEditSpecificAreaScreen {


    public Spinner effectEditContent;
    public EditText effectDurationContent;

    public EffectEditSpecificAreaScreen(Context context) {
        super(context);
    }

    public EffectEditSpecificAreaScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EffectEditSpecificAreaScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EffectEditSpecificAreaScreen(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void init()
    {
        super.init();
        effectEditContent = findViewById(R.id.effectContent);
        effectDurationContent = findViewById(R.id.durationContent);


        effectEditContent.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, FXCommandEmitter.FXRegistry.effectsFXMap.values().toArray(new String[0])));

    }


}
