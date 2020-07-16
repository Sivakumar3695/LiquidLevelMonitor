package com.myapp.checkWaterLevel;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.MacAddress;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    WifiManager wifiManager;
    IntentFilter intentFilter;
    BroadcastReceiver wifiScanReceiver;

    ProgressBar scanProgressBar;
    ProgressBar connectionProgressBar;

    ImageView tickScan;
    ImageView tickConnection;

    ImageView failedScan;
    ImageView failedConnection;

    TextView scanTextView;
    TextView connectionTextView;

    Button wifiButton;
    boolean connectedToNodeMcu;
    boolean scanCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        scanProgressBar = findViewById(R.id.progressBar);
        ((ProgressBar)findViewById(R.id.progressBar))
                .getIndeterminateDrawable()
                .setColorFilter(Color.parseColor("#3916A4"), PorterDuff.Mode.SRC_IN);

        connectionProgressBar = findViewById(R.id.progressBar2);
        ((ProgressBar)findViewById(R.id.progressBar2))
                .getIndeterminateDrawable()
                .setColorFilter(Color.parseColor("#3916A4"), PorterDuff.Mode.SRC_IN);

        tickScan = findViewById(R.id.imageView);
        failedScan = findViewById(R.id.scan_failed);

        tickConnection = findViewById(R.id.imageView2);
        failedConnection = findViewById(R.id.connection_failed);

        wifiButton = findViewById(R.id.wifi_button);
        wifiButton.setTextColor(Color.WHITE);
        wifiButton.setBackgroundColor(Color.parseColor("#470FFA"));

        scanTextView = findViewById(R.id.scan_text_view);
        connectionTextView = findViewById(R.id.connection_text_view);

        initialiseViews();


        connectedToNodeMcu = false;
        scanCompleted = false;

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        wifiScanReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (!success)
                {
                    // scan failure handling
                    Log.v("Wifi Scan", "No new wifi found");
                }
            }
        };
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);

    }

    public void connecToWifi(View view) throws Exception
    {
        wifiManager.setWifiEnabled(true);
        connectedToNodeMcu = false;
        scanCompleted = false;
        wifiButton.setClickable(false);
        initialiseViews();
        new scanAndConnectToNodeMcu().execute();
    }

    private void initialiseViews()
    {
        scanProgressBar.setVisibility(View.INVISIBLE);
        connectionProgressBar.setVisibility(View.INVISIBLE);
        tickScan.setVisibility(View.INVISIBLE);
        failedScan.setVisibility(View.INVISIBLE);
        tickConnection.setVisibility(View.INVISIBLE);
        failedConnection.setVisibility(View.INVISIBLE);

        scanTextView.setText("");
        connectionTextView.setText("");
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiScanReceiver, intentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiScanReceiver);
    }

    private void scanSuccess() {

        List<ScanResult> results = wifiManager.getScanResults();
        //String ssid = "Sivakumar’s iPhone";
        String ssid = "NodeMCU";
        //String ssid = "banu\uD83D\uDE09";
        for (ScanResult scanResult : results)
        {
            if (scanResult.SSID.equals(ssid))
            {
                //onScanCompleted();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                {
                    connectWifiForApi29AndAbove(scanResult);
                }
                else
                {
                    connectWifiForApiBelow29(scanResult);
                }
            }
        }

    }

    private void viewWaterLevel()
    {
        Intent intent = new Intent(this, WaterLevelSimulatorActivity.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void connectWifiForApi29AndAbove(ScanResult scanResult)
    {
        final NetworkSpecifier specifier = new WifiNetworkSpecifier.Builder()
                        .setSsid(scanResult.SSID)
                        .setBssid(MacAddress.fromString(scanResult.BSSID))
                        .build();
        final NetworkRequest request = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .setNetworkSpecifier(specifier)
                        .build();
        final ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {

            @Override
            public void onAvailable(Network network) {
            }

        };
        connectivityManager.requestNetwork(request, networkCallback);
    }

    private void connectWifiForApiBelow29(ScanResult scanResult)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        List<WifiConfiguration> wifiConfigList = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : wifiConfigList)
        {
            if (wifiConfiguration.SSID.equals("\""+scanResult.SSID+"\""))
            {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null)
                {
                    wifiManager.disableNetwork(wifiInfo.getNetworkId());
                }
                wifiManager.disconnect();
                wifiManager.enableNetwork(wifiConfiguration.networkId, true);

                while (true)
                {
                    wifiInfo = wifiManager.getConnectionInfo();
                    if (wifiInfo.getSSID().equals("\"NodeMCU\""))
                    {
                        connectedToNodeMcu = true;
                        break;
                    }
                }
            }
        }
    }

    private class scanAndConnectToNodeMcu extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            scanCompleted = wifiManager.startScan();
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            scanProgressBar.setVisibility(View.INVISIBLE);
            wifiButton.setClickable(true);

            if (scanCompleted)
            {
                tickScan.setVisibility(View.VISIBLE);
                scanTextView.setText("Scanning completed!!");

                final ConnectToNodeMcuAsync connectToNodeMcuAsync  = new ConnectToNodeMcuAsync();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run() {
                        if ( connectToNodeMcuAsync.getStatus() == AsyncTask.Status.RUNNING )
                            connectToNodeMcuAsync.cancel(true);
                    }
                }, 10000 );
                connectToNodeMcuAsync.execute();
            }
            else
            {
                failedScan.setVisibility(View.VISIBLE);
                scanTextView.setText("Scan failed. Check App Permissions");
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            scanProgressBar.setVisibility(View.VISIBLE);
            scanTextView.setText("Wifi device scanning in progress...");
        }
    }

    private class ConnectToNodeMcuAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            scanSuccess();
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            connectionProgressBar.setVisibility(View.INVISIBLE);
            wifiButton.setClickable(true);
            if (connectedToNodeMcu)
            {
                tickConnection.setVisibility(View.VISIBLE);
                connectionTextView.setText("Connected to Our wifi(NodeMCU)");

                wifiButton.setText("Check Water Level");
                wifiButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        viewWaterLevel();
                    }
                });
            }
            else
            {
                failedConnection.setVisibility(View.VISIBLE);
                connectionTextView.setText("Connection to our Wifi failed. Please Turn ON NodeMCU Wifi.");

            }
            super.onPostExecute(result);
        }

        @Override
        protected void onCancelled()
        {
            wifiButton.setClickable(true);
            failedConnection.setVisibility(View.VISIBLE);
            connectionTextView.setText("Connection to our Wifi failed. Please Turn ON NodeMCU Wifi.");
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            connectionProgressBar.setVisibility(View.VISIBLE);
            connectionTextView.setText("Connecting to Our wifi(NodeMCU)");
        }
    }
//    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
//    public void connectToBluetooth(View view) throws Exception
//    {
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (bluetoothAdapter == null) {
////            Intent intent = new Intent(this, WaterLevelSimulatorActivity.class);
////            startActivity(intent);
//            Toast.makeText(getApplicationContext(), "Unable to Connect BTooth", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (!bluetoothAdapter.isEnabled()) {
//            int REQUEST_ENABLE_BT = 1;
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        }
//
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//
//        ConnectThread connectThread = null;
//        if (pairedDevices.size() > 0) {
//            // There are paired devices. Get the name and address of each paired device.
//            for (BluetoothDevice device : pairedDevices) {
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC address
//                if (deviceName.equals("Banu"))
//                {
//                    connectThread = new ConnectThread(device);
//                    connectThread.run();
//                    if (isConnected(device))
//                    {
//                        Toast.makeText(getApplicationContext(), "BTooth Connected", Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(this, WaterLevelSimulatorActivity.class);
//                        startActivity(intent);
//                    }
//                    else
//                    {
////                        Intent intent = new Intent(this, WaterLevelSimulatorActivity.class);
////                        startActivity(intent);
//                        Toast.makeText(getApplicationContext(), "BTooth Connection failed", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                    break;
//                }
//            }
//
//        }
//       /* if (connectThread != null)
//        {
//            String msg = "Hi Banu";
//            connectThread.write(msg.getBytes());
//            Toast.makeText(getApplicationContext(), "Msg Sent", Toast.LENGTH_SHORT).show();
//
//        }*/
//    }
//
//    public static boolean isConnected(BluetoothDevice device) {
//        try {
//            Method m = device.getClass().getMethod("isConnected", (Class[]) null);
//            boolean connected = (boolean) m.invoke(device, (Object[]) null);
//            return connected;
//        } catch (Exception e) {
//            throw new IllegalStateException(e);
//        }
//    }
}