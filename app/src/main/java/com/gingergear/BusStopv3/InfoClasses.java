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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.gingergear.BusStopv3.ui.BusControl.BusControlFragment;
import com.gingergear.BusStopv3.ui.Map.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.gingergear.BusStopv3.SaveData.ReadMySavedPos;

public class InfoClasses {

    public static String county;
    public static Hashtable<String, LatLng> countyCenters = new Hashtable<>();

    public static void commitCounty(String County) {

        county = County;
        countyCenters.put("Henry", new LatLng(33.4479112, -84.1479229));

        MainActivity.focus = InfoClasses.countyCenters.get(InfoClasses.county);

    }

    public static class Login {

        public static String Username;
        public static String Password;
        public static String key;
        public static int gatheredMode = 0;

        public static String ERROR;

        static public void reset() {

            Username = null;
            Password = null;
            key = null;
            gatheredMode = 0;
            ERROR = null;
        }
    }


    public static class Markers {

        public static BitmapDescriptor MyBitmap;
        public static BitmapDescriptor BusBitmap;
        public static BitmapDescriptor BusBitmapGREYs;
        public static BitmapDescriptor BusBitmaps;


        public static void getMarkers(Context context) {

            Bitmap MyBitmapsi = getBitmapFromVectorDrawable(context, R.drawable.kid);
            Bitmap BusBitmapi = getBitmapFromVectorDrawable(context, R.drawable.bus);
            Bitmap BusGBitmai = getBitmapFromVectorDrawable(context, R.drawable.ic_baseline_directions_bus_24);
            Bitmap BusGBitmapi = getBitmapFromVectorDrawable(context, R.drawable.ic_baseline_directions_bus_24_grey);


            MyBitmap = BitmapDescriptorFactory.fromBitmap(MyBitmapsi);
            BusBitmap = BitmapDescriptorFactory.fromBitmap(BusBitmapi);
            BusBitmapGREYs = BitmapDescriptorFactory.fromBitmap(BusGBitmapi);
            BusBitmaps = BitmapDescriptorFactory.fromBitmap(BusGBitmai);


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

        public LatLng BusLocation = new LatLng(0, 0);
        public String School;
        public String BusNumber;
        public String Route;
        public Boolean Active = false;
        public Marker marker;
        public boolean updates = false;

        public Buses(String busNumber, LatLng busLocation, String school) {

            BusNumber = busNumber;
            BusLocation = busLocation;
            School = school;

        }

        public Buses(String busNumber, LatLng busLocation) {

            BusNumber = busNumber;
            BusLocation = busLocation;
            Active = false;
        }

        public void createMarker() {

            if (Mode.RIDER()) {
                marker = MainActivity.mMap.addMarker(new MarkerOptions()
                        .icon(Markers.BusBitmap)
                        .position(BusLocation)
                        .title(BusNumber)
                        .snippet("Runnning for " + School)
                        .visible(true));
            } else {

                marker = MainActivity.mMap.addMarker(new MarkerOptions()
                        .icon(Active ? Markers.BusBitmaps : Markers.BusBitmapGREYs)
                        .position(BusLocation)
                        .snippet(Active ? "Currently running route " + Route : "Not currently active")
                        .title(BusNumber)
                        .visible(true));
            }
        }

        public void updatePosition() {

            if (marker == null) {

                createMarker();
            }

            if (!marker.getTitle().equals(BusNumber)) {

                marker.setTitle(BusNumber);

            }

            if (Active) {

                marker.setIcon(Markers.BusBitmaps);
                marker.setSnippet("Currently running route " + Route);
                marker.setFlat(false);

            } else {
                marker.setIcon(Markers.BusBitmapGREYs);
                marker.setFlat(true);
            }

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

    public static class AdminInfo {

        public static String user;
        public static Hashtable<String, Buses> CountyBuses = new Hashtable<>();
        public static ArrayList<String> AvailableBusNumbers = new ArrayList<>();
        public static ArrayList<String> AvailableRoutes = new ArrayList<>();

        public static void updateBusPositions() {

            for (Buses bus : CountyBuses.values()) {

                if(bus.updates) {
                    bus.updates = false;
                    bus.marker.setVisible(false);
                    bus.createMarker();
                    bus.updatePosition();
                    Log.e("tag", "Update");
                }
            }
        }

        public static void recreateMarkers() {

            for (Buses bus : CountyBuses.values()) {

                bus.createMarker();
            }
        }
    }

    public static class BusInfo {

        public static String BusNumber = "14-71";
        public static LatLng BusLocation = new LatLng(0, 0);
        public static String BusDriver;
        public static ArrayList<String> AssignedBusRoutes;
        public static ArrayList<String> CompletedBusRoutes = new ArrayList<>();
        public static Marker marker;

        public static String CurrentRoute;

        public static void disconnectFromBus(Context context) {

            if (Bluetooth.isConnected) {
                Internet.disconnectYourBus(BusNumber, BusLocation);
                InfoClasses.Bluetooth.disconnectBluetoothDevice(context);
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

    public static class MyInfo {

        public static List<String> BusRoutes = new ArrayList<>();
        public static List<String> ZonedSchools = new ArrayList<>();

        public static List<String> SchoolURL = new ArrayList<>();
        public static Hashtable<String, Buses> myBuses = new Hashtable<>();

        public static String Address;
        public static LatLng CurrentLocation;
        public static LatLng savedLocation = null;

        public static Marker marker;

        public static String getSchoolFromRoute(String Route) {

            int index = BusRoutes.indexOf(Route);

            if (index != -1) {

                return ZonedSchools.get(index);
            }

            return null;
        }

        public static void updateBusPositions() {

            for (Buses bus : myBuses.values()) {

                bus.updatePosition();
            }
        }

        public static void recreateMarkers() {

            for (Buses bus : myBuses.values()) {

                bus.createMarker();
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

                                Log.i("Bluetooth", "Successfully Connected to" + BusInfo.BusNumber);
                                return;
                            }

                        }

                        Log.e("Bluetooth", "Unsuccessfully Connected to " + BusInfo.BusNumber);
                        isSearching = false;

                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);

                    Log.e(" Bluetooth", "Unsuccessfully Connected to" + BusInfo.BusNumber);

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
        static public int LOGIN = 5;
        static public int ActiveFragment = -1;

        public static boolean Map() {
            return ActiveFragment == Map;
        }

        public static boolean Setting() {
            return ActiveFragment == Setting;
        }

        public static boolean Rider() {
            return ActiveFragment == Rider;
        }

        public static boolean Driver() {
            return ActiveFragment == Driver;
        }

        public static boolean Admin() {
            return ActiveFragment == Admin;
        }

        public static boolean Login() {
            return ActiveFragment == LOGIN;
        }
    }

    public static class Mode {

        static public int ADMIN = 2;
        static public int DRIVER = 1;
        static public int RIDER = 0;
        static public int Rider_Driver = -1;

        static public boolean RIDER() {

            return RIDER == Rider_Driver;
        }

        static public boolean DRIVER() {

            return DRIVER == Rider_Driver;
        }

        static public boolean ADMIN() {

            return ADMIN == Rider_Driver;
        }


        public static void ChangeToDriverMode(Context context) {

            if (!DRIVER()) {

                Rider_Driver = DRIVER;
                MainActivity.ModeJustChanged();
                SaveData.SaveAppMode(DRIVER);

                ArrayList<String> data = SaveData.ReadBusCompletedRoutes();

                if (data != null) {
                    InfoClasses.BusInfo.CompletedBusRoutes = SaveData.ReadBusCompletedRoutes();
                }

                Toast.makeText(context, "You just entered the secret Bus Driver Mode!", Toast.LENGTH_SHORT).show();
                BusInfo.marker.setIcon(Markers.BusBitmap);

                InfoClasses.Login.key = SaveData.ReadKEY();
                Internet.joinRoute_AsBus(BusInfo.BusNumber);
                Internet.fetchYourRoutes(InfoClasses.BusInfo.BusNumber);


                MainActivity.mMap.getUiSettings().setScrollGesturesEnabled(false);
                MainActivity.mMap.getUiSettings().setZoomGesturesEnabled(false);

                Icons(DRIVER);
                Markers.getMarkers(context);


            }
        }

        public static void ChangeToRiderMode(Context context) {

            if (!RIDER()) {

                MainActivity.mMap.getUiSettings().setScrollGesturesEnabled(false);
                MainActivity.mMap.getUiSettings().setZoomGesturesEnabled(false);

                MainActivity.UpdatesAvailable = true;
                Rider_Driver = RIDER;
                SaveData.SaveAppMode(RIDER);

                if (SaveData.ReadMySavedRoutes() != null) {
                    ArrayList<String> routes = Objects.requireNonNull(SaveData.ReadMySavedRoutes())[0];
                    ArrayList<String> buses = Objects.requireNonNull(SaveData.ReadMySavedRoutes())[1];

                    MyInfo.BusRoutes.clear();
                    MyInfo.ZonedSchools.clear();

                    MyInfo.BusRoutes.addAll(routes);
                    MyInfo.ZonedSchools.addAll(buses);

                    Internet.joinRoute_AsRider();
                }

                LatLng savedPos = ReadMySavedPos();

                if (savedPos != null) {
                    if (savedPos.latitude != 0) {

                        MyInfo.savedLocation = savedPos;
                        MyInfo.CurrentLocation = savedPos;
                        MyInfo.Address = SaveData.ReadMySavedAddy();
                    }
                }


                MainActivity.ModeJustChanged();
                BusInfo.disconnectFromBus(context);

                Icons(RIDER);
                Markers.getMarkers(context);

                Toast.makeText(context, "You just reverted back to Rider Mode", Toast.LENGTH_SHORT).show();
            }
        }

        public static void ChangeToAdminMode(Context context) {

            if (!ADMIN()) {

                Rider_Driver = ADMIN;
                BusInfo.disconnectFromBus(context);

                SaveData.SaveAppMode(2);

                InfoClasses.Login.key = SaveData.ReadKEY();

                Internet.join_AsAdmin();
                Internet.retrieveAllLocations();
                Internet.retrieveAllRoutes();

                MainActivity.ModeJustChanged();

                MainActivity.mMap.getUiSettings().setScrollGesturesEnabled(true);
                MainActivity.mMap.getUiSettings().setZoomGesturesEnabled(true);

                Icons(ADMIN);
                Markers.getMarkers(context);


                Toast.makeText(context, "You just entered the secret ADMIN MODE", Toast.LENGTH_SHORT).show();
            }
        }

        public static void Icons(int Mode) {

            MainActivity.navigationView.setCheckedItem(0);
            if (Mode == ADMIN) {
                MainActivity.navigationView.getMenu().getItem(1).setVisible(false);
                MainActivity.navigationView.getMenu().getItem(2).setVisible(false);
                MainActivity.navigationView.getMenu().getItem(4).setVisible(true);
                MainActivity.navigationView.getMenu().getItem(3).setVisible(false);
                MainActivity.navigationView.getMenu().getItem(5).setVisible(true);
                MainActivity.navigationView.getMenu().getItem(6).setVisible(true);
            }

            if (Mode == DRIVER) {
                MainActivity.navigationView.getMenu().getItem(1).setVisible(false);
                MainActivity.navigationView.getMenu().getItem(2).setVisible(false);
                MainActivity.navigationView.getMenu().getItem(4).setVisible(false);
                MainActivity.navigationView.getMenu().getItem(3).setVisible(true);
                MainActivity.navigationView.getMenu().getItem(5).setVisible(true);
                MainActivity.navigationView.getMenu().getItem(6).setVisible(true);
            }

            if (Mode == RIDER) {
                MainActivity.navigationView.getMenu().getItem(1).setVisible(true);
                MainActivity.navigationView.getMenu().getItem(2).setVisible(true);
                MainActivity.navigationView.getMenu().getItem(4).setVisible(false);
                MainActivity.navigationView.getMenu().getItem(3).setVisible(false);
                MainActivity.navigationView.getMenu().getItem(5).setVisible(false);
                MainActivity.navigationView.getMenu().getItem(6).setVisible(false);
            }
        }
    }
}