package com.darren.android.bluetoothdemo.ViewModel;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import com.darren.android.bluetoothdemo.Adapters.DeviceListAdapter;
import com.darren.android.bluetoothdemo.Services.BluetoothService;
import com.darren.android.bluetoothdemo.Services.MainService;

import java.util.ArrayList;

/**
 * Created by Darren on 6/17/2017.
 */

public class MainViewModel {
    private static final String TAG = "MainViewModel";

    private MainService mainService;
    private BluetoothService bluetoothService;
    private ArrayList<BluetoothDevice> pairedBtDevices;
    private ArrayList<BluetoothDevice> foundBtDevices;
    private DeviceListAdapter pairedDeviceListAdapter;
    private DeviceListAdapter foundDeviceListAdapter;
    private BluetoothDevice selectedBluetoothDevice;

    public MainViewModel(Context context) {
        mainService = MainService.getMainService();
        mainService.init(context);
        this.bluetoothService = mainService.getBluetoothService();
        this.pairedBtDevices = new ArrayList<>();
        this.foundBtDevices = new ArrayList<>();
    }

    public boolean isBluetoothAvailable(){
        return bluetoothService.getIsBluetoothAvailable();
    }

    public boolean isBluetoothEnabled() {
        return bluetoothService.isBluetoothEnabled();
    }

    public ArrayList<BluetoothDevice> getPairedBtDevices() { return pairedBtDevices; }

    public void addPairedDevice(BluetoothDevice device) {
        pairedBtDevices.add(device);
        pairedDeviceListAdapter.notifyDataSetChanged();
    }

    public ArrayList<BluetoothDevice> getFoundBtDevices() {
        return foundBtDevices;
    }

    public IntentFilter getBluetoothStateChangedFilter() {
        return bluetoothService.getBluetoothStateChangedFilter();
    }

    public IntentFilter getScanModeChangedFilter() {
        return bluetoothService.getScanModeChangedFilter();
    }

    public IntentFilter getDeviceFoundFilter() {
        return bluetoothService.getDeviceFoundFilter();
    }

    public IntentFilter getBondStateChangedFilter() {
        return bluetoothService.getBondStateChangedFilter();
    }

    public void addFoundDevice(BluetoothDevice device) {
        foundBtDevices.add(device);
        foundDeviceListAdapter.notifyDataSetChanged();
        Log.d(TAG, "deviceFoundReceiver: device name: " + device.getName() + ", device address: " + device.getAddress());
    }

    public DeviceListAdapter getPairedDeviceListAdapter() { return pairedDeviceListAdapter; }

    public void setPairedDeviceListAdapter(DeviceListAdapter pairedDeviceListAdapter) {
        this.pairedDeviceListAdapter = pairedDeviceListAdapter;
    }

    public DeviceListAdapter getFoundDeviceListAdapter() {
        return foundDeviceListAdapter;
    }

    public void setFoundDeviceListAdapter(DeviceListAdapter foundDeviceListAdapter) {
        this.foundDeviceListAdapter = foundDeviceListAdapter;
    }

    public boolean isBluetoothDeviceSelected() {
        return selectedBluetoothDevice != null;
    }

    public String getSelectedDeviceName() {
        return selectedBluetoothDevice.getName();
    }

    public void disableBluetooth() {
        bluetoothService.disableBluetooth();
    }

    public void scanBluetoothDevices() {
        selectedBluetoothDevice = null;
        if(foundBtDevices.size() > 0){
            foundBtDevices.clear();
            foundDeviceListAdapter.notifyDataSetChanged();
        }
        bluetoothService.scanBluetoothDevices();
    }

    public void selectPairedBluetoothDevice(int position) {
        this.selectedBluetoothDevice = pairedBtDevices.get(position);
    }

    public void selectFoundBluetoothDevice(int position) {
        this.selectedBluetoothDevice = foundBtDevices.get(position);
    }

    public int getSelectedDeviceBondStatus() {
        return selectedBluetoothDevice.getBondState();
    }

    public void bondBluetoothDevice() {
        bluetoothService.bondBluetoothDevice(selectedBluetoothDevice);
    }

    public boolean unBondBluetoothDevice() {
        boolean isUnpaired = bluetoothService.unBondBluetoothDevice(selectedBluetoothDevice);
        if(isUnpaired && pairedBtDevices != null && pairedBtDevices.contains(selectedBluetoothDevice)){
            pairedBtDevices.remove(selectedBluetoothDevice);
            pairedDeviceListAdapter.notifyDataSetChanged();
        }
        return isUnpaired;
    }

    public void connectBluetoothDevice() {
        bluetoothService.startBluetoothConnection(selectedBluetoothDevice);
    }

    public void findPairedDevices() {
        if(pairedBtDevices.size() > 0)
            pairedBtDevices.clear();
        pairedBtDevices.addAll(bluetoothService.findPairedDevices());
        pairedDeviceListAdapter.notifyDataSetChanged();
    }

//    public void discoverDevices() {
//        bluetoothService.discoverDevices();
//        mainActivity.registerReceiver(deviceFoundReceiver, bluetoothService.getDeviceFoundFilter());
//    }
}
