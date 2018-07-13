package tic.tack.toe.arduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import tic.tack.toe.arduino.bluetooth.BleManager;
import timber.log.Timber;

import static tic.tack.toe.arduino.Constants.MAC_ADDRESS;
import static tic.tack.toe.arduino.Constants.TAG;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String START_CHARACTER = "O";

    private static final String EMPTY = "";

    private final String[] fieldValueArray = new String[9];

    private String currentCharacter = START_CHARACTER;

    private BleManager bleManager;

    private ImageView bluetoothStatusImageView;
    private ProgressBar progressBar;
    private GridLayout gridLayout;

    private ImageView xImageView;
    private ImageView oImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        Timber.tag(Constants.TAG).e("onCreate");

        this.gridLayout = findViewById(R.id.gridLayout);
        int childCount = this.gridLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.fieldValueArray[i] = EMPTY;
            View view = this.gridLayout.getChildAt(i);
            view.setTag(i);
            view.setOnClickListener(this);
        }

        this.xImageView = findViewById(R.id.xImageView);
        this.oImageView = findViewById(R.id.oImageView);

        this.progressBar = findViewById(R.id.progressBar);
        this.bluetoothStatusImageView = findViewById(R.id.bluetoothStatusImageView);
        this.bluetoothStatusImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reconnectClick();
            }
        });

        this.bleManager = BleManager.getInstance(this);
        this.bleManager.setBleListener(this.bleCallbacks);
        this.bleManager.connect(MainActivity.this, MAC_ADDRESS);

        if (savedInstanceState == null) {
            setupMenuFragment();
        }
    }

    public void setLedType(String type) {
        String ledType = null;
        switch (type) {
            case "Kółko krzyżyk":
                ledType = "k";
                break;
            case "Serduszka":
                ledType = "s";
                break;
        }

        if (!TextUtils.isEmpty(ledType)) {
            String message = "l_type:" + ledType + "\r\n";
            writeMessage(message);
        }
    }

    public void setLedColor(String color) {
        String message = "l_color:" + color + "\r\n";
        writeMessage(message);
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

            this.bleManager.connect(MainActivity.this, MAC_ADDRESS);
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
                    progressBar.setVisibility(View.VISIBLE);
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
                        bluetoothStatusImageView.setImageResource(R.drawable.ic_bluetooth_connected);
                    } else {
                        bluetoothStatusImageView.setImageResource(R.drawable.ic_bluetooth_disconnected);
                    }
                    progressBar.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void displayMessageDialog(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Komunikat");
        alertDialogBuilder.setMessage(message)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                })
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onClick(View view) {
        int index = (int) view.getTag();
        String value = this.fieldValueArray[index];
        if (!TextUtils.isEmpty(value)) {
            return;
        }

        String character = getCharacter();
        String message = "value:" + character + " index:" + index + "\r\n";
        writeMessage(message);

        this.fieldValueArray[index] = character;
        this.updateUI(index);
        this.checkWin();
    }

    private void checkCurrentState() {
        int currentState = this.bleManager.getCurrentState();
        if (currentState != BleManager.STATE_CONNECTED) {
            Toast.makeText(this, "Brak połącznenia z bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    private void writeMessage(String message) {
        checkCurrentState();

        this.bleManager.writeService(this.bleManager.getGattService(
                "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"),
                "6E400002-B5A3-F393-E0A9-E50E24DCCA9E", message.getBytes());
    }

    private void win(String value) {
        String message = "win:" + value + "\r\n";
        writeMessage(message);

        if (value.equals("O")) {
            value = "kółko";
        } else {
            value = "krzyżyk";
        }

        displayMessageDialog("Wygrywa: " + value);
        reset();
    }

    private void reset() {
        this.currentCharacter = START_CHARACTER;

        int childCount = this.gridLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.fieldValueArray[i] = EMPTY;
            ImageView view = (ImageView) gridLayout.getChildAt(i);
            view.setImageDrawable(null);
        }

        if (this.currentCharacter.equals("O")) {
            this.xImageView.setImageResource(R.drawable.ic_x_active);
            this.oImageView.setImageResource(R.drawable.ic_o_inactive);
        } else {
            this.xImageView.setImageResource(R.drawable.ic_x_inactive);
            this.oImageView.setImageResource(R.drawable.ic_o_active);
        }

        String message = "restart:" + true + "\r\n";
        writeMessage(message);
    }

    private void updateUI(int index) {
        ImageView view = (ImageView) gridLayout.getChildAt(index);
        if (this.currentCharacter.equals("X")) {
            view.setImageResource(R.drawable.ic_big_x);
        } else {
            view.setImageResource(R.drawable.ic_big_o);
        }
    }

    private String getCharacter() {
        final String character;
        if (this.currentCharacter.equals("X")) {
            this.xImageView.setImageResource(R.drawable.ic_x_active);
            this.oImageView.setImageResource(R.drawable.ic_o_inactive);
            character = "O";
        } else {
            this.xImageView.setImageResource(R.drawable.ic_x_inactive);
            this.oImageView.setImageResource(R.drawable.ic_o_active);
            character = "X";
        }

        this.currentCharacter = character;
        return character;
    }

    private void checkWin() {
        checkLine1();
        checkLine2();
        checkLine3();
        checkLine4();
        checkLine5();
        checkLine6();
        checkLine7();
        checkLine8();

        for (int i = 0; i < 9; i++) {
            String character = String.valueOf(this.fieldValueArray[i]);
            if (TextUtils.isEmpty(character)) {
                return;
            }
        }

        reset();
    }

    private void checkLine1() {
        String v1 = this.fieldValueArray[0];
        String v2 = this.fieldValueArray[1];
        String v3 = this.fieldValueArray[2];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
        }
    }

    private void checkLine2() {
        String v1 = this.fieldValueArray[3];
        String v2 = this.fieldValueArray[4];
        String v3 = this.fieldValueArray[5];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
        }
    }

    private void checkLine3() {
        String v1 = this.fieldValueArray[6];
        String v2 = this.fieldValueArray[7];
        String v3 = this.fieldValueArray[8];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
        }
    }

    private void checkLine4() {
        String v1 = this.fieldValueArray[0];
        String v2 = this.fieldValueArray[3];
        String v3 = this.fieldValueArray[6];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
        }
    }

    private void checkLine5() {
        String v1 = this.fieldValueArray[1];
        String v2 = this.fieldValueArray[4];
        String v3 = this.fieldValueArray[7];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
        }
    }

    private void checkLine6() {
        String v1 = this.fieldValueArray[2];
        String v2 = this.fieldValueArray[5];
        String v3 = this.fieldValueArray[8];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
        }
    }

    private void checkLine7() {
        String v1 = this.fieldValueArray[0];
        String v2 = this.fieldValueArray[4];
        String v3 = this.fieldValueArray[8];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
        }
    }

    private void checkLine8() {
        String v1 = this.fieldValueArray[2];
        String v2 = this.fieldValueArray[4];
        String v3 = this.fieldValueArray[6];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.bleManager.disconnect();
        this.bleManager.close();
    }

    public void onNewGameClick(View view) {
        reset();
    }
}
