package tic.tack.toe.arduino.game;

public class CMD {

    public static final String BRIGHTNESS = "42";//ustawia jasność diody
    public static final String SET_PIXEL = "50";//zapala diodę + index + kolor
    public static final String RESET_ALL = "52";//resetuje diody
    public static final String RESET_PIXEL = "44";//reset diody + index (np 4401)

}
