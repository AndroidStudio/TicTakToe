package tic.tack.toe.arduino.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.json.JSONObject;

import java.util.Objects;

import tic.tack.toe.arduino.GameApplication;
import tic.tack.toe.arduino.sockets.MessageListener;

public class BaseFragment extends Fragment implements MessageListener {

    private GameApplication mGameApplication;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mGameApplication = (GameApplication) Objects.requireNonNull(getActivity()).getApplication();
        this.mGameApplication.addMessageListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mGameApplication.removeMessageListener(this);
    }

    protected void setMessage(JSONObject message) {
        this.mGameApplication.sendMessage(message.toString());
    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onConnectionError() {

    }
}
