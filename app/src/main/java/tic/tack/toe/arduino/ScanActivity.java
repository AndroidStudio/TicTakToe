package tic.tack.toe.arduino;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
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
    private final Handler scanningHandler = new Handler();
    private final Handler handler = new Handler();

    private BluetoothAdapter mBluetoothAdapter;
    private ProgressDialog mScanDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.scan_activity);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            this.startMainActivity();
            return;
        }

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            this.mBluetoothAdapter = bluetoothManager.getAdapter();
        }
    }

    private void startMainActivity() {
        if (this.mMessageDialog != null && this.mMessageDialog.isShowing()) {
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(InitDeviceActivity.LED_DIAGNOSTICS,
                getIntent().getBooleanExtra(InitDeviceActivity.LED_DIAGNOSTICS, false));
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

        if (TextUtils.isEmpty(GameSettings.getInstance().getMacAddress(this))) {
            this.startMainActivity();
            return;
        }

        this.startScanning();
    }

    private void startScanning() {
        Timber.tag(TAG).e("startScanning");
        BluetoothLeScanner bluetoothLeScanner = this.mBluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(this.mScanCallback);
        displayScanDialog();

        scanningHandler.removeCallbacksAndMessages(null);
        scanningHandler.postDelayed(() -> {
            hideScanDialog();
            startMainActivity();
        }, 10000);
    }

    private void stopScanning() {
        Timber.tag(TAG).e("stopScanning");
        scanningHandler.removeCallbacksAndMessages(null);

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
        this.mScanDialog.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                finish();
                System.exit(0);
            }
            return true;
        });
        this.mScanDialog.setCanceledOnTouchOutside(false);
        this.mScanDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.mScanDialog.setMessage("Skanowanie");
        this.mScanDialog.show();
    }

    public void hideScanDialog() {
        this.runOnUiThread(() -> {
            if (mScanDialog != null && mScanDialog.isShowing()) {
                mScanDialog.dismiss();
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
            if (address.equals(GameSettings.getInstance().getMacAddress(ScanActivity.this))) {
                handler.postDelayed(ScanActivity.this, 3000);
                stopScanning();
                Toast.makeText(ScanActivity.this, "Urządzenie zostało odnalezione",
                        Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        this.scanningHandler.removeCallbacksAndMessages(null);
        this.handler.removeCallbacksAndMessages(null);
        this.stopScanning();
    }

    @Override
    public void run() {
        this.startMainActivity();
    }
}
