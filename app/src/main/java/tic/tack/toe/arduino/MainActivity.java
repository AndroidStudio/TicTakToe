package tic.tack.toe.arduino;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import tic.tack.toe.arduino.service.BleManager;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private final static String macAddress = "D7:90:F4:FE:A4:55";
    private final static String TAG = "Bluetooth";
    private final static String EMPTY = "";

    private final String[] fieldValueArray = new String[9];

    private String currentCharacter = "X";

    private GridLayout gridLayout;
    private BleManager bleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        Timber.plant(new Timber.DebugTree());
        Timber.tag(TAG).e("onCreate");

        this.gridLayout = findViewById(R.id.gridLayout);
        int childCount = this.gridLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.fieldValueArray[i] = EMPTY;
            View view = this.gridLayout.getChildAt(i);
            view.setTag(i);
            view.setOnClickListener(this);
        }

        this.bleManager = BleManager.getInstance(this);
        this.bleManager.setBleListener(new BleManager.BleManagerListener() {
            @Override
            public void onConnected() {
                Timber.tag(TAG).e("onConnected");
            }

            @Override
            public void onConnecting() {
                Timber.tag(TAG).e("onConnecting");
            }

            @Override
            public void onDisconnected() {
                Timber.tag(TAG).e("onDisconnected");
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
        });

        this.bleManager.connect(MainActivity.this, macAddress);
    }

    @Override
    public void onClick(View view) {
        int index = (int) view.getTag();
        String value = this.fieldValueArray[index];
        if (!TextUtils.isEmpty(value)) {
            return;
        }

        String character = getCharacter();
        String message = "value: " + character + " index: " + index + "\r\n";

        this.fieldValueArray[index] = character;
        this.bleManager.writeService(this.bleManager.getGattService(
                "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"),
                "6E400002-B5A3-F393-E0A9-E50E24DCCA9E", message.getBytes());
        this.updateUI();
        this.checkWin();
    }

    private void win(String value) {
        Toast.makeText(this, "Wygrywa: " + value, Toast.LENGTH_LONG).show();
    }

    private void reset() {
        this.currentCharacter = "X";

        int childCount = this.gridLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.fieldValueArray[i] = EMPTY;
            Button view = (Button) gridLayout.getChildAt(i);
            view.setText(EMPTY);
        }
    }

    private void updateUI() {
        int childCount = this.gridLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            Button view = (Button) gridLayout.getChildAt(i);
            String character = String.valueOf(this.fieldValueArray[i]);
            view.setText(character);
        }
    }

    private String getCharacter() {
        final String character;
        if (this.currentCharacter.equals("X")) {
            character = "O";
        } else {
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
            this.reset();
        }
    }

    private void checkLine2() {
        String v1 = this.fieldValueArray[3];
        String v2 = this.fieldValueArray[4];
        String v3 = this.fieldValueArray[5];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
            this.reset();
        }
    }

    private void checkLine3() {
        String v1 = this.fieldValueArray[6];
        String v2 = this.fieldValueArray[7];
        String v3 = this.fieldValueArray[8];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
            this.reset();
        }
    }

    private void checkLine4() {
        String v1 = this.fieldValueArray[0];
        String v2 = this.fieldValueArray[3];
        String v3 = this.fieldValueArray[6];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
            this.reset();
        }
    }

    private void checkLine5() {
        String v1 = this.fieldValueArray[1];
        String v2 = this.fieldValueArray[4];
        String v3 = this.fieldValueArray[7];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
            this.reset();
        }
    }

    private void checkLine6() {
        String v1 = this.fieldValueArray[2];
        String v2 = this.fieldValueArray[5];
        String v3 = this.fieldValueArray[8];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
            this.reset();
        }
    }

    private void checkLine7() {
        String v1 = this.fieldValueArray[0];
        String v2 = this.fieldValueArray[4];
        String v3 = this.fieldValueArray[8];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
            this.reset();
        }
    }

    private void checkLine8() {
        String v1 = this.fieldValueArray[2];
        String v2 = this.fieldValueArray[4];
        String v3 = this.fieldValueArray[6];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
            this.reset();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.bleManager.disconnect();
    }
}
