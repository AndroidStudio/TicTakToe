package tic.tack.toe.arduino.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import java.util.LinkedList;
import java.util.UUID;

import timber.log.Timber;

import static tic.tack.toe.arduino.Constants.TAG;

class BleGattExecutor extends BluetoothGattCallback {

    interface ServiceAction {
        boolean execute(BluetoothGatt bluetoothGatt);
    }

    private final LinkedList<ServiceAction> mQueue = new LinkedList<>();
    private volatile ServiceAction mCurrentAction;

    void write(BluetoothGattService gattService, String uuid, byte[] value) {
        ServiceAction action = serviceWriteAction(gattService, uuid, value);
        this.mQueue.add(action);
    }

    private BleGattExecutor.ServiceAction serviceWriteAction(final BluetoothGattService gattService, final String uuid, final byte[] value) {
        return bluetoothGatt -> {
            Timber.tag(TAG).e("serviceWriteAction: %s", bytesToHex(value));

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
        };
    }

    void execute(BluetoothGatt gatt) {
        if (this.mCurrentAction == null) {
            while (!mQueue.isEmpty()) {
                final BleGattExecutor.ServiceAction action = mQueue.pop();
                this.mCurrentAction = action;
                if (!action.execute(gatt))
                    break;
                this.mCurrentAction = null;
            }
        }
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
        this.mCurrentAction = null;
        this.execute(gatt);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        this.mCurrentAction = null;
        this.execute(gatt);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        this.mCurrentAction = null;
        this.execute(gatt);

        Timber.tag(TAG).e("onCharacteristicWrite: %s", bytesToHex(characteristic.getValue()));
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            this.mQueue.clear();
            this.mCurrentAction = null;
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        this.mCurrentAction = null;
        this.execute(gatt);
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
