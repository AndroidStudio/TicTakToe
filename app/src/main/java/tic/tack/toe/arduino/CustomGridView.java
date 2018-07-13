package tic.tack.toe.arduino;

import android.content.Context;
import android.util.AttributeSet;

public class CustomGridView extends android.support.v7.widget.GridLayout {

    public CustomGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int parentWidth = MeasureSpec.getSize(widthSpec);
        int parentHeight = MeasureSpec.getSize(heightSpec);

        if (parentHeight > parentWidth) {
            super.onMeasure(widthSpec, widthSpec);
        } else {
            super.onMeasure(heightSpec, heightSpec);
        }
    }
}
