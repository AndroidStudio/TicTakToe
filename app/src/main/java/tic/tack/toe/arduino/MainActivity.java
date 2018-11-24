package tic.tack.toe.arduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Locale;
import java.util.Objects;

import tic.tack.toe.arduino.bluetooth.BleManager;
import tic.tack.toe.arduino.fragments.FragmentController;
import tic.tack.toe.arduino.fragments.GameSymbolFragment;
import tic.tack.toe.arduino.fragments.LedTest;
import tic.tack.toe.arduino.fragments.MenuFragment;
import tic.tack.toe.arduino.game.CMD;
import tic.tack.toe.arduino.game.GameSettings;
import tic.tack.toe.arduino.sockets.SocketConstants;
import tic.tack.toe.arduino.sockets.UDID;
import timber.log.Timber;

import static tic.tack.toe.arduino.Constants.TAG;

public class MainActivity extends BaseActivity {
    private final Handler handler = new Handler();

    private ImageView mBluetoothStatusImageView;
    private ProgressBar mProgressBar;

    public BleManager bleManager;
    public TextView mMacAddressTextView;

    public int[] mFieldBluetoothIndexArray = new int[]{8, 7, 6, 3, 4, 5, 2, 1, 0};

    private final GameSettings mGameSettings = GameSettings.getInstance();
    private AlertDialog closeGameDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        Timber.tag(Constants.TAG).e("onCreate");

        this.mProgressBar = findViewById(R.id.progressBar);
        this.mBluetoothStatusImageView = findViewById(R.id.bluetoothStatusImageView);
        this.mBluetoothStatusImageView.setOnClickListener(v -> reconnectClick());

        this.bleManager = BleManager.getInstance(this);
        this.bleManager.setBleListener(this.bleCallbacks);

        String macAddress = GameSettings.getInstance().getMacAddress(this);
        this.bleManager.connect(MainActivity.this, macAddress);

        if (savedInstanceState == null) {
            this.setupMenuFragment();
            FragmentController.setCurrentFragment(this, new GameSymbolFragment(), false);

            if (getIntent().getBooleanExtra(InitDeviceActivity.LED_DIAGNOSTICS, false)) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .add(R.id.contentLayout, new LedTest(), null)
                        .commit();
            }
        }

        this.mMacAddressTextView = findViewById(R.id.macAddressTextView);
        this.mMacAddressTextView.setText("Bluetooth: "
                + GameSettings.getInstance().getMacAddress(this) + "\n" + getShirtName()
                + " (disconnected)");

        if (macAddress.equals("C4:2B:2D:00:FF:1D")) {
            mFieldBluetoothIndexArray = new int[]{8, 7, 6, 3, 4, 5, 2, 1, 0};
        } else {
            mFieldBluetoothIndexArray = new int[]{6, 7, 8, 5, 4, 3, 0, 1, 2};
        }
    }

    private String getShirtName() {
        String macAddress = GameSettings.getInstance().getMacAddress(this);
        if (TextUtils.isEmpty(macAddress)) {
            return "";
        }
        if (macAddress.equals("C4:2B:2D:00:FF:1D")) {
            return "Koszulka A";
        } else {
            return "Koszulka B";
        }
    }

    private void reconnectClick() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(
                Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null) {
                return;
            }

            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                return;
            }

            if (!checkPermissions()) {
                this.requestPermissions();
                return;
            }

            connectBle();
        }
    }

    private void connectBle() {
        this.bleManager.connect(MainActivity.this, GameSettings.getInstance()
                .getMacAddress(this));
    }

    private final BleManager.BleManagerListener bleCallbacks = new BleManager.BleManagerListener() {

        @Override
        public void onConnected() {
            Timber.tag(TAG).e("onConnected");
            updateBluetoothStatusUI(true);
            handler.postDelayed(() -> writeMessage(hexStringToByteArray(CMD.RESET)), 1000);
        }

        @Override
        public void onConnecting() {
            Timber.tag(TAG).e("onConnecting");
            runOnUiThread(() -> mProgressBar.setVisibility(View.VISIBLE));
        }

        @Override
        public void onDisconnected() {
            Timber.tag(TAG).e("onDisconnected");
            updateBluetoothStatusUI(false);

            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(() -> connectBle(), 3000);
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
        if (closeGameDialog != null) {
            closeGameDialog.dismiss();
        }
        new Handler().postDelayed(() -> closeDrawerLayout(), 300);
        handler.removeCallbacksAndMessages(null);
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

        closeGameDialog = new AlertDialog.Builder(this)
                .setTitle("Czy na pewno chcesz zakończyć grę")
                .setPositiveButton("Tak",
                        (dialog, whichButton) -> {
                            disconnectClient();
                            writeMessage(hexStringToByteArray(CMD.RESET));
                            ActivityCompat.finishAffinity(Objects.requireNonNull(this));
                            System.exit(0);
                        }
                )
                .setNegativeButton("Nie",
                        (dialog, whichButton) -> dialog.dismiss()
                )
                .create();
        closeGameDialog.show();
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

    public void closeMenu() {
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void updateBluetoothStatusUI(final boolean isConnected) {
        this.runOnUiThread(() -> {
            try {
                if (isConnected) {
                    mBluetoothStatusImageView.setImageResource(R.drawable.ic_bluetooth_connected);
                    mMacAddressTextView.setText("Bluetooth: "
                            + GameSettings.getInstance().getMacAddress(this) + "\n" + getShirtName()
                            + " (connected)");
                } else {
                    mBluetoothStatusImageView.setImageResource(R.drawable.ic_bluetooth_disconnected);
                    mMacAddressTextView.setText("Bluetooth: "
                            + GameSettings.getInstance().getMacAddress(this) + "\n" + getShirtName()
                            + " (disconnected)");
                }
                mProgressBar.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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

    public byte[] hexStringToByteArray(String value) {
        int length = value.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(value.charAt(i), 16) << 4)
                    + Character.digit(value.charAt(i + 1), 16));
        }
        return data;
    }

    public String toHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) stringBuilder.append(String.format("%x", b));
        return stringBuilder.toString();
    }

    public void writeMessage(byte[] message) {
        bleManager.writeService(bleManager.getGattService(
                "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"),
                "6E400002-B5A3-F393-E0A9-E50E24DCCA9E", message);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.bleManager.disconnect();
        this.bleManager.close();
    }
}
