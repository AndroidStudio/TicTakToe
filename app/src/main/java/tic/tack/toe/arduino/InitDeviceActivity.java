package tic.tack.toe.arduino;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import org.json.JSONObject;

import tic.tack.toe.arduino.dialog.InputMACDialog;
import tic.tack.toe.arduino.dialog.MessageDialog;
import tic.tack.toe.arduino.game.GameSettings;
import tic.tack.toe.arduino.sockets.SocketConstants;
import timber.log.Timber;

public class InitDeviceActivity extends BaseActivity {

    private static final String TAG = "InitDeviceActivity";
    public static final String LED_DIAGNOSTICS = "led_diagnostics";

    private ProgressDialog mInitDeviceProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.splash_activity);

        findViewById(R.id.ledDiagnostics).setOnClickListener(v -> initDeviceSuccess(true));

        findViewById(R.id.startGame).setOnClickListener(v -> {
            if (!isInternetConnection()) {
                displayNoInternetDialog();
                return;
            }
            initDevice();
        });

    }

    private void displayNoInternetDialog() {
        AlertDialog alertDialog = MessageDialog.displayDialog(this,
                getString(R.string.no_internet_connection));
        alertDialog.setOnDismissListener(dialog -> {
            finish();
            System.exit(0);
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
                    this.initDeviceSuccess(false);
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
        this.mInitDeviceProgressDialog.setOnKeyListener((dialog, keyCode, event) -> {
            try {
                mInitDeviceProgressDialog.dismiss();
                finish();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
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

    private void initDeviceSuccess(boolean ledDiagnostics) {
        GameApplication gameApplication = (GameApplication) getApplication();
        gameApplication.startPing();

        try {
            mInitDeviceProgressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (this.mMessageDialog != null && this.mMessageDialog.isShowing()) {
            return;
        }

        String mac = GameSettings.getInstance().getMacAddress(this);
        if (TextUtils.isEmpty(mac)) {
            InputMACDialog macDialog = new InputMACDialog(this,ledDiagnostics);
            macDialog.show();
        } else {
            GameSettings.getInstance().setMacAddress(mac, this);
            Intent intent = new Intent(this, ScanActivity.class);
            intent.putExtra(LED_DIAGNOSTICS, ledDiagnostics);
            startActivity(intent);
            finish();
        }

        Timber.tag(TAG).e("initDeviceSuccess");
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
