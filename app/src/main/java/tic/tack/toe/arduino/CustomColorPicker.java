package tic.tack.toe.arduino;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;

import com.github.mata1.simpledroidcolorpicker.pickers.CircleColorPicker;

public class CustomColorPicker extends CircleColorPicker {

    public CustomColorPicker(Context context, AttributeSet attrs) {
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
