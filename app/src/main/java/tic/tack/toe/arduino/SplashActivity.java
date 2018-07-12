package tic.tack.toe.arduino;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static tic.tack.toe.arduino.Constants.TAG;

public class SplashActivity extends AppCompatActivity implements Runnable {

    private static final int REQUEST_LOCATION_ACCESS = 1000;
    private static final int REQUEST_ENABLE_BT = 1001;

    private final Handler mHandler = new Handler();

    private BluetoothAdapter mBluetoothAdapter;

    private boolean mDeviceFound = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        Timber.plant(new Timber.DebugTree());

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            finish();
            return;
        }

        BluetoothManager bluetoothManager = (BluetoothManager)
                getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            this.mBluetoothAdapter = bluetoothManager.getAdapter();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.mBluetoothAdapter == null) {
            return;
        }

        if (!this.mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkPermissions()) {
            requestPermissions();
            return;
        }

        startScanning();
    }

    private void startScanning() {
        Timber.tag(TAG).e("startScanning");

        this.mDeviceFound = false;
        List<ScanFilter> filters = new ArrayList<>();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(1000)
                .build();

        BluetoothLeScanner bluetoothLeScanner = this.mBluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(filters, settings, mScanCallback);
    }

    private void stopScanning() {
        Timber.tag(TAG).e("stopScanning");

        if (this.mBluetoothAdapter != null) {
            BluetoothLeScanner bluetoothLeScanner = this.mBluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.stopScan(this.mScanCallback);
        }
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
            Toast.makeText(SplashActivity.this, address, Toast.LENGTH_LONG).show();
            if (address.equals(MainActivity.MAC_ADDRESS) && !mDeviceFound) {
                mHandler.postDelayed(SplashActivity.this, 3000);
                mDeviceFound = true;
                stopScanning();
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        this.mHandler.removeCallbacksAndMessages(null);
        stopScanning();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_LOCATION_ACCESS);
    }

    private boolean checkPermissions() {
        int accessFineLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int accessCorseLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return accessFineLocation == PackageManager.PERMISSION_GRANTED
                && accessCorseLocation == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_ACCESS:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void run() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
