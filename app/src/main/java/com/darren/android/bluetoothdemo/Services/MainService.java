package com.darren.android.bluetoothdemo.Services;

import android.content.Context;
import android.os.Handler;

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
    private BLEService bleService;

    public BluetoothService getBluetoothService(Context context) {
        if(this.bluetoothService == null)
            this.bluetoothService = new BluetoothService(context);
        return bluetoothService;
    }

    public BLEService getBLEService(Context context, Handler handler) {
        if(this.bleService == null)
            this.bleService = new BLEService(context, handler);
        return bleService; }
}
