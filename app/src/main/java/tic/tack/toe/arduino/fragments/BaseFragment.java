package tic.tack.toe.arduino.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import org.json.JSONObject;

import java.util.Objects;

import tic.tack.toe.arduino.sockets.MessageListener;
import tic.tack.toe.arduino.viewmodel.SocketViewModel;

public class BaseFragment extends Fragment implements MessageListener {

    private SocketViewModel mViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity()))
                .get(SocketViewModel.class);
        this.mViewModel.addMessageListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mViewModel.removeMessageListener(this);
    }

    protected void setMessage(JSONObject message) {
        this.mViewModel.sendMessage(message.toString());
    }

    @Override
    public void onMessage(String message) {

    }
}
