package com.gingergear.BusStopv3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Interpolator;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.BounceInterpolator;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.gingergear.BusStopv3.ui.BusControl.BusControlFragment;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.gingergear.BusStopv3.SaveData.ReadMySavedPos;

public class InfoClasses {

    public static class DriverBus {

        public static String BusNumber = "14-71";
        public static LatLng BusLocation;
        public static String BusDriver;
        public static ArrayList<String> AssignedBusRoutes;
        public static ArrayList<String> CompletedBusRoutes = new ArrayList<>();
        public static Marker marker;

        public static String CurrentRoute;

        public static void disconnectFromBus(Context context) {

            if (Bluetooth.isConnected) {
                InfoClasses.Bluetooth.disconnectBluetoothDevice(context);
                Internet.disconnectYourBus(BusNumber, BusLocation);
                context.stopService(BusControlFragment.intent);
                Bluetooth.isConnected = false;
                CurrentRoute = null;
            }

        }

        public static void connectToBus() {

            MainActivity mainActivity = new MainActivity();
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    InfoClasses.Bluetooth.connect();
                }

            });
        }

    }

    public static class Markers {

        public static BitmapDescriptor MyBitmap;
        public static BitmapDescriptor BusBitmap;

        public Markers(Context context){

            Bitmap MyBitmapsi = getBitmapFromVectorDrawable(context, R.drawable.kid);
            Bitmap BusBitmapi = getBitmapFromVectorDrawable(context, R.drawable.bus);

            MyBitmap = BitmapDescriptorFactory.fromBitmap(MyBitmapsi);
            BusBitmap = BitmapDescriptorFactory.fromBitmap(BusBitmapi);
        }

        public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
            Drawable drawable = ContextCompat.getDrawable(context, drawableId);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = (DrawableCompat.wrap(drawable)).mutate();
            }

            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        }
    }

    public class Buses {

        public LatLng BusLocation;
        public String School;
        public String BusNumber;
        public String Route;
        public Marker marker;

        public Buses(String busNumber, LatLng busLocation, String school){

            BusNumber = busNumber;
            BusLocation = busLocation;
            School = school;

        }

        public void createMarker(){

            marker = MainActivity.mMap.addMarker(new MarkerOptions()
                    .icon(Markers.BusBitmap)
                    .position(BusLocation)
                    .title(BusNumber)
                    .snippet("Runnning for " + School)
                    .visible(true));
        }

        public void updatePosition() {

            if (marker == null) {

                createMarker();
            } else {

                if(!marker.getTitle().equals(BusNumber)){

                    marker.setTitle(BusNumber);
                }
                Log.e("tag", "setting pus pos");
                final Handler handler = new Handler();
                final long start = SystemClock.uptimeMillis();
                final long duration = 1000;

                final BounceInterpolator interpolator = new BounceInterpolator();
                marker.setVisible(true);

                double Lat = BusLocation.latitude - marker.getPosition().latitude;
                double Lng = BusLocation.longitude - marker.getPosition().longitude;
                final LatLng begin = marker.getPosition();
                final LatLng distanceCoor = new LatLng(Lat, Lng);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        long elapsed = SystemClock.uptimeMillis() - start;
                        float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);

                        marker.setPosition(new LatLng(begin.latitude + elapsed * (distanceCoor.latitude / duration), begin.longitude + elapsed * (distanceCoor.longitude / duration)));

                        if (t > 0.0) {
                            // Post again 16ms later.
                            handler.postDelayed(this, 16);
                        }
                    }
                });
            }
        }
    }

    public static class myInfo {

        public static List<String> BusRoutes = new ArrayList<>(Collections.nCopies(3, "null"));
        public static List<String> ZonedSchools = new ArrayList<>(Collections.nCopies(3, "null"));

        public static List<String> SchoolURL = new ArrayList<>();
        public static Hashtable<String,Buses> myBuses = new Hashtable<>();

        public static String Address;
        public static LatLng CurrentLocation;
        public static LatLng savedLocation = null;

        public static Marker marker;

        public static String getSchoolFromRoute(String Route){

            int index = BusRoutes.indexOf(Route);

            if(index != -1){

                return ZonedSchools.get(index);
            }

            return null;
        }

        public static void updateBusPositions(){

            for(Buses bus : myBuses.values()){

                bus.updatePosition();
            }
        }
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
                if (bluetoothGatt != null) {
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

                                Log.i("Bluetooth", "Successfully Connected to" + DriverBus.BusNumber);
                                return;
                            }

                        }

                        Log.e("Bluetooth", "Unsuccessfully Connected to " + DriverBus.BusNumber);
                        isSearching = false;

                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);

                    Log.e(" Bluetooth", "Unsuccessfully Connected to" + DriverBus.BusNumber);

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

        public static boolean Map(){
            return ActiveFragment == Map;
        }
        public static boolean Setting(){
            return ActiveFragment == Setting;
        }
        public static boolean Rider(){
            return ActiveFragment == Rider;
        }
        public static boolean Driver(){
            return ActiveFragment == Driver;
        }
        public static boolean Admin(){
            return ActiveFragment == Admin;
        }
    }

    public static class Mode {

        static public int ADMIN = 1;
        static public int DRIVER = 1;
        static public int RIDER = 0;
        static public int Rider_Driver = RIDER;

        static public boolean RIDER() {

            return RIDER == Rider_Driver;
        }

        static public boolean DRIVER() {

            return DRIVER == Rider_Driver;
        }

        public static void ChangeToDriverMode(Context context) {

            if (Rider_Driver == RIDER) {

                Rider_Driver = DRIVER;
                MainActivity.ModeJustChanged();
                Internet.joinRoute_AsBus(DriverBus.BusNumber);
                Internet.fetchYourRoutes(DriverBus.BusNumber);
                SaveData.SaveAppMode(DRIVER);

                Toast.makeText(context, "You just entered the secret Bus Driver Mode!", Toast.LENGTH_SHORT).show();

            }
        }

        public static void ChangeToRiderMode(Context context) {

            if (Rider_Driver == DRIVER) {

                MainActivity.UpdatesAvailable = true;
                Rider_Driver = RIDER;
                SaveData.SaveAppMode(RIDER);

                if (SaveData.ReadMySavedRoutes() != null) {
                    ArrayList<String> routes = Objects.requireNonNull(SaveData.ReadMySavedRoutes())[0];
                    ArrayList<String> buses = Objects.requireNonNull(SaveData.ReadMySavedRoutes())[1];

                    InfoClasses.myInfo.BusRoutes.clear();
                    InfoClasses.myInfo.ZonedSchools.clear();

                    InfoClasses.myInfo.BusRoutes.addAll(routes);
                    InfoClasses.myInfo.ZonedSchools.addAll(buses);

                    for (String s : InfoClasses.myInfo.ZonedSchools) {

                        Log.e("tag", s);
                    }

                    for (String s : InfoClasses.myInfo.BusRoutes) {

                        Log.e("tag", s);
                    }

                    Internet.joinRoute_AsRider();
                }

                LatLng savedPos = ReadMySavedPos();

                if (savedPos != null) {
                    if (savedPos.latitude != 0) {

                        InfoClasses.myInfo.savedLocation = savedPos;
                        InfoClasses.myInfo.CurrentLocation = savedPos;
                        InfoClasses.myInfo.Address = SaveData.ReadMySavedAddy();
                    }
                }


                MainActivity.ModeJustChanged();
                DriverBus.disconnectFromBus(context);
                Toast.makeText(context, "You just reverted back to Rider Mode", Toast.LENGTH_SHORT).show();

            }
        }

        public static void ChangeToAdminMode(Context context) {

            Rider_Driver = ADMIN;
            Toast.makeText(context, "You just entered the secret ADMIN MODE", Toast.LENGTH_SHORT).show();
        }
    }
}