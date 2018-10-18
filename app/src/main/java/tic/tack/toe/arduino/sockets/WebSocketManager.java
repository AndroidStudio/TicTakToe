package tic.tack.toe.arduino.sockets;

import android.os.Handler;
import android.os.Looper;

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
    private static final String API_URL = "ws://graox.dev2.wcstd.net:9700";
    // private static final String API_URL = "ws://192.168.1.28:9696";

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final OkHttpClient mClient = new OkHttpClient();

    private SocketConnectionListener mSocketConnectionListener = null;
    private MessageListener mMessageListener = null;

    private WebSocket mWebSocket;

    public void start() {
        Request request = new Request.Builder()
                .url(API_URL)
                .build();
        this.mClient.newWebSocket(request, this);
    }

    public void setMessageListener(MessageListener messageListener) {
        this.mMessageListener = messageListener;
    }

    public void setOpenSocketListener(SocketConnectionListener socketConnectionListener) {
        this.mSocketConnectionListener = socketConnectionListener;
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
                if (mSocketConnectionListener != null) {
                    mSocketConnectionListener.onSocketOpen();
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
        this.mWebSocket = null;

        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSocketConnectionListener != null) {
                    mSocketConnectionListener.onSocketClose();
                }
            }
        });
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        Timber.tag(TAG).e("onFailure %s", t.getMessage());
        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSocketConnectionListener != null) {
                    mSocketConnectionListener.onSocketFailure();
                }
            }
        });
    }

    public void sendMessage(String message) {
        Timber.tag(TAG).e("sendMessage %s", message);

        if (this.mWebSocket != null) {
            this.mWebSocket.send(message);
        }
    }
}

