package tic.tack.toe.arduino.service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import java.util.LinkedList;
import java.util.UUID;

import timber.log.Timber;

class BleGattExecutor extends BluetoothGattCallback {
    private final static String TAG = "Bluetooth";

    interface ServiceAction {
        ServiceAction NULL = new ServiceAction() {
            @Override
            public boolean execute(BluetoothGatt bluetoothGatt) {
                return true;
            }
        };
        boolean execute(BluetoothGatt bluetoothGatt);
    }

    private final LinkedList<ServiceAction> mQueue = new LinkedList<>();
    private volatile ServiceAction mCurrentAction;

    public void write(BluetoothGattService gattService, String uuid, byte[] value) {
        ServiceAction action = serviceWriteAction(gattService, uuid, value);
        mQueue.add(action);
    }

    private BleGattExecutor.ServiceAction serviceWriteAction(final BluetoothGattService gattService, final String uuid, final byte[] value) {
        return new BleGattExecutor.ServiceAction() {
            @Override
            public boolean execute(BluetoothGatt bluetoothGatt) {
                Timber.tag(TAG).e("serviceWriteAction");

                final UUID characteristicUuid = UUID.fromString(uuid);
                final BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(characteristicUuid);
                if (characteristic != null) {
                    characteristic.setValue(value);
                    bluetoothGatt.writeCharacteristic(characteristic);
                    return false;
                } else {
                    Timber.tag(TAG).e("write: characteristic not found: %s", uuid);
                    return true;
                }
            }
        };
    }

    void execute(BluetoothGatt gatt) {
        if (mCurrentAction == null) {
            while (!mQueue.isEmpty()) {
                final BleGattExecutor.ServiceAction action = mQueue.pop();
                mCurrentAction = action;
                if (!action.execute(gatt))
                    break;
                mCurrentAction = null;
            }
        }
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
        mCurrentAction = null;
        execute(gatt);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        mCurrentAction = null;
        execute(gatt);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        mCurrentAction = null;
        execute(gatt);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mQueue.clear();
            mCurrentAction = null;
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        mCurrentAction = null;
        execute(gatt);
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {

    }

    static BleGattExecutor createExecutor(final BleExecutorListener listener) {
        return new BleGattExecutor() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                listener.onConnectionStateChange(gatt, status, newState);
                Timber.tag(TAG).e("onConnectionStateChange");

            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                listener.onServicesDiscovered(gatt, status);
                Timber.tag(TAG).e("onServicesDiscovered");
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                listener.onCharacteristicRead(gatt, characteristic, status);
                Timber.tag(TAG).e("onCharacteristicRead");
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                listener.onCharacteristicChanged(gatt, characteristic);
                Timber.tag(TAG).e("onCharacteristicChanged");
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
                listener.onDescriptorRead(gatt, descriptor, status);
                Timber.tag(TAG).e("onDescriptorRead");
            }

            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                listener.onReadRemoteRssi(gatt, rssi, status);
                Timber.tag(TAG).e("onReadRemoteRssi");
            }
        };
    }

    interface BleExecutorListener {

        void onConnectionStateChange(BluetoothGatt gatt, int status, int newState);

        void onServicesDiscovered(BluetoothGatt gatt, int status);

        void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);

        void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);

        void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);

        void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status);

    }
}
