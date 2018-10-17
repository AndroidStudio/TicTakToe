package tic.tack.toe.arduino;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import tic.tack.toe.arduino.game.GameSettings;
import timber.log.Timber;

import static tic.tack.toe.arduino.Constants.TAG;

public class ScanActivity extends BaseActivity implements Runnable {

    private final Handler mHandler = new Handler();

    private BluetoothAdapter mBluetoothAdapter;
    private ProgressDialog mScanDialog;

    private boolean mDeviceFound = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.splash_activity);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            this.startMainActivity();
            return;
        }

        BluetoothManager bluetoothManager = (BluetoothManager)
                getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            this.mBluetoothAdapter = bluetoothManager.getAdapter();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.mBluetoothAdapter == null) {
            return;
        }

        if (!this.mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        if (!checkPermissions()) {
            this.requestPermissions();
            return;
        }

        if (TextUtils.isEmpty(GameSettings.getInstance().getMacAddress())) {
            this.startMainActivity();
            return;
        }

        this.startScanning();
    }

    private void startScanning() {
        Timber.tag(TAG).e("startScanning");

        this.mDeviceFound = false;

        BluetoothLeScanner bluetoothLeScanner = this.mBluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(this.mScanCallback);
        this.displayScanDialog();
    }

    private void stopScanning() {
        Timber.tag(TAG).e("stopScanning");

        if (this.mBluetoothAdapter != null) {
            BluetoothLeScanner bluetoothLeScanner = this.mBluetoothAdapter.getBluetoothLeScanner();
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(this.mScanCallback);
            }
        }
        this.hideScanDialog();
    }

    public void displayScanDialog() {
        if (this.mScanDialog != null && this.mScanDialog.isShowing()) {
            return;
        }

        this.mScanDialog = new ProgressDialog(this);
        this.mScanDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    finish();
                }
                return true;
            }
        });
        this.mScanDialog.setCanceledOnTouchOutside(false);
        this.mScanDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.mScanDialog.setMessage("Wyszukiwanie urządzenia MAC: " + GameSettings
                .getInstance().getMacAddress());
        this.mScanDialog.show();
    }

    public void hideScanDialog() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mScanDialog != null && mScanDialog.isShowing()) {
                    mScanDialog.dismiss();
                }
            }
        });
    }

    private final ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Timber.tag(TAG).e("onScanFailed: %s", errorCode);
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            String address = result.getDevice().getAddress();
            Timber.tag(TAG).e("Device found address: %s", address);
            if (address.equals(GameSettings.getInstance().getMacAddress()) && !mDeviceFound) {
                mHandler.postDelayed(ScanActivity.this, 3000);
                mDeviceFound = true;
                stopScanning();
                Toast.makeText(ScanActivity.this, "Urządzenie zostało odnalezione",
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        this.mHandler.removeCallbacksAndMessages(null);
        this.stopScanning();
    }

    @Override
    public void run() {
        this.startMainActivity();
    }
}
