package com.darren.android.bluetoothdemo.Adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.darren.android.bluetoothdemo.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

/**
 * Created by Darren on 6/17/2017.
 */

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private static final String TAG = "DeviceListAdapter";

    private LayoutInflater mLayoutInflater;
    private ArrayList<BluetoothDevice> mDevices;
    private int mViewResourceId;


    public DeviceListAdapter(Context context, int tvResourceId, ArrayList<BluetoothDevice> devices) {
        super(context, tvResourceId, devices);
        this.mDevices = devices;
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mLayoutInflater.inflate(mViewResourceId, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        BluetoothDevice device = mDevices.get(position);

        if(device != null) {

            if(holder.deviceName != null) {
                holder.deviceName.setText(device.getName());
            }
            if(holder.deviceAddress != null) {
                holder.deviceAddress.setText(device.getAddress());
            }
        }

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.btDeviceName)
        TextView deviceName;
        @BindView(R.id.btDeviceAddress)
        TextView deviceAddress;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
