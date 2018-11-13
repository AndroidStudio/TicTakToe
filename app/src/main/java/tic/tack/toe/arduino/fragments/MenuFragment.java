package tic.tack.toe.arduino.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;

import tic.tack.toe.arduino.MainActivity;
import tic.tack.toe.arduino.R;
import tic.tack.toe.arduino.bluetooth.BleManager;
import tic.tack.toe.arduino.game.CMD;
import tic.tack.toe.arduino.game.GameSettings;
import tic.tack.toe.arduino.sockets.SocketConstants;
import tic.tack.toe.arduino.sockets.UDID;
import timber.log.Timber;

public class MenuFragment extends BaseFragment {

    private static final String TAG = "MenuFragment";

    private final GameSettings mGameSettings = GameSettings.getInstance();

    private final int[] mFieldBluetoothIndexArray = new int[]{
            6, 7, 8, 5, 4, 3, 0, 1, 2
    };

    private AlertDialog closeGameDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.finishButton).setOnClickListener(v -> {
            closeGameDialog = new AlertDialog.Builder(getActivity())
                    .setTitle("Czy na pewno chcesz zakończyć grę")
                    .setPositiveButton("Tak",
                            (dialog, whichButton) -> {
                                disconnectClient();
                                writeMessage(hexStringToByteArray(CMD.RESET));
                                ActivityCompat.finishAffinity(Objects.requireNonNull(getActivity()));
                                System.exit(0);
                            }
                    )
                    .setNegativeButton("Nie",
                            (dialog, whichButton) -> dialog.dismiss()
                    )
                    .create();
            closeGameDialog.show();
        });

        view.findViewById(R.id.testButton).setOnClickListener(v -> {
            setPixel(0, 1);
            setPixel(1, 1);
            setPixel(2, 1);
            setPixel(3, 1);
            setPixel(4, 1);
            setPixel(5, 1);
            setPixel(6, 1);
            setPixel(7, 1);
            setPixel(8, 1);
            writeMessage(hexStringToByteArray(CMD.RESET));
        });

        view.findViewById(R.id.serverDiagnostic).setOnClickListener(v -> {
            MainActivity activity = (MainActivity) getActivity();
            activity.closeMenu();

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .add(R.id.contentLayout, new DiagnosticFragment(), null)
                    .commit();
        });


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        closeGameDialog.dismiss();
    }

    private BleManager getBleManager() {
        return ((MainActivity) Objects.requireNonNull(getActivity())).mBleManager;
    }

    public void setPixel(int index, int value) {
        Timber.tag(TAG).e("setPixelIndex %s", index);

        String indexHex = String.format(Locale.getDefault(),
                "%02d", this.mFieldBluetoothIndexArray[index]);
        String message = CMD.PIXEL + indexHex + (value == 1
                ? this.mGameSettings.getPlayer_01Color()
                : this.mGameSettings.getPlayer_02Color());
        Timber.tag(TAG).e("message %s", message);
        this.writeMessage(hexStringToByteArray(message));
    }

    private byte[] hexStringToByteArray(String value) {
        int length = value.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(value.charAt(i), 16) << 4)
                    + Character.digit(value.charAt(i + 1), 16));
        }
        return data;
    }

    private void writeMessage(byte[] message) {
        BleManager bleManager = this.getBleManager();
        bleManager.writeService(bleManager.getGattService(
                "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"),
                "6E400002-B5A3-F393-E0A9-E50E24DCCA9E", message);
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
