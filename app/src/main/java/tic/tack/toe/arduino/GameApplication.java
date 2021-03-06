package tic.tack.toe.arduino;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.view.KeyEvent;

import com.splunk.mint.Mint;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tic.tack.toe.arduino.sockets.ForegroundService;
import tic.tack.toe.arduino.sockets.MessageListener;
import tic.tack.toe.arduino.sockets.SocketConstants;
import tic.tack.toe.arduino.sockets.SocketManager;
import tic.tack.toe.arduino.sockets.UDID;
import timber.log.Timber;

public class GameApplication extends Application implements MessageListener {

    private SocketManager socketManager;

    private List<MessageListener> mMessageListenerList = new ArrayList<>();

    private static final String TAG = "GameApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        socketManager = new SocketManager(this);

        Timber.tag(TAG).e("onMessage");
        this.initTimber();
        this.initUDID();

        Mint.initAndStartSession(this, "4d92ba79");
        Mint.disableNetworkMonitoring();
        Mint.flush();

        Intent intent = new Intent(this, ForegroundService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    public void startSocket() {
        socketManager.setMessageListener(this);
        socketManager.start();
    }

    private void initUDID() {
        UDID.init(this);
        Timber.tag(TAG).e("UDID: %s", UDID.getUDID());
    }

    private void initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    public void addMessageListener(MessageListener messageListener) {
        this.mMessageListenerList.add(messageListener);
    }

    public void sendMessage(String message) {
        this.socketManager.sendMessage(message);
    }

    @Override
    public void onMessage(final String message) {
        for (int i = 0; i < mMessageListenerList.size(); i++) {
            this.mMessageListenerList.get(i).onMessage(message);
        }
    }

    @Override
    public void onConnectionError() {
        for (int i = 0; i < mMessageListenerList.size(); i++) {
            this.mMessageListenerList.get(i).onConnectionError();
        }
    }

    public void removeMessageListener(MessageListener messageListener) {
        this.mMessageListenerList.remove(messageListener);
    }

    @SuppressWarnings("all")
    public void onKeyDown(int keyCode, KeyEvent event) {
        try {
            Timber.tag(TAG).e("onKeyDown %s: ", keyCode);
            switch (keyCode) {
                case 8:
                    initDevice();
                    break;
                case 9:
                    initSymbol();
                    break;
                case 10:
                    initLedColor();
                    break;
                case 11:
                    gameInfo();
                    break;
                case 12:
                    newGame();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Key code 1
     * */

    private void initDevice() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(SocketConstants.TYPE, SocketConstants.INIT_GAME);
        jsonObject.put(SocketConstants.BLUETOOTH_ADDRESS, Constants.EMPTY_STRING);

        onMessage(jsonObject.toString());
    }
    /*
     * Key code 2
     * */

    private void initSymbol() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(SocketConstants.TYPE, SocketConstants.SYMBOL);
        jsonObject.put(SocketConstants.SYMBOL, SocketConstants.OK);
        onMessage(jsonObject.toString());
    }
    /*
     * Key code 3
     * */

    private void initLedColor() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(SocketConstants.TYPE, SocketConstants.LED_SETTINGS);
        jsonObject.put(SocketConstants.COLOR, SocketConstants.OK);
        onMessage(jsonObject.toString());
    }
    /*
     * Key code 4
     * */

    private void gameInfo() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(SocketConstants.TYPE, SocketConstants.GAME_INFO);
        jsonObject.put(SocketConstants.STATUS, SocketConstants.OK);
        jsonObject.put(SocketConstants.CURRENT_PLAYER, UDID.getUDID());

        JSONArray infoArray = new JSONArray();
        JSONObject player_01Object = new JSONObject();
        player_01Object.put(SocketConstants.UDID, UDID.getUDID());
        player_01Object.put(SocketConstants.COLOR, "0000FF");
        player_01Object.put(SocketConstants.SYMBOL, 0);
        infoArray.put(player_01Object);

        JSONObject player_02Object = new JSONObject();
        player_02Object.put(SocketConstants.UDID, "PLAYER_02");
        player_02Object.put(SocketConstants.COLOR, "FF0000");
        player_02Object.put(SocketConstants.SYMBOL, 3);
        infoArray.put(player_02Object);

        JSONArray gameBoardArray = new JSONArray();
        gameBoardArray.put(1);
        gameBoardArray.put(1);
        gameBoardArray.put(0);
        gameBoardArray.put(0);
        gameBoardArray.put(0);
        gameBoardArray.put(0);
        gameBoardArray.put(2);
        gameBoardArray.put(2);
        gameBoardArray.put(0);

        jsonObject.put(SocketConstants.GAME_BOARD, gameBoardArray);

        jsonObject.put(SocketConstants.INFO, infoArray);
        onMessage(jsonObject.toString());
    }

    /*
     * Key code 5
     * */
    private void newGame() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(SocketConstants.TYPE, SocketConstants.NEW_GAME);
        jsonObject.put(SocketConstants.UDID, UDID.getUDID());
        onMessage(jsonObject.toString());
    }

    public void startPing() {
        socketManager.startPing();
    }
}
