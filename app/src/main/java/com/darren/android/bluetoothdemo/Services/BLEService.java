package com.darren.android.bluetoothdemo.Services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.ArrayList;


/**
 * Created by Darren on 6/20/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BLEService {
    private static final String TAG = "BLEService";

    private Context context;
    private Handler handler;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBLEAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private long scanPeriod;
    private boolean isScanning;

    private String connectedBluetoothDeviceAddress;
    private BluetoothGatt bluetoothGatt;
    private int connectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.darren.android.bluetoothdemo.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.darren.android.bluetoothdemo.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.darren.android.bluetoothdemo.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.darren.android.bluetoothdemo.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.darren.android.bluetoothdemo.EXTRA_DATA";

    public BLEService(Context context, Handler handler){
        this.context = context;
        this.handler = handler;

        bluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        this.mBLEAdapter = bluetoothManager.getAdapter();
        this.bluetoothLeScanner = mBLEAdapter.getBluetoothLeScanner();
        this.scanPeriod = 3000;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startScan() {
        // Scan for devices
        ScanFilter bleFilter = new ScanFilter.Builder().build();

        ArrayList<ScanFilter> filters = new ArrayList<>();
        filters.add(bleFilter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        bluetoothLeScanner.startScan(filters, settings, scanCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG, "ScanCallback onScanResult");
            processResult(result);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.w(TAG, "ScanCallback onScanFailed: " + errorCode);
        }

        private void processResult(ScanResult result) {
            Log.d(TAG, "scan result: name: " + result.getDevice().getName() + " Rssi :" + result.getRssi());
            if(result.getDevice().getName() != null) {
                BluetoothDevice device = result.getDevice();
                handler.sendMessage(Message.obtain(null, 0, device));
            }
        }
    };

    public void stopScan() {bluetoothLeScanner.stopScan(scanCallback); }

    public boolean getIsScanning() { return isScanning; }

    public boolean connect(final String bluetoothDeviceAddress) {

        // Previously connected device.  Try to reconnect.
        if (connectedBluetoothDeviceAddress != null && bluetoothDeviceAddress.equals(connectedBluetoothDeviceAddress)
                && bluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if(bluetoothGatt.connect()){
                connectionState = STATE_CONNECTED;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBLEAdapter.getRemoteDevice(bluetoothDeviceAddress);
        if(device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        bluetoothGatt = device.connectGatt(context, false, gattCallback);
        connectedBluetoothDeviceAddress = bluetoothDeviceAddress;
        connectionState = STATE_CONNECTED;
        return true;
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            String intentAction;
            if(newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                connectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        bluetoothGatt.discoverServices());
            } else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                connectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        // New service discovered
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if(status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else
                Log.w(TAG, "onServicesDiscovered received: " + status);
        }

        // Result of a characteristic read operation
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        // TODO: send broadcast here
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        // TODO: handle characteristic here
        // ......
        // TODO: send broadcast here
        // ......
    }
}
