package com.gingergear.BusStopv3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.gingergear.BusStopv3.ui.BusControl.BusControlFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class InfoClasses {

    public static class busInfo {

        public static String BusNumber = "14-71";
        public static LatLng BusLocation;
        public static String BusDriver;
        public static ArrayList<String> AssignedBusRoutes;
        public static ArrayList<String> CompletedBusRoutes = new ArrayList<>();

        public static String CurrentRoute;

        public static void disconnectFromBus(Context context){

            if(Bluetooth.isConnected) {
                InfoClasses.Bluetooth.disconnectBluetoothDevice(context);
                Internet.disconnectYourBus(BusNumber, BusLocation);
                context.stopService(BusControlFragment.intent);
                Bluetooth.isConnected = false;
            }

        }

        public static void connectToBus(){

            MainActivity mainActivity = new MainActivity();
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    InfoClasses.Bluetooth.connect();
                }

            });
        }

    }

    public static class myInfo {

        public static List<String> BusRoutes;
        public static String Address;
        public static LatLng CurrentLocation;

    }

    public static class Bluetooth {

        public static BluetoothAdapter bluetoothAdapter;
        public static BluetoothDevice connectedDevice;
        public static BluetoothGatt bluetoothGatt;
        private static BluetoothLeScanner BLEscanner;
        private static String BluetoothName = "BT05";
        public static Boolean isConnected = false;
        public static Boolean isSearching = false;


        public static void disconnectBluetoothDevice(Context context) {

            if (isConnected) {
                if(bluetoothGatt != null) {
                    bluetoothGatt.disconnect();
                    bluetoothGatt.close();
                }
                Internet.CloseWebSocket();
                isConnected = false;
                InfoClasses.Status.inRoute = false;
                context.stopService(BusControlFragment.intent);

            }
        }

        private static void connect() {
            isSearching = true;
            isConnected = false;
            ScanSettings.Builder scanSettings = new ScanSettings.Builder();
            BLEscanner = bluetoothAdapter.getBluetoothLeScanner();

            final ScanCallback BLEcallback = new ScanCallback() {

                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    Log.i("Bluetooth", result.getDevice().getName());

                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);

                    if (isSearching) {

                        ArrayList<BluetoothDevice> availableDevices = new ArrayList<>();

                        for (ScanResult result : results) {
                            if (result.getDevice().getName() != null) {
                                Log.e("Bluetooth", "Devices found: " + result.getDevice().getName());
                                availableDevices.add(result.getDevice());
                            }
                        }

                        for (BluetoothDevice devices : availableDevices) {

                            Log.i("Bluetooth", devices.getName());
                            if (devices.getName().equals(BluetoothName)) {
                                availableDevices.clear();
                                connectedDevice = devices;

                                Log.e("Bluetooth", "Bluetooth Device Found");

                                isConnected = true;
                                isSearching = false;

                                SaveData.SaveMyBus(true, busInfo.BusNumber);
                                Log.i("Bluetooth", "Successfully Connected to" + busInfo.BusNumber);
                                return;
                            }

                        }

                        Log.e("Bluetooth", "Unsuccessfully Connected to " + busInfo.BusNumber);
                        isSearching = false;

                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);

                    Log.e(" Bluetooth", "Unsuccessfully Connected to" + busInfo.BusNumber);

                    isSearching = false;

                    switch (errorCode) {

                        case ScanCallback.SCAN_FAILED_ALREADY_STARTED:
                            Log.e("Bluetooth", "CAN_FAILED_ALREADY_STARTED");

                            break;

                        case ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                            Log.e("bluetooth", "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED");

                            break;

                        case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
                            Log.e("bluetooth", "SCAN_FAILED_FEATURE_UNSUPPORTED");

                            break;

                        case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
                            Log.e("bluetooth", "SCAN_FAILED_INTERNAL_ERROR");

                            break;

                        default:
                            Log.e("bluetooth", "ERROR");

                    }

                }

            };

            Log.d("Bluetooth", "Bluetooth Scan Initiated");

            BLEscanner.startScan(null, scanSettings.setReportDelay(500).build(), BLEcallback);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {

                    BLEscanner.stopScan(BLEcallback);
                    Log.e("Bluetooth", "Bluetooth Scan Complete");

                    isSearching = false;
                }
            }, 7000);
        }
    }

    public static class Status {

        static public int NORMAL = 0;
        static public int Not_Joined = -1;
        static public int Connection_notConnected = -2;
        static public int Disconnected = -3;
        static public int PAUSED = -4;
        static public int DONE = 1;
        static public boolean inRoute = false;
        static public int Status = Not_Joined;

        static public int Map = 0;
        static public int Setting = 1;
        static public int Rider = 2;
        static public int Driver = 3;
        static public int Admin = 4;
        static public int ActiveFragment = 0;

    }

    public static class Mode {

        static public int DRIVER = 0;
        static public int RIDER = 1;
        static public int Rider_Driver = DRIVER;

        public static void ChangeToDriverMode(){

            if(Rider_Driver == RIDER) {
                Internet.joinRoute_AsBus(InfoClasses.busInfo.BusNumber);
                Internet.fetchYourRoutes(InfoClasses.busInfo.BusNumber);
                Rider_Driver = DRIVER;
            }
        }
    }
}