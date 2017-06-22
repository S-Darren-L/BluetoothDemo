package com.darren.android.bluetoothdemo.Activities;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.darren.android.bluetoothdemo.Adapters.DeviceListAdapter;
import com.darren.android.bluetoothdemo.R;
import com.darren.android.bluetoothdemo.Utils.Utils;
import com.darren.android.bluetoothdemo.ViewModel.BLEScanViewModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class BLEScanActivity extends AppCompatActivity {
    private static final String TAG = "BLEScanActivity";

    private BLEScanViewModel bleScanViewModel;

    @BindView(R.id.bleIndeterminateBar)
    ProgressBar bleIndeterminateBar;
    @BindView(R.id.bleStartScanButton)
    Button bleStartScanButton;
    @BindView(R.id.bleStopScanButton)
    Button bleStopScanButton;
    @BindView(R.id.selectedBLEDeviceName)
    TextView selectedBLEDeviceNameTV;
    @BindView(R.id.bleDeviceConnectionStateTextView)
    TextView bleDeviceConnectionStateTV;
    @BindView(R.id.bleStartConnectionButton)
    Button bleStartConnectionButton;
    @BindView(R.id.bleDevicesListView)
    ListView bleDevicesListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan);
        ButterKnife.bind(this);

        // Check if BLE is supported on the device
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.showAlertDialog(getString(R.string.no_ble_support), getApplicationContext());
        }

        this.bleScanViewModel = new BLEScanViewModel(getApplicationContext());

        bleScanViewModel.setFoundBLEDeviceAdapter(new DeviceListAdapter(this, R.layout.layout_device_item, bleScanViewModel.getBleDevicesList()));
        bleDevicesListView.setAdapter(bleScanViewModel.getFoundBLEDeviceAdapter());
    }

    @OnClick(R.id.bleStartScanButton)
    public void onBLEStartScanClicked() {
        bleIndeterminateBar.setVisibility(View.VISIBLE);
        bleScanViewModel.startScan();
    }

    @OnClick(R.id.bleStopScanButton)
    public void onBLEStopScanClicked() {
        bleIndeterminateBar.setVisibility(View.GONE);
        bleScanViewModel.stopScan();
    }

    @OnItemClick(R.id.bleDevicesListView)
    public void onBLEDevicesItemClicked(int position) {
        bleScanViewModel.setSelectedBLEDevice(position);
        selectedBLEDeviceNameTV.setText(bleScanViewModel.getSelectedBLEDeviceName());
    }

    @OnClick(R.id.bleStartConnectionButton)
    public void onBLEStartConnectionClicked() {
        bleScanViewModel.connect();
    }
}
