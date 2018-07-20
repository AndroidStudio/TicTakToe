package tic.tack.toe.arduino;

import android.app.Application;
import android.nfc.Tag;

import tic.tack.toe.arduino.sockets.UDID;
import timber.log.Timber;

public class GameApplication extends Application {

    private static final String TAG = "GameApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        UDID.init(this);

        Timber.tag(TAG).e(UDID.getUDID());
    }
}
