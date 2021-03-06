package tic.tack.toe.arduino.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import tic.tack.toe.arduino.R;

public class GameSettings {

    private final static String DEFAULT_PLAYER_01_COLOR = "FF0000";
    private final static String DEFAULT_PLAYER_02_COLOR = "0000FF";

    private final static int DEFAULT_BRIGHTNESS = 255;

    private final static int PLAYER_01SYMBOL_DEFAULT = R.drawable.ic_game_symbol_01;
    private final static int PLAYER_02SYMBOL_DEFAULT = R.drawable.ic_game_symbol_02;

    private static GameSettings INSTANCE = null;

    private String mPlayer_01Color = DEFAULT_PLAYER_01_COLOR;
    private String mPlayer_02Color = DEFAULT_PLAYER_02_COLOR;

    private int mPlayer_01Symbol = PLAYER_01SYMBOL_DEFAULT;
    private int mPlayer_02Symbol = PLAYER_02SYMBOL_DEFAULT;

    private int mBrightness = DEFAULT_BRIGHTNESS;

    public final static int NO_SYMBOL_INDEX = -1;

    public final static int[] mSymbolArray = new int[]{
            R.drawable.ic_game_symbol_01,
            R.drawable.ic_game_symbol_02,
            R.drawable.ic_game_symbol_03,
            R.drawable.ic_game_symbol_04,
            R.drawable.ic_game_symbol_05,
            R.drawable.ic_game_symbol_06,
            R.drawable.ic_game_symbol_07,
            R.drawable.ic_game_symbol_08,
            R.drawable.ic_game_symbol_09,
    };

    public static synchronized GameSettings getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GameSettings();
        }
        return INSTANCE;
    }

    public void setLedBrightness(int brightness) {
        this.mBrightness = brightness;
    }

    public int getLedBrightness() {
        return this.mBrightness;
    }

    /*
     * Mac address bluetooth
     * */
    public void setMacAddress(String mac, Context context) {
        SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(context);
        preferenceManager.edit().putString("MAC", mac).apply();
    }

    public String getMacAddress(Context context) {
        SharedPreferences preferenceManager = PreferenceManager.getDefaultSharedPreferences(context);
        return preferenceManager.getString("MAC", "");
    }

    /*
     * Kolor gracza 1
     * */
    public void setPlayer_01Color(String mPlayer_01Color) {
        this.mPlayer_01Color = mPlayer_01Color;
    }

    public String getPlayer_01Color() {
        return this.mPlayer_01Color;
    }

    /*
     * Kolor gracza 2
     * */
    public String getPlayer_02Color() {
        return this.mPlayer_02Color;
    }

    public void setPlayer_02Color(String mPlayer_02Color) {
        this.mPlayer_02Color = mPlayer_02Color;
    }

    /*
     * Symbol gracza 1
     * */
    public int getPlayer_01Symbol() {
        return this.mPlayer_01Symbol;
    }

    public void setPlayer_01Symbol(int mPlayer_01Symbol) {
        this.mPlayer_01Symbol = mPlayer_01Symbol;
    }

    /*
     * Symbol gracza 2
     * */
    public int getPlayer_02Symbol() {
        return this.mPlayer_02Symbol;
    }

    public void setPlayer_02Symbol(int mPlayer_02Symbol) {
        this.mPlayer_02Symbol = mPlayer_02Symbol;
    }
}
