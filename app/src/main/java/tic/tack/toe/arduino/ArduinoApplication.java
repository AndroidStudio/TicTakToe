package tic.tack.toe.arduino;

import android.app.Application;

import timber.log.Timber;

public class ArduinoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
    }
}