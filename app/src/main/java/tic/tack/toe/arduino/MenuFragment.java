package tic.tack.toe.arduino;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.util.Objects;

import tic.tack.toe.arduino.fragments.BaseFragment;
import tic.tack.toe.arduino.sockets.SocketConstants;
import tic.tack.toe.arduino.sockets.UDID;
import timber.log.Timber;

public class MenuFragment extends BaseFragment {

    private static final String TAG = "MenuFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.finishButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectClient();
                ActivityCompat.finishAffinity(Objects.requireNonNull(getActivity()));
                System.exit(0);
            }
        });
    }

    public void disconnectClient() {
        Timber.tag(TAG).e("disconnectClient");

        try {
            JSONObject object = new JSONObject();
            object.put(SocketConstants.TYPE, SocketConstants.EXIT_GAME);
            object.put(SocketConstants.UDID, UDID.getUDID());
            setMessage(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
