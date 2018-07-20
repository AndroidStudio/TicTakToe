package tic.tack.toe.arduino.splash;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;

import org.json.JSONObject;

import java.util.Objects;

import tic.tack.toe.arduino.R;
import tic.tack.toe.arduino.ScanActivity;
import tic.tack.toe.arduino.dialog.MessageDialog;
import tic.tack.toe.arduino.fragments.BaseFragment;
import tic.tack.toe.arduino.game.GameSettings;
import tic.tack.toe.arduino.sockets.UDID;
import tic.tack.toe.arduino.sockets.WebSocketConstants;
import timber.log.Timber;

public class InitFragment extends BaseFragment {

    private static final String TAG = "InitFragment";
    private ProgressDialog mInitDeviceProgressDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isInternetConnection()) {
            displayNoInternetDialog();
            return;
        }
        initDevice();
    }

    private void displayNoInternetDialog() {
        AlertDialog alertDialog = MessageDialog.displayDialog(getActivity(),
                getString(R.string.no_internet_connection));
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Objects.requireNonNull(getActivity()).finish();
            }
        });
    }

    private void initDevice() {
        displayInitDeviceProgressDialog();
        try {
            JSONObject messageObject = new JSONObject();
            messageObject.put(WebSocketConstants.UDID, UDID.getUDID());
            messageObject.put(WebSocketConstants.INIT_GAME, true);
            setMessage(messageObject);

            Timber.tag(TAG).e("initDevice");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONObject messageObject = new JSONObject(message);
            if (messageObject.has(WebSocketConstants.BLUETOOTH_ADDRESS)) {
                String macAddress = messageObject.getString(WebSocketConstants.BLUETOOTH_ADDRESS);
                GameSettings.getInstance().setMacAddress(macAddress);
                initDeviceSuccess();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayInitDeviceProgressDialog() {
        if (this.mInitDeviceProgressDialog != null && this.mInitDeviceProgressDialog.isShowing()) {
            return;
        }

        this.mInitDeviceProgressDialog = new ProgressDialog(getActivity());
        this.mInitDeviceProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    Objects.requireNonNull(getActivity()).finish();
                }
                return true;
            }
        });
        this.mInitDeviceProgressDialog.setCanceledOnTouchOutside(false);
        this.mInitDeviceProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.mInitDeviceProgressDialog.setMessage("Inicjalizacja urządzenia,\nUDID: " + UDID.getUDID());
        this.mInitDeviceProgressDialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        hideInitDeviceProgressDialog();
    }

    public void hideInitDeviceProgressDialog() {
        if (this.mInitDeviceProgressDialog != null && this.mInitDeviceProgressDialog.isShowing()) {
            this.mInitDeviceProgressDialog.dismiss();
        }

    }

    private void initDeviceSuccess() {
        Timber.tag(TAG).e("initDeviceSuccess");

        Intent intent = new Intent(getActivity(), ScanActivity.class);
        startActivity(intent);
    }

    @SuppressWarnings("all")
    public boolean isInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity()
                .getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        NetworkInfo wifiNetwork = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            return true;
        }

        NetworkInfo mobileNetwork = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnected()) {
            return true;
        }

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }
        return false;
    }
}
