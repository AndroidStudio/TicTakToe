package tic.tack.toe.arduino.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

import tic.tack.toe.arduino.MainActivity;
import tic.tack.toe.arduino.R;
import tic.tack.toe.arduino.bluetooth.BleManager;
import tic.tack.toe.arduino.game.CMD;

public class MenuFragment extends BaseFragment {

    private AlertDialog closeGameDialog;

    private boolean canLedTest = true;


    private final Handler ledTestHandler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        canLedTest = true;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity mainActivity = (MainActivity) getActivity();
        view.findViewById(R.id.finishButton).setOnClickListener(v -> {
            closeGameDialog = new AlertDialog.Builder(mainActivity)
                    .setTitle("Czy na pewno chcesz zakończyć grę")
                    .setPositiveButton("Tak",
                            (dialog, whichButton) -> {
                                mainActivity.disconnectClient();
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
            if (canLedTest) {
                writeMessage(hexStringToByteArray(CMD.RESET));
                canLedTest = false;

                ledTestHandler.removeCallbacksAndMessages(null);
                ledTestHandler.postDelayed(() -> canLedTest = true, 2000);

                MainActivity activity = (MainActivity) getActivity();
                activity.closeMenu();

                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .add(R.id.contentLayout, new LedTest(), null)
                        .commit();
            }
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
        if (closeGameDialog != null) {
            closeGameDialog.dismiss();
        }
    }

    private BleManager getBleManager() {
        return ((MainActivity) Objects.requireNonNull(getActivity())).bleManager;
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
}
