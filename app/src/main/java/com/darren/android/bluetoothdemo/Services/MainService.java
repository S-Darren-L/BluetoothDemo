package com.darren.android.bluetoothdemo.Services;

import android.content.Context;

/**
 * Created by Darren on 6/20/2017.
 */

public class MainService
{
    private static MainService mainService = null;

    private MainService(){ }

    public static MainService getMainService() {
        if(mainService == null)
            mainService = new MainService();
        return mainService;
    }

    private BluetoothService bluetoothService;

    public void init(Context context) {
        this.bluetoothService = new BluetoothService(context);
    }

    public BluetoothService getBluetoothService() {
        return bluetoothService;
    }
}
