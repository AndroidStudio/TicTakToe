package tic.tack.toe.arduino.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.view.KeyEvent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tic.tack.toe.arduino.Constants;
import tic.tack.toe.arduino.sockets.MessageListener;
import tic.tack.toe.arduino.sockets.SocketConstants;
import tic.tack.toe.arduino.sockets.UDID;
import tic.tack.toe.arduino.sockets.WebSocketManager;
import timber.log.Timber;

public class SocketViewModel extends AndroidViewModel implements MessageListener {
    private static final String TAG = "SocketViewModel";

    private final WebSocketManager mWebSocketManager = new WebSocketManager();

    private List<MessageListener> mMessageListenerList = new ArrayList<>();

    public SocketViewModel(@NonNull Application application) {
        super(application);
        this.mWebSocketManager.setMessageListener(this);
    }

    public void addMessageListener(MessageListener messageListener) {
        this.mMessageListenerList.add(messageListener);
    }

    public void sendMessage(String message) {
        this.mWebSocketManager.sendMessage(message);
    }

    @Override
    public void onMessage(String message) {
        for (int i = 0; i < this.mMessageListenerList.size(); i++) {
            this.mMessageListenerList.get(i).onMessage(message);
        }
    }

    public void removeMessageListener(MessageListener messageListener) {
        this.mMessageListenerList.remove(messageListener);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.mMessageListenerList.clear();
    }

    public void disconnectClient() {
        if (this.mWebSocketManager != null) {
            this.mWebSocketManager.disconnectClient();
        }
    }

    @SuppressWarnings("all")
    public void onKeyDown(int keyCode, KeyEvent event) {
        try {
            Timber.tag(TAG).e("onKeyDown %s: ", keyCode);
            switch (keyCode) {
                case 8: {
                    initDevice();
                    break;
                }
                case 9: {
                    initSymbol();
                    break;
                }
                case 10: {
                    initLedColor();
                    break;
                }
                case 11: {
                    gameInfo();
                    break;
                }
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
        jsonObject.put(SocketConstants.CURRENT_PLAYER, "PLAYER_02");

        JSONArray infoArray = new JSONArray();
        JSONObject player_01Object = new JSONObject();
        player_01Object.put(SocketConstants.UDID, UDID.getUDID());
        player_01Object.put(SocketConstants.COLOR, "0000FF");
        player_01Object.put(SocketConstants.SYMBOL, 1);
        infoArray.put(player_01Object);

        JSONObject player_02Object = new JSONObject();
        player_02Object.put(SocketConstants.UDID, UDID.getUDID());
        player_02Object.put(SocketConstants.COLOR, "FF0000");
        player_02Object.put(SocketConstants.SYMBOL, 3);
        infoArray.put(player_02Object);

        onMessage(jsonObject.toString());
    }
}
