package tic.tack.toe.arduino;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

public class CustomSeekBar extends AppCompatSeekBar {

    public CustomSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean result = super.dispatchTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            disableTouch(getParent());
        return result;
    }

    private void disableTouch(ViewParent parent) {
        if (parent == null) {
            return;
        }

        if (parent instanceof DrawerLayout) {
            parent.requestDisallowInterceptTouchEvent(true);
        } else {
            disableTouch(parent.getParent());
        }
    }
}
