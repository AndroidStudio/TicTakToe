package tic.tack.toe.arduino.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import tic.tack.toe.arduino.R;

public class FragmentController {

    public static void setCurrentFragment(FragmentActivity context, Fragment fragment) {
        context.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentLayout, fragment, null)
                .commitNow();
    }
}
