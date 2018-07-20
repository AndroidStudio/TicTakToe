package tic.tack.toe.arduino;

import android.os.Bundle;
import android.support.annotation.Nullable;

import tic.tack.toe.arduino.fragments.FragmentController;
import tic.tack.toe.arduino.splash.InitFragment;

public class InitDeviceActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        if (savedInstanceState == null) {
            FragmentController.setCurrentFragment(this, new InitFragment());
        }
    }
}
