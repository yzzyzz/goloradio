package com.golo.goloradio;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import android.graphics.Rect;


public class MarqueeText extends TextView{
    public MarqueeText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public MarqueeText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MarqueeText(Context context) {
        super(context);
    }
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if(focused)
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }
    @Override
    public void onWindowFocusChanged(boolean focused) {
        if(focused)
            super.onWindowFocusChanged(focused);
    }
    @Override
    public boolean isFocused() {
        return true;
    }
}
