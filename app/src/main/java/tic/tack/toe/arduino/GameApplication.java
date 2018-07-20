package tic.tack.toe.arduino;

import android.app.Application;

import tic.tack.toe.arduino.sockets.UDID;
import timber.log.Timber;

public class GameApplication extends Application {

    private static final String TAG = "GameApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        this.initTimber();
        this.initUDID();
    }

    private void initUDID() {
        UDID.init(this);
        Timber.tag(TAG).e("UDID: %s" , UDID.getUDID());
    }

    private void initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
