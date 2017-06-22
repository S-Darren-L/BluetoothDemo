package com.darren.android.bluetoothdemo.ViewModel;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.darren.android.bluetoothdemo.Adapters.DeviceListAdapter;
import com.darren.android.bluetoothdemo.Services.BLEService;
import com.darren.android.bluetoothdemo.Services.MainService;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Darren on 6/20/2017.
 */

public class BLEScanViewModel {
    private static final String TAG = "BLEScanViewModel";

    private Context context;
    private BLEService bleService;

    // Collect unique devices discovered, keyed by address
    private HashMap<String, BluetoothDevice> bleDevices;
    private ArrayList<BluetoothDevice> bleDevicesList;
    private DeviceListAdapter foundBLEDeviceAdapter;
    private BluetoothDevice selectedBLEDevice;

    public BLEScanViewModel(Context context) {
        this.context = context;
        this.bleService = MainService.getMainService().getBLEService(context, bleHandler);

        this.bleDevicesList = new ArrayList<>();
    }

    private Handler bleHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BluetoothDevice device = (BluetoothDevice)msg.obj;
            if(!bleDevicesList.contains(device)){
                bleDevicesList.add(device);
                foundBLEDeviceAdapter.notifyDataSetChanged();
            }
        }
    };

    public void startScan() {
        bleService.startScan();
    }

    public void stopScan() {
        bleService.stopScan();
    }

    public ArrayList<BluetoothDevice> getBleDevicesList() {
        return bleDevicesList;
    }

    public DeviceListAdapter getFoundBLEDeviceAdapter() {
        return foundBLEDeviceAdapter;
    }

    public void setFoundBLEDeviceAdapter(DeviceListAdapter foundBLEDeviceAdapter) {
        this.foundBLEDeviceAdapter = foundBLEDeviceAdapter;
    }

    public String getSelectedBLEDeviceName() {
        return selectedBLEDevice == null ? "" : selectedBLEDevice.getName();
    }

    public void setSelectedBLEDevice(int position) {
        this.selectedBLEDevice = bleDevicesList.get(position);
    }

    public void connect() {
        bleService.connect(selectedBLEDevice.getAddress());
    }
}
