package tic.tack.toe.arduino.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;

import java.util.UUID;

import timber.log.Timber;

import static tic.tack.toe.arduino.Constants.TAG;

public class BleManager implements BleGattExecutor.BleExecutorListener {

    private static BleManager mInstance = null;

    private final BleGattExecutor mExecutor = BleGattExecutor.createExecutor(this);
    private BluetoothAdapter mAdapter;
    private BluetoothGatt mGatt;

    private BluetoothDevice mDevice;

    private BleManagerListener mBleListener;

    public static BleManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BleManager(context);
        }
        return mInstance;
    }

    public void setBleListener(BleManagerListener listener) {
        mBleListener = listener;
    }

    private BleManager(Context context) {
        if (mAdapter == null) {
            mAdapter = BleUtils.getBluetoothAdapter(context);
        }

        if (mAdapter == null || !mAdapter.isEnabled()) {
            Timber.tag(TAG).e("Unable to obtain a BluetoothAdapter.");
        }
    }

    public void connect(Context context, String address) {
        Timber.tag(TAG).e("start connect");

        if (mAdapter == null || address == null) {
            Timber.tag(TAG).e("connect: BluetoothAdapter not initialized or unspecified address.");
            return;
        }

        mDevice = mAdapter.getRemoteDevice(address);
        if (mDevice == null) {
            Timber.tag(TAG).e("Device not found.  Unable to connect.");
            return;
        }

        if (mBleListener != null) {
            mBleListener.onConnecting();
        }

        mGatt = mDevice.connectGatt(context, false, mExecutor);
    }

    public void disconnect() {
        mDevice = null;
        if (mAdapter == null || mGatt == null) {
            Timber.tag(TAG).e("disconnect: BluetoothAdapter not initialized");
            return;
        }
        mGatt.disconnect();
    }

    public void close() {
        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
            mDevice = null;
        }
    }

    public void writeService(BluetoothGattService service, String uuid, byte[] value) {
        if (service != null) {
            if (mAdapter == null || mGatt == null) {
                Timber.tag(TAG).e("writeService: BluetoothAdapter not initialized");
                return;
            }

            mExecutor.write(service, uuid, value);
            mExecutor.execute(mGatt);
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
            if (mBleListener != null) {
                mBleListener.onConnected();
            }
            gatt.discoverServices();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            if (mBleListener != null) {
                mBleListener.onDisconnected();
            }
        } else if (newState == BluetoothProfile.STATE_CONNECTING) {
            if (mBleListener != null) {
                mBleListener.onConnecting();
            }
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (mBleListener != null)
            mBleListener.onServicesDiscovered();
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Timber.tag(TAG).e("onServicesDiscovered status: %s", status);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (mBleListener != null) {
            mBleListener.onDataAvailable(characteristic);
        }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Timber.tag(TAG).e("onCharacteristicRead status: %s", status);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (mBleListener != null) {
            mBleListener.onDataAvailable(characteristic);
        }
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if (mBleListener != null) {
            mBleListener.onDataAvailable(descriptor);
        }

        if (status != BluetoothGatt.GATT_SUCCESS) {
            Timber.tag(TAG).e("onDescriptorRead status: %s", status);
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        if (mBleListener != null) {
            mBleListener.onReadRemoteRssi(rssi);
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
