package tic.tack.toe.arduino.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import tic.tack.toe.arduino.sockets.MessageListener;
import tic.tack.toe.arduino.sockets.WebSocketManager;

public class SocketViewModel extends AndroidViewModel {

    private final WebSocketManager mWebSocketManager = new WebSocketManager();

    public SocketViewModel(@NonNull Application application) {
        super(application);
        this.mWebSocketManager.start();
    }

    public void setMessageListener(MessageListener messageListener) {
        this.mWebSocketManager.setMessageListener(messageListener);
    }

    public void sendMessage(String message) {
        this.mWebSocketManager.sendMessage(message);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        this.mWebSocketManager.close();
    }
}
