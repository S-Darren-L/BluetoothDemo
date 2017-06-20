package com.darren.android.bluetoothdemo.Services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.darren.android.bluetoothdemo.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Darren on 6/17/2017.
 */

public class BluetoothService {
    private static final String TAG = "BluetoothService";
    private static final String appName = "BluetoothDemo";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("262c9b11-5851-4c20-8b92-22acad47687a");

    private Context context;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice bluetoothDevice;

    private boolean isBluetoothAvailable;
    private IntentFilter bluetoothStateChangedFilter;
    private IntentFilter deviceFoundFilter;
    private IntentFilter scanModeChangedFilter;
    private IntentFilter bondStateChangedFilter;

    private AcceptThread insecureAcceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    public BluetoothService(Context context) {
        this.context = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothStateChangedFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        deviceFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        scanModeChangedFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        bondStateChangedFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

        // Start bluetooth Server Socket
        startBluetoothConnectionService();
    }

    public boolean isBluetoothEnabled() {
        if (isBluetoothAvailable) {
            return mBluetoothAdapter.isEnabled();
        } else {
            return false;
        }
    }

    public boolean getIsBluetoothAvailable() {
        isBluetoothAvailable = mBluetoothAdapter != null;
        return isBluetoothAvailable;
    }

    public IntentFilter getBluetoothStateChangedFilter() {
        return bluetoothStateChangedFilter;
    }

    public IntentFilter getDeviceFoundFilter() {
        return deviceFoundFilter;
    }

    public IntentFilter getScanModeChangedFilter() {
        return scanModeChangedFilter;
    }

    public IntentFilter getBondStateChangedFilter() {
        return bondStateChangedFilter;
    }

    public void disableBluetooth() {
        Log.d(TAG, "switchEnableBluetooth: Disable Bluetooth");
        mBluetoothAdapter.disable();
    }

    public ArrayList<BluetoothDevice> findPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> pairedDevicesList = new ArrayList<>();
        if(pairedDevices.size() > 0) {
            for(BluetoothDevice device : pairedDevices) {
                pairedDevicesList.add(device);
                Log.d(TAG, "findPairedDevices: device name: " + device.getName() + ", device address: " + device.getAddress());
            }
        }
        return pairedDevicesList;
    }

    public void scanBluetoothDevices() {
        Log.d(TAG, "Looking for new bluetooth devices...");
        if(mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "scanBluetoothDevices: Canceling discovery");
        }
        discoverDevices();
    }

    public void bondBluetoothDevice(BluetoothDevice device) {
        Log.d(TAG, "bondBluetoothDevice: cancel discovery");
        mBluetoothAdapter.cancelDiscovery();

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.d(TAG, "bondBluetoothDevice: Trying to pair with: " + device.getName());
            device.createBond();
        }
    }

    public boolean unBondBluetoothDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            return true;
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "unBondBluetoothDevice: NoSuchMethodException: " + device.getName());
        } catch (IllegalAccessException e) {
            Log.e(TAG, "unBondBluetoothDevice: IllegalAccessException: " + device.getName());
        } catch (InvocationTargetException e) {
            Log.e(TAG, "unBondBluetoothDevice: InvocationTargetException: " + device.getName());
        }
        return false;
    }

    public void startBluetoothConnection(BluetoothDevice device) {
        Log.d(TAG, "startBluetoothConnection: Initializing RFCOMM Bluetooth connection");

        startClient(device);

    }

    public void discoverDevices() {
        mBluetoothAdapter.startDiscovery();
    }

    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket localServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
                Log.d(TAG, "AcceptThread: setting up server using: " + MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage().toString());
            }

            localServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running");

            // Create a new bluetooth socket
            BluetoothSocket socket = null;

            while (true) {
                try {
                    // This is a blocking call and will only return on a successful connection or an exception
                    Log.d(TAG, "run: RFCOMM server socket start...");

                    socket = localServerSocket.accept();
                    Log.d(TAG, "run: RFCOMM server socket accepted connection");
                } catch (IOException e) {
                    Log.e(TAG, "run: IOException: " + e.getMessage().toString());
                }

                if(socket != null) {
                    connected(socket, bluetoothDevice);
                    // Close local Server Socket
                    cancel();
                    break;
                }
            }
            Log.i(TAG, "END AcceptThread");
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread");

            try {
                localServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed: " + e.getMessage().toString());
            }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket bluetoothSocket;

        public ConnectThread(BluetoothDevice device) {
            Log.d(TAG, "ConnectThread: started");
            bluetoothDevice = device;

            BluetoothSocket tmp = null;

            // Get a bluetoothSocket for a connection with the given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: " + MY_UUID_INSECURE);
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket: " + e.getMessage().toString());
            }

            bluetoothSocket = tmp;
        }

        public void run() {

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();
            // Connection succeed or failed intent
            Intent connectionFinishedIntent = new Intent(context.getString(R.string.tag_connection_finished));

            try {
                // This is a blocking call and will only return on a successful connection or an exception
                bluetoothSocket.connect();
                Log.d(TAG, "run: ConnectThread connected");
                connectionFinishedIntent.putExtra(context.getString(R.string.tag_connection_finished), context.getString(R.string.tag_connection_succeed));
                LocalBroadcastManager.getInstance(context).sendBroadcast(connectionFinishedIntent);
            } catch (IOException e) {
                // Close the socket
                cancel();
                Log.e(TAG, "ConnectThread: run: Could not connect to UUID: " + MY_UUID_INSECURE + " : " + e.getMessage().toString());
                connectionFinishedIntent.putExtra(context.getString(R.string.tag_connection_finished), context.getString(R.string.tag_connection_failed));
                LocalBroadcastManager.getInstance(context).sendBroadcast(connectionFinishedIntent);
                return;
            }

            connected(bluetoothSocket, bluetoothDevice);
        }

        public void cancel() {
            Log.d(TAG, "cancel: Closing Client Socket");

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of bluetoothSocket in ConnectThread failed: " + e.getMessage().toString());
            }
        }
    }

    public synchronized void startBluetoothConnectionService() {
        Log.d(TAG, "start bluetooth connection service");

        // Cancel any thread attempting to make a connection
        if(connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
        if (insecureAcceptThread == null) {
            insecureAcceptThread = new AcceptThread();
            insecureAcceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device) {
        Log.d(TAG, "startClient: start");

        connectThread = new ConnectThread(device);
        connectThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private byte[] buffer;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: start");

            bluetoothSocket = socket;
            InputStream tmpInput = null;
            OutputStream tmpOutput = null;

            try {
                tmpInput = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "run: Error occurred when creating input stream: " + e.getMessage().toString());
            }

            try {
                tmpOutput = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "run: Error occurred when creating output stream: " + e.getMessage().toString());
            }

            inputStream = tmpInput;
            outputStream = tmpOutput;
        }

        public void run() {
            // Buffer store for the stream
            buffer = new byte[1024];
            // Byte returned from read()
            int bytes;

            // Keep listening to the InputStream until an exception occurs
            while(true) {
                // Read from the inputStream
                try {
                    bytes = inputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "inputStream: " + incomingMessage);

                    // Set incoming message intent
                    Intent incomingMessageIntent = new Intent(context.getString(R.string.tag_incoming_message));
                    incomingMessageIntent.putExtra(context.getString(R.string.tag_message), incomingMessage);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(incomingMessageIntent);
                } catch (IOException e) {
                    Log.e(TAG, "run: Error reading inputStream: " + e.getMessage().toString());
                    break;
                }
            }
        }

        // Call this from the activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputStream: " + text);
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to outputStream: " + e.getMessage().toString());
            }
        }

        public void cancel() {
            Log.d(TAG, "ConnectedThread: cancel: Closing Client Socket");

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of bluetoothSocket in ConnectedThread failed: " + e.getMessage().toString());
            }
        }
    }

    private void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected: Start");

        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    public void write(byte[] out) {
        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write called");
        if(connectedThread != null)
            connectedThread.write(out);
    }
}
