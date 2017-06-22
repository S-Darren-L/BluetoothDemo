package com.darren.android.bluetoothdemo.Activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.darren.android.bluetoothdemo.Adapters.DeviceListAdapter;
import com.darren.android.bluetoothdemo.R;
import com.darren.android.bluetoothdemo.Utils.Utils;
import com.darren.android.bluetoothdemo.ViewModel.MainViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;

    private MainViewModel mainViewModel;

    private boolean isBluetoothEnabled;

    @BindView(R.id.indeterminateBar)
    ProgressBar indeterminateBar;
    @BindView(R.id.bluetoothEnableTextView)
    TextView bluetoothEnableTV;
    @BindView(R.id.blueToothEnableSwitch)
    Switch blueToothEnableSwitch;
    @BindView(R.id.pairedDevicesListView)
    ListView pairedDevicesListView;
    @BindView(R.id.bluetoothDiscoverableTextView)
    TextView bluetoothDiscoverableTV;
    @BindView(R.id.blueToothDiscoverableButton)
    Button bluetoothDiscoverableButton;
    @BindView(R.id.bluetoothScanButton)
    Button bluetoothScanButton;
    @BindView(R.id.selectedDeviceName)
    TextView selectedDeviceNameTV;
    @BindView(R.id.devicePairingStateLabel)
    TextView devicePairingStateLabel;
    @BindView(R.id.devicePairingStateTextView)
    TextView devicePairingStateTV;
    @BindView(R.id.bluetoothStartPairingButton)
    Button btDevicePairButton;
    @BindView(R.id.bluetoothUnpairingButton)
    Button bluetoothUnpairingButton;
    @BindView(R.id.deviceConnectionStateLabel)
    TextView deviceConnectionStateLabel;
    @BindView(R.id.deviceConnectionStateTextView)
    TextView deviceConnectionStateTV;
    @BindView(R.id.bluetoothStartConnectionButton)
    Button startConnectionButton;
    @BindView(R.id.newDevicesListView)
    ListView newDevicesListView;
    @BindView(R.id.startMessagingButton)
    Button startMessagingButton;
    @BindView(R.id.startBLEScanButton)
    Button startBLEScanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mainViewModel = new MainViewModel(getApplicationContext());

        isBluetoothEnabled = checkBluetoothStatus();

        mainViewModel.setPairedDeviceListAdapter(new DeviceListAdapter(this, R.layout.layout_device_item, mainViewModel.getPairedBtDevices()));
        pairedDevicesListView.setAdapter(mainViewModel.getPairedDeviceListAdapter());

        mainViewModel.setFoundDeviceListAdapter(new DeviceListAdapter(this, R.layout.layout_device_item, mainViewModel.getFoundBtDevices()));
        newDevicesListView.setAdapter(mainViewModel.getFoundDeviceListAdapter());

        setBluetoothStateUI(isBluetoothEnabled);

        LocalBroadcastManager.getInstance(this).registerReceiver(connectionFinishedReceiver, new IntentFilter(getString(R.string.tag_connection_finished)));
    }

    @OnCheckedChanged(R.id.blueToothEnableSwitch)
    public void onBTEnableSwitchCheckedChanged(boolean isChecked) {
        if(mainViewModel.isBluetoothAvailable()) {
            if(isChecked){
                Log.d(TAG, "switchEnableBluetooth: Enable Bluetooth");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else
                mainViewModel.disableBluetooth();
            registerReceiver(stateChangedReceiver, mainViewModel.getBluetoothStateChangedFilter());
        }
    }

    @OnClick(R.id.blueToothDiscoverableButton)
    public void onBTDiscoverableButtonClicked() {
        if(mainViewModel.isBluetoothAvailable()) {
            Log.d(TAG, "discoverableBluetoothOn: Making device discoverable for 300 seconds");
            Intent discoverableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableBtIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableBtIntent);
            registerReceiver(scanModeChangedReceiver, mainViewModel.getScanModeChangedFilter());
        }
    }

    @OnClick(R.id.bluetoothScanButton)
    public void onBluetoothScanButtonClicked() {
        selectedDeviceNameTV.setText("");
        checkBluetoothPermission();
        mainViewModel.scanBluetoothDevices();
        registerReceiver(deviceFoundReceiver, mainViewModel.getDeviceFoundFilter());
    }

    @OnItemClick(R.id.pairedDevicesListView)
    public void onPairedDeviceNameClicked(int position) {
        Log.d(TAG, "onPairedDeviceNameClicked: device selected, id: " + position);
        mainViewModel.selectPairedBluetoothDevice(position);
        selectedDeviceNameTV.setText(mainViewModel.getSelectedDeviceName());
        setBondStateUI(mainViewModel.getSelectedDeviceBondStatus());
    }

    @OnItemClick(R.id.newDevicesListView)
    public void onNewDeviceNameClicked(int position) {
        Log.d(TAG, "onNewDeviceNameClicked: device selected, id: " + position);
        mainViewModel.selectFoundBluetoothDevice(position);
        selectedDeviceNameTV.setText(mainViewModel.getSelectedDeviceName());
        setBondStateUI(mainViewModel.getSelectedDeviceBondStatus());
    }

    @OnClick(R.id.bluetoothStartPairingButton)
    public void onStartPairingClicked() {
        if(mainViewModel.isBluetoothDeviceSelected()) {
            mainViewModel.bondBluetoothDevice();
            indeterminateBar.setVisibility(View.VISIBLE);
            registerReceiver(bondStateChangedReceiver, mainViewModel.getBondStateChangedFilter());
        }
        else {
            Utils.showAlertDialog(getString(R.string.no_device_selected), getApplicationContext());
        }
    }

    @OnClick(R.id.bluetoothUnpairingButton)
    public void onUnpairingClicked() {
        if(mainViewModel.isBluetoothDeviceSelected()) {
            boolean isUnpaired = mainViewModel.unBondBluetoothDevice();
            if(isUnpaired)
                setPairingStateLabelText(getString(R.string.bond_none));
        }
        else {
            Utils.showAlertDialog(getString(R.string.no_device_selected), getApplicationContext());
        }
    }

    @OnClick(R.id.bluetoothStartConnectionButton)
    public void onStartConnectionClicked() {
        if(mainViewModel.isBluetoothDeviceSelected()){
            indeterminateBar.setVisibility(View.VISIBLE);
            deviceConnectionStateTV.setText(getString(R.string.connecting));
            mainViewModel.connectBluetoothDevice();
        } else {
            Utils.showAlertDialog(getString(R.string.no_device_selected), getApplicationContext());
        }
    }

    @OnClick(R.id.startMessagingButton)
    public void onStartMessagingClicked() {
        Intent startMessageActivityIntent = new Intent(this, MessageActivity.class);
        startActivity(startMessageActivityIntent);
    }

    @OnClick(R.id.startBLEScanButton)
    public void onStartBLEScanClicked() {
        Intent startBLEScanActivityIntent = new Intent(this, BLEScanActivity.class);
        startActivity(startBLEScanActivityIntent);
    }

    private void checkBluetoothPermission() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.BLUETOOTH");
            permissionCheck += this.checkSelfPermission("Manifest.permission.BLUETOOTH_ADMIN");
            permissionCheck += this.checkSelfPermission("Manifest.permission.BLUETOOTH_PRIVILEGED");
            if (permissionCheck != 0) {
                this.requestPermissions(new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_PRIVILEGED}, 0);
            }
        } else {
            Log.d(TAG, "checkBluetoothPermission: No need to check permissions.");
        }
    }
    private boolean checkBluetoothStatus() {
        if (!mainViewModel.isBluetoothAvailable()) {
            // Device does not support Bluetooth
            Utils.showAlertDialog(getString(R.string.bluetooth_not_support), getApplicationContext());
            return false;
        } else {
            if(mainViewModel.isBluetoothEnabled()) {
                blueToothEnableSwitch.setChecked(true);
                blueToothEnableSwitch.setText(getString(R.string.on));
                return true;
            }
            else {
                blueToothEnableSwitch.setChecked(false);
                blueToothEnableSwitch.setText(getString(R.string.off));
                return false;
            }
        }
    }

    private void setPairingStateLabelText(String state) {
        devicePairingStateTV.setText(state);
    }

    private void setBondStateUI(int bondState) {
        switch (bondState) {
            case BluetoothDevice.BOND_BONDING:
                Log.d(TAG, "bondStateChangedReceiver: BOND_BONDING");
                setPairingStateLabelText(getString(R.string.bond_bonding));
                break;
            case BluetoothDevice.BOND_BONDED:
                Log.d(TAG, "bondStateChangedReceiver: BOND_BONDED");
                setPairingStateLabelText(getString(R.string.bond_bonded));
                indeterminateBar.setVisibility(View.GONE);
                break;
            case BluetoothDevice.BOND_NONE:
                Log.d(TAG, "bondStateChangedReceiver: BOND_NONE");
                setPairingStateLabelText(getString(R.string.bond_none));
                indeterminateBar.setVisibility(View.GONE);
                break;
        }
    }

    private void setBluetoothStateUI(boolean isBluetoothEnabled) {
        if(isBluetoothEnabled) {
            mainViewModel.findPairedDevices();
            blueToothEnableSwitch.setText(getString(R.string.on));
            startMessagingButton.setEnabled(true);
            startBLEScanButton.setEnabled(true);
        } else {
            blueToothEnableSwitch.setText(getString(R.string.off));
            startMessagingButton.setEnabled(false);
            startBLEScanButton.setEnabled(false);
        }

    }

    // Broadcast receiver for ACTION_STATE_CHANGED
    private final BroadcastReceiver stateChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "stateChangedReceiver: Receive state change");
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "stateChangedReceiver: STATE_ON");
                        isBluetoothEnabled = true;
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "stateChangedReceiver: STATE_TURNING_ON");
                        isBluetoothEnabled = true;
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "stateChangedReceiver: STATE_OFF");
                        isBluetoothEnabled = false;
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "stateChangedReceiver: STATE_TURNING_OFF");
                        isBluetoothEnabled = false;
                        break;
                }
                setBluetoothStateUI(isBluetoothEnabled);
            }
        }
    };

    // Broadcast receiver for ACTION_FOUND
    private final BroadcastReceiver deviceFoundReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "deviceFoundReceiver: Receive device found");
            String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mainViewModel.addFoundDevice(device);
            }
        }
    };

    // Broadcast receiver for ACTION_SCAN_MODE_CHANGED
    private final BroadcastReceiver scanModeChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "scanModeChangedReceiver: Receive scan mode change");
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "scanModeChangedReceiver: Discoverability Enabled");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "scanModeChangedReceiver: Discoverability Disabled. Able to receive connections");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "scanModeChangedReceiver: Discoverability Disabled. Not able to receive connections");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "scanModeChangedReceiver: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "scanModeChangedReceiver: Connected.");
                        break;
                }
            }
        }
    };

    // Broadcast receiver for ACTION_BOND_STATE_CHANGED
    private final BroadcastReceiver bondStateChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "bondStateChangedReceiver: Receive bond state changed");
            String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getBondState() == BluetoothDevice.BOND_BONDED)
                    mainViewModel.addPairedDevice(device);
                setBondStateUI(device.getBondState());
            }
        }
    };

    // Broadcast receiver for bluetooth connection finished
    private final BroadcastReceiver connectionFinishedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "connectionFinishedReceiver: Receive connection finished");
            indeterminateBar.setVisibility(View.GONE);

            String connectionResult = intent.getStringExtra(getString(R.string.tag_connection_finished));
            if(connectionResult.equals(getString(R.string.tag_connection_succeed)))
                deviceConnectionStateTV.setText(getString(R.string.connected));
            else
                deviceConnectionStateTV.setText(getString(R.string.not_connected));
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: onDestroy called");
        super.onDestroy();
        unregisterReceiver(deviceFoundReceiver);
        unregisterReceiver(stateChangedReceiver);
        unregisterReceiver(scanModeChangedReceiver);
        unregisterReceiver(bondStateChangedReceiver);
    }
}
