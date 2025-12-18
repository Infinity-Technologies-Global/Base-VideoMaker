package com.vanvatcorporation.doubleclips.activities.editing;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.vanvatcorporation.doubleclips.FXCommandEmitter;
import com.vanvatcorporation.doubleclips.R;
import com.vanvatcorporation.doubleclips.activities.EditingActivity;

public class TransitionEditSpecificAreaScreen extends BaseEditSpecificAreaScreen {

    public Button applyAllTransitionButton;
    public Spinner transitionEditContent;
    public EditText transitionDurationContent;
    public Spinner transitionModeEditContent;


    public TransitionEditSpecificAreaScreen(Context context) {
        super(context);
    }

    public TransitionEditSpecificAreaScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TransitionEditSpecificAreaScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TransitionEditSpecificAreaScreen(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void init()
    {
        super.init();

        applyAllTransitionButton = findViewById(R.id.applyAllButton);
        transitionEditContent = findViewById(R.id.transitionContent);
        transitionDurationContent = findViewById(R.id.durationContent);
        transitionModeEditContent = findViewById(R.id.transitionModeContent);

        transitionEditContent.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, FXCommandEmitter.FXRegistry.transitionFXMap.values().toArray(new String[0])));
        transitionModeEditContent.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, EditingActivity.TransitionClip.TransitionMode.values()));


        onClose.add(() -> {
            transitionEditContent.clearFocus();
            transitionDurationContent.clearFocus();
        });
    }





}
