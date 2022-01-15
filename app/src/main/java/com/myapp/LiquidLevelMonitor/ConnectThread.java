package com.myapp.checkWaterLevel;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.OutputStream;

public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final OutputStream mmOutStream;
    private Handler handler; // handler that gets info from Bluetooth service
    private byte[] mmBuffer; // mmBuffer store for the stream

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    public ConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(mmDevice.getUuids()[0].getUuid());
        } catch (IOException e) {
            //Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;

        OutputStream tmpOut = null;
        try {
            tmpOut = mmSocket.getOutputStream();
        } catch (IOException e) {
           // Log.e(TAG, "Error occurred when creating output stream", e);
        }
        mmOutStream = tmpOut;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                //Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
       // manageMyConnectedSocket(mmSocket);
    }

    // Call this from the main activity to send data to the remote device.
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);

            // Share the sent message with the UI activity.
            Message writtenMsg = handler.obtainMessage(
                    MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
            writtenMsg.sendToTarget();

            Log.e("TEST", "Bluetooth msg sent");
        } catch (IOException e) {
//            Log.e(TAG, "Error occurred when sending data", e);
//
//            // Send a failure message back to the activity.
//            Message writeErrorMsg =
//                    handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
//            Bundle bundle = new Bundle();
//            bundle.putString("toast",
//                    "Couldn't send data to the other device");
//            writeErrorMsg.setData(bundle);
//            handler.sendMessage(writeErrorMsg);
        }
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            //Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
