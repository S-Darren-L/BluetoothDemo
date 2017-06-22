package com.darren.android.bluetoothdemo.ViewModel;

import android.content.Context;

import com.darren.android.bluetoothdemo.Services.BluetoothService;
import com.darren.android.bluetoothdemo.Services.MainService;

/**
 * Created by Darren on 6/20/2017.
 */

public class MessageViewModel {
    private static final String TAG = "MessageViewModel";

    private Context context;
    private BluetoothService bluetoothService;

    public MessageViewModel(Context context) {
        this.context = context;
        this.bluetoothService = MainService.getMainService().getBluetoothService(context);
    }

    public void bluetoothMessageWrite(byte[] bytes) {
        bluetoothService.write(bytes);
    }
}
