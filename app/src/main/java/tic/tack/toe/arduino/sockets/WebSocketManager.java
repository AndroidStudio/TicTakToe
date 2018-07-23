package tic.tack.toe.arduino.sockets;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import timber.log.Timber;

public class WebSocketManager extends WebSocketListener {
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private static final String TAG = "WebSocketManager";

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final OkHttpClient mClient = new OkHttpClient();

    private OpenSocketListener mOpenSocketListener = null;
    private MessageListener mMessageListener = null;

    private WebSocket mWebSocket;

    public void start() {
        Request request = new Request.Builder()
                .url("ws://192.168.1.28:9696")
                .build();
        this.mClient.newWebSocket(request, this);
    }

    public void setMessageListener(MessageListener messageListener) {
        this.mMessageListener = messageListener;
    }

    public void setOpenSocketListener(OpenSocketListener openSocketListener) {
        this.mOpenSocketListener = openSocketListener;
    }

    public boolean isSocketOpen() {
        return this.mWebSocket != null;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        this.mWebSocket = webSocket;
        Timber.tag(TAG).e("onOpen");

        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOpenSocketListener != null) {
                    mOpenSocketListener.onSocketOpen();
                }
            }
        });
    }

    @Override
    public void onMessage(WebSocket webSocket, final String text) {
        Timber.tag(TAG).e("onMessage %s", text);

        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mMessageListener != null) {
                    mMessageListener.onMessage(text);
                }
            }
        });
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        //empty
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        Timber.tag(TAG).e("onClosing");
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Timber.tag(TAG).e("onFailure %s", t.getMessage());
    }

    public void sendMessage(String message) {
        Timber.tag(TAG).e("sendMessage %s", message);

        if (this.mWebSocket != null) {
            this.mWebSocket.send(message);
        }
    }

    private void closeConnection() {
        if (this.mWebSocket != null) {
            this.mWebSocket.close(NORMAL_CLOSURE_STATUS, "Client close request");
            Timber.tag(TAG).e("closeConnection");
        }
    }

    public void disconnectClient() {
        Timber.tag(TAG).e("disconnectClient");

        try {
            JSONObject object = new JSONObject();
            object.put(SocketConstants.TYPE, SocketConstants.EXIT_GAME);
            object.put(SocketConstants.UDID, UDID.getUDID());
            sendMessage(object.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.closeConnection();
    }
}

