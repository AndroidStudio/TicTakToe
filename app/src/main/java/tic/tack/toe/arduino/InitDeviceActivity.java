package tic.tack.toe.arduino;

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

import tic.tack.toe.arduino.dialog.MessageDialog;
import tic.tack.toe.arduino.game.GameSettings;
import tic.tack.toe.arduino.sockets.SocketConnectionListener;
import tic.tack.toe.arduino.sockets.SocketConstants;
import tic.tack.toe.arduino.sockets.UDID;
import timber.log.Timber;

public class InitDeviceActivity extends BaseActivity {

    private static final String TAG = "InitDeviceActivity";

    private ProgressDialog mInitDeviceProgressDialog;
    private AlertDialog mSocketConnectionDialog;

    private boolean mIsDeviceInitialized = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.splash_activity);
        if (!this.isInternetConnection()) {
            this.displayNoInternetDialog();
            return;
        }

        this.mGameApplication.setOpenSocketListener(new SocketConnectionListener() {

            @Override
            public void onSocketOpen() {
                Timber.tag(TAG).e("onSocketOpen");
                initDevice();
            }

            @Override
            public void onSocketFailure() {
                displaySocketConnectionErrorDialog();
            }

            @Override
            public void onSocketClose() {
                displaySocketConnectionErrorDialog();
            }
        });
    }

    private void displaySocketConnectionErrorDialog() {
        this.mSocketConnectionDialog = MessageDialog.displayDialog(this,
                getString(R.string.blad_polaczenia_z_serwerem));
        this.mSocketConnectionDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
    }

    private void hideSocketConnectionErrorDialog() {
        if (this.mSocketConnectionDialog != null && this.mSocketConnectionDialog.isShowing()) {
            this.mSocketConnectionDialog.dismiss();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (this.mGameApplication.isSocketOpen()) {
            this.initDevice();
        } else {
            this.mGameApplication.openSocket();
        }
    }

    private void displayNoInternetDialog() {
        AlertDialog alertDialog = MessageDialog.displayDialog(this,
                getString(R.string.no_internet_connection));
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
    }

    private void initDevice() {
        this.displayInitDeviceProgressDialog();
        try {
            JSONObject messageObject = new JSONObject();
            messageObject.put(SocketConstants.TYPE, SocketConstants.INIT_GAME);
            messageObject.put(SocketConstants.UDID, UDID.getUDID());
            setMessage(messageObject);

            Timber.tag(TAG).e("initDevice");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(String message) {
        super.onMessage(message);
        try {
            JSONObject messageObject = new JSONObject(message);
            switch (messageObject.getString(SocketConstants.TYPE)) {
                case SocketConstants.INIT_GAME:
                    String macAddress = messageObject.getString(SocketConstants.BLUETOOTH_ADDRESS);
                    this.initDeviceSuccess(macAddress);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayInitDeviceProgressDialog() {
        if (this.mInitDeviceProgressDialog != null && this.mInitDeviceProgressDialog.isShowing()) {
            return;
        }

        this.mInitDeviceProgressDialog = new ProgressDialog(this);
        this.mInitDeviceProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                onKeyDown(keyCode, event);
                return true;
            }
        });
        this.mInitDeviceProgressDialog.setCanceledOnTouchOutside(false);
        this.mInitDeviceProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.mInitDeviceProgressDialog.setMessage("Inicjalizacja urządzenia,\nUDID: " + UDID.getUDID());
        this.mInitDeviceProgressDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.hideSocketConnectionErrorDialog();
        this.hideInitDeviceProgressDialog();
    }

    public void hideInitDeviceProgressDialog() {
        if (this.mInitDeviceProgressDialog != null && this.mInitDeviceProgressDialog.isShowing()) {
            this.mInitDeviceProgressDialog.dismiss();
        }
    }

    private void initDeviceSuccess(String macAddress) {
        if (this.mIsDeviceInitialized) {
            return;
        }

        Timber.tag(TAG).e("initDeviceSuccess");

        GameSettings.getInstance().setMacAddress(macAddress);
        Intent intent = new Intent(this, ScanActivity.class);
        this.startActivity(intent);
        this.finish();

        this.mIsDeviceInitialized = true;
    }

    @SuppressWarnings("all")
    public boolean isInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplication()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
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
