package tic.tack.toe.arduino.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.UUID;

import timber.log.Timber;

public class BleManager implements BleGattExecutor.BleExecutorListener {
    private final static String TAG = "Bluetooth";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private static BleManager mInstance = null;

    private final BleGattExecutor mExecutor = BleGattExecutor.createExecutor(this);
    private BluetoothAdapter mAdapter;
    private BluetoothGatt mGatt;

    private BluetoothDevice mDevice;
    private String mDeviceAddress;
    private int mConnectionState = STATE_DISCONNECTED;

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

    public BluetoothAdapter getAdapter() {
        return this.mAdapter;
    }

    public int getConnectionState() {
        return mConnectionState;
    }

    public boolean connect(Context context, String address) {
        Timber.tag(TAG).e("start connect");

        if (mAdapter == null || address == null) {
            Timber.tag(TAG).e("connect: BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean reuseExistingConnection = sharedPreferences.getBoolean("pref_recycleconnection", false);

        if (reuseExistingConnection) {
            if (mDeviceAddress != null && address.equalsIgnoreCase(mDeviceAddress) && mGatt != null) {
                Timber.tag(TAG).e("Trying to use an existing BluetoothGatt for connection.");
                if (mGatt.connect()) {
                    mConnectionState = STATE_CONNECTING;
                    if (mBleListener != null)
                        mBleListener.onConnecting();
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            final boolean forceCloseBeforeNewConnection = sharedPreferences.getBoolean("pref_forcecloseconnection", true);
            if (forceCloseBeforeNewConnection) {
                close();
            }
        }

        mDevice = mAdapter.getRemoteDevice(address);
        if (mDevice == null) {
            Timber.tag(TAG).e("Device not found.  Unable to connect.");
            return false;
        }

        mDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        if (mBleListener != null) {
            mBleListener.onConnecting();
        }

        final boolean gattAutoconnect = sharedPreferences.getBoolean("pref_gattautoconnect", false);
        mGatt = mDevice.connectGatt(context, gattAutoconnect, mExecutor);
        return true;
    }

    public void disconnect() {
        mDevice = null;
        if (mAdapter == null || mGatt == null) {
            Timber.tag(TAG).e("disconnect: BluetoothAdapter not initialized");
            return;
        }
        mGatt.disconnect();
    }

    private void close() {
        if (mGatt != null) {
            mGatt.close();
            mGatt = null;
            mDeviceAddress = null;
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
            mConnectionState = STATE_CONNECTED;

            if (mBleListener != null) {
                mBleListener.onConnected();
            }
            gatt.discoverServices();
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mConnectionState = STATE_DISCONNECTED;
            if (mBleListener != null) {
                mBleListener.onDisconnected();
            }
        } else if (newState == BluetoothProfile.STATE_CONNECTING) {
            mConnectionState = STATE_CONNECTING;
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
