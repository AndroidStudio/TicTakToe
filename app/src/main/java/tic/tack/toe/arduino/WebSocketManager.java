package tic.tack.toe.arduino;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketManager extends WebSocketListener {

    private static final int NORMAL_CLOSURE_STATUS = 1000;

    public void start() {
        OkHttpClient  client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://echo.websocket.org").build();
        client.newWebSocket(request, this);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        webSocket.send("Hello, it's SSaurel !");
        webSocket.send("What's up ?");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {

    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {

    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {

    }
}

