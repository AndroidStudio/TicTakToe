package tic.tack.toe.arduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import tic.tack.toe.arduino.bluetooth.BleManager;
import tic.tack.toe.arduino.fragments.FragmentController;
import tic.tack.toe.arduino.fragments.GameSymbolFragment;
import timber.log.Timber;

import static tic.tack.toe.arduino.Constants.MAC_ADDRESS;
import static tic.tack.toe.arduino.Constants.TAG;

public class MainActivity extends BaseActivity {

    private ImageView mBluetoothStatusImageView;
    private ProgressBar mProgressBar;

    public BleManager mBleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        Timber.tag(Constants.TAG).e("onCreate");

        this.mProgressBar = findViewById(R.id.progressBar);
        this.mBluetoothStatusImageView = findViewById(R.id.bluetoothStatusImageView);
        this.mBluetoothStatusImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reconnectClick();
            }
        });

        this.mBleManager = BleManager.getInstance(this);
        this.mBleManager.setBleListener(this.bleCallbacks);
        this.mBleManager.connect(MainActivity.this, MAC_ADDRESS);

        if (savedInstanceState == null) {
            setupMenuFragment();

            FragmentController.setCurrentFragment(this, new GameSymbolFragment());
        }
    }

    private void reconnectClick() {
        BluetoothManager bluetoothManager = (BluetoothManager)
                getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                return;
            }

            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                return;
            }

            if (!checkPermissions()) {
                requestPermissions();
                return;
            }

            this.mBleManager.connect(MainActivity.this, MAC_ADDRESS);
        }
    }

    private final BleManager.BleManagerListener bleCallbacks = new BleManager.BleManagerListener() {

        @Override
        public void onConnected() {
            Timber.tag(TAG).e("onConnected");
            updateBluetoothStatusUI(true);
        }

        @Override
        public void onConnecting() {
            Timber.tag(TAG).e("onConnecting");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public void onDisconnected() {
            Timber.tag(TAG).e("onDisconnected");
            updateBluetoothStatusUI(false);
        }

        @Override
        public void onServicesDiscovered() {
            Timber.tag(TAG).e("onServicesDiscovered");
        }

        @Override
        public void onDataAvailable(BluetoothGattCharacteristic characteristic) {
            Timber.tag(TAG).e("onDataAvailable");
        }

        @Override
        public void onDataAvailable(BluetoothGattDescriptor descriptor) {
            Timber.tag(TAG).e("onDataAvailable");
        }

        @Override
        public void onReadRemoteRssi(int rssi) {
            Timber.tag(TAG).e("onReadRemoteRssi");
        }
    };

    public void openMenuClick(View view) {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        if (drawerLayout != null) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                closeDrawerLayout();
            }
        }, 300);
    }

    public void setupMenuFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.menuContainer, new MenuFragment())
                .commitNow();
    }

    public void closeDrawerLayout() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    private void updateBluetoothStatusUI(final boolean isConnected) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isConnected) {
                        mBluetoothStatusImageView.setImageResource(R.drawable.ic_bluetooth_connected);
                    } else {
                        mBluetoothStatusImageView.setImageResource(R.drawable.ic_bluetooth_disconnected);
                    }
                    mProgressBar.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mBleManager.disconnect();
        this.mBleManager.close();
    }
}