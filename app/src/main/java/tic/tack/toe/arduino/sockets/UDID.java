package tic.tack.toe.arduino.sockets;

import android.content.Context;
import android.provider.Settings;

public class UDID {

    private static UDID INSTANCE = null;
    private String mUdid;

    private UDID(Context context) {
        this.mUdid = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static synchronized void init(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new UDID(context);
        }
    }

    public static String getUDID() {
        return INSTANCE.mUdid;
    }
}
