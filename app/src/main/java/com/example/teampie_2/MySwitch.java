package com.example.teampie_2;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.Switch;

public class MySwitch extends Switch {
    public int index =0;
    public MySwitch(Context context){
        super(context);
    }

    public MySwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
