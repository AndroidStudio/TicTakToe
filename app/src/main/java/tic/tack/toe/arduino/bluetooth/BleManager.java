package tic.tack.toe.arduino.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.UUID;

import tic.tack.toe.arduino.dialog.MessageDialog;
import timber.log.Timber;

import static tic.tack.toe.arduino.Constants.TAG;

public class BleManager implements BleGattExecutor.BleExecutorListener {

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;

    public static final int STATE_CONNECTED = 2;

    private static BleManager INSTANCE = null;

    private final BleGattExecutor mExecutor = BleGattExecutor.createExecutor(this);
    private BluetoothAdapter mAdapter;
    private BluetoothGatt mGatt;

    private BluetoothDevice mDevice;

    private BleManagerListener mBleListener;

    private int mConnectionState = STATE_DISCONNECTED;

    public static BleManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new BleManager(context);
        }
        return INSTANCE;
    }

    public void setBleListener(BleManagerListener listener) {
        mBleListener = listener;
    }

    private BleManager(Context context) {
        if (this.mAdapter == null) {
            this.mAdapter = BleUtils.getBluetoothAdapter(context);
        }

        if (this.mAdapter == null || !mAdapter.isEnabled()) {
            Timber.tag(TAG).e("Unable to obtain a BluetoothAdapter.");
        }
    }

    public void connect(final Context context, String address) {
        Timber.tag(TAG).e("start connect");

        if (this.mAdapter == null || address == null) {
            Timber.tag(TAG).e("connect: BluetoothAdapter not initialized or unspecified address.");
            return;
        }

        try {
            this.mDevice = this.mAdapter.getRemoteDevice(address);
            if (this.mDevice == null) {
                Timber.tag(TAG).e("Device not found.  Unable to connect.");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.displayDialog(context, "Nieprawidłowy adres bluetooth");
            return;
        }

        this.mConnectionState = STATE_CONNECTING;

        if (this.mBleListener != null) {
            this.mBleListener.onConnecting();
        }

        this.mGatt = this.mDevice.connectGatt(context, false, this.mExecutor);
    }

    public void disconnect() {
        this.mDevice = null;
        if (this.mAdapter == null || mGatt == null) {
            Timber.tag(TAG).e("disconnect: BluetoothAdapter not initialized");
            return;
        }
        this.mGatt.disconnect();
    }

    public void close() {
        if (this.mGatt != null) {
            this.mGatt.close();
            this.mGatt = null;
            this.mDevice = null;
        }
    }

    public int getCurrentState() {
        return this.mConnectionState;
    }

    public void writeService(BluetoothGattService service, String uuid, byte[] value) {
        if (service != null) {
            if (this.mAdapter == null || this.mGatt == null) {
                Timber.tag(TAG).e("writeService: BluetoothAdapter not initialized");
                return;
            }

            this.mExecutor.write(service, uuid, value);
            this.mExecutor.execute(this.mGatt);
        }
    }

    public BluetoothGattService getGattService(String uuid) {
        if (mGatt != null) {
            final UUID serviceUuid = UUID.fromString(uuid);
            return mGatt.getService(serviceUuid);
        } else {
            return null;
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            this.mConnectionState = STATE_CONNECTED;
            if (this.mBleListener != null) {
                this.mBleListener.onConnected();
            }
            gatt.discoverServices();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            this.mConnectionState = STATE_DISCONNECTED;
            if (this.mBleListener != null) {
                this.mBleListener.onDisconnected();
            }
        } else if (newState == BluetoothProfile.STATE_CONNECTING) {
            this.mConnectionState = STATE_CONNECTING;
            if (this.mBleListener != null) {
                this.mBleListener.onConnecting();
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (this.mBleListener != null)
            this.mBleListener.onServicesDiscovered();
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Timber.tag(TAG).e("onServicesDiscovered status: %s", status);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (this.mBleListener != null) {
            this.mBleListener.onDataAvailable(characteristic);
        }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Timber.tag(TAG).e("onCharacteristicRead status: %s", status);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (this.mBleListener != null) {
            this.mBleListener.onDataAvailable(characteristic);
        }
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (this.mBleListener != null) {
            this.mBleListener.onDataAvailable(descriptor);
        }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Timber.tag(TAG).e("onDescriptorRead status: %s", status);
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        if (this.mBleListener != null) {
            this.mBleListener.onReadRemoteRssi(rssi);
        }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Timber.tag(TAG).e("onReadRemoteRssi status: %s", status);
        }
    }

    public interface BleManagerListener {

        void onConnected();

        void onConnecting();

        void onDisconnected();

        void onServicesDiscovered();

        void onDataAvailable(BluetoothGattCharacteristic characteristic);

        void onDataAvailable(BluetoothGattDescriptor descriptor);

        void onReadRemoteRssi(int rssi);
    }
}
