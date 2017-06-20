package com.darren.android.bluetoothdemo.ViewModel;

import com.darren.android.bluetoothdemo.Services.BluetoothService;
import com.darren.android.bluetoothdemo.Services.MainService;

/**
 * Created by Darren on 6/20/2017.
 */

public class MessageViewModel {
    private static final String TAG = "MessageViewModel";

    private BluetoothService bluetoothService;
    private MainService mainService;

    public MessageViewModel() {
        mainService = MainService.getMainService();
        this.bluetoothService = mainService.getBluetoothService();
    }

    public void bluetoothMessageWrite(byte[] bytes) {
        bluetoothService.write(bytes);
    }
}
