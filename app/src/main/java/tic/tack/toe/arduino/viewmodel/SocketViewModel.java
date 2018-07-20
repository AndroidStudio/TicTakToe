package tic.tack.toe.arduino.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import tic.tack.toe.arduino.sockets.MessageListener;
import tic.tack.toe.arduino.sockets.WebSocketManager;

public class SocketViewModel extends AndroidViewModel implements MessageListener {

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
}
