package tic.tack.toe.arduino;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;

import org.json.JSONObject;

import tic.tack.toe.arduino.dialog.InputMACDialog;
import tic.tack.toe.arduino.dialog.MessageDialog;
import tic.tack.toe.arduino.sockets.SocketConstants;
import tic.tack.toe.arduino.sockets.UDID;
import timber.log.Timber;

public class InitDeviceActivity extends BaseActivity {

    private static final String TAG = "InitDeviceActivity";

    private ProgressDialog mInitDeviceProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.splash_activity);
        if (!this.isInternetConnection()) {
            this.displayNoInternetDialog();
            return;
        }
        this.initDevice();
    }

    private void displayNoInternetDialog() {
        AlertDialog alertDialog = MessageDialog.displayDialog(this,
                getString(R.string.no_internet_connection));
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
                System.exit(0);
            }
        });
    }

    private void initDevice() {
        this.displayInitDeviceProgressDialog();
        startSocket();
    }

    @Override
    public void onMessage(String message) {
        super.onMessage(message);
        try {
            JSONObject messageObject = new JSONObject(message);
            switch (messageObject.getString(SocketConstants.TYPE)) {
                case SocketConstants.INIT_GAME:
                    this.initDeviceSuccess();
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
                try {
                    mInitDeviceProgressDialog.dismiss();
                    finish();
                    System.exit(0);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        this.mInitDeviceProgressDialog.setCanceledOnTouchOutside(false);
        this.mInitDeviceProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.mInitDeviceProgressDialog.setMessage("Oczekiwanie na przeciwnika");
        this.mInitDeviceProgressDialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.hideInitDeviceProgressDialog();
    }

    public void hideInitDeviceProgressDialog() {
        if (this.mInitDeviceProgressDialog != null && this.mInitDeviceProgressDialog.isShowing()) {
            this.mInitDeviceProgressDialog.dismiss();
        }
    }

    private void initDeviceSuccess() {
        try {
            mInitDeviceProgressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Timber.tag(TAG).e("initDeviceSuccess");
        InputMACDialog macDialog = new InputMACDialog(this);
        macDialog.show();
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
