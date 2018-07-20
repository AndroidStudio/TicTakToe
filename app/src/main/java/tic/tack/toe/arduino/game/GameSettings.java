package tic.tack.toe.arduino.game;

import tic.tack.toe.arduino.R;

public class GameSettings {
    private final static String DEFAULT_PLAYER_01_COLOR = "FF0000";
    private final static String DEFAULT_PLAYER_02_COLOR = "0000FF";

    private final static int DEFAULT_BRIGHTNESS = 255;

    private final static int PLAYER_01SYMBOL_DEFAULT = R.drawable.ic_game_symbol_01;
    private final static int PLAYER_02SYMBOL_DEFAULT = R.drawable.ic_game_symbol_02;

    private static GameSettings INSTANCE = null;

    private int mBrightness = DEFAULT_BRIGHTNESS;

    private String mPlayer_01Color = DEFAULT_PLAYER_01_COLOR;
    private String mPlayer_02Color = DEFAULT_PLAYER_02_COLOR;

    private int mPlayer_01Symbol = PLAYER_01SYMBOL_DEFAULT;
    private int mPlayer_02Symbol = PLAYER_02SYMBOL_DEFAULT;

    public static synchronized GameSettings getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GameSettings();
        }
        return INSTANCE;
    }

    public void setPlayer_01Color(String mPlayer_01Color) {
        this.mPlayer_01Color = mPlayer_01Color;
    }

    public String getPlayer_01Color() {
        return mPlayer_01Color;
    }

    public void setLedBrightness(int brightness) {
        this.mBrightness = brightness;
    }

    public int getLedBrightness() {
        return mBrightness;
    }

    public int getPlayer_01Symbol() {
        return mPlayer_01Symbol;
    }

    public void setPlayer_01Symbol(int mPlayer_01Symbol) {
        this.mPlayer_01Symbol = mPlayer_01Symbol;
    }

    public int getPlayer_02Symbol() {
        return mPlayer_02Symbol;
    }

    public void setPlayer_02Symbol(int mPlayer_02Symbol) {
        this.mPlayer_02Symbol = mPlayer_02Symbol;
    }

    public String getPlayer_02Color() {
        return mPlayer_02Color;
    }

    public void setPlayer_02Color(String mPlayer_02Color) {
        this.mPlayer_02Color = mPlayer_02Color;
    }
}