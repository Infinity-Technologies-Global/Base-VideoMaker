package com.vanvatcorporation.doubleclips.activities.editing;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.vanvatcorporation.doubleclips.R;

public class TextEditSpecificAreaScreen extends BaseEditSpecificAreaScreen {

    public EditText textEditContent;
    public EditText textSizeContent;


    public TextEditSpecificAreaScreen(Context context) {
        super(context);
    }

    public TextEditSpecificAreaScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextEditSpecificAreaScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TextEditSpecificAreaScreen(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void init()
    {
        super.init();
        textEditContent = findViewById(R.id.textContent);
        textSizeContent = findViewById(R.id.sizeContent);


        onClose.add(() -> {
            textEditContent.clearFocus();
            textSizeContent.clearFocus();
        });
    }


}
