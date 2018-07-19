package tic.tack.toe.arduino.game;

import android.annotation.SuppressLint;
import android.content.Context;

public class GameCore {

    @SuppressLint("StaticFieldLeak")
    private static GameCore INSTANCE = null;
    private final Context context;

    private GameCore(Context context) {
        this.context = context;
    }

    public static synchronized GameCore getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new GameCore(context);
        }
        return INSTANCE;
    }

}
