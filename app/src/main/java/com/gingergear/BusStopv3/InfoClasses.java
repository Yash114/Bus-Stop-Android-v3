package com.gingergear.BusStopv3;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
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
        public String Route = "null";
        public Boolean Active = false;
        public Marker marker;
        public boolean updates = false;

        public int counter = 15;

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
                        .icon(Markers.BusBitmaps)
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
                        .rotation(0)
                        .visible(true));

            }
            marker.setTag("bus");


        }

        public void updatePosition() {

            counter = 15;
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
                marker.setSnippet("Not currently active");
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

        public static Hashtable<String, String> R2N = new Hashtable<>();
        public static Hashtable<String, String> N2R = new Hashtable<>();

        public static Boolean updateAllLocations = false;

        public static void updateBusPositions() {

            for (Buses bus : CountyBuses.values()) {

                if (bus.updates) {
                    bus.updates = false;
                    bus.marker.setVisible(true);
                    bus.updatePosition();
                    Log.e("tag", "Update");
                }
            }
        }

        public static void recreateMarkers() {

            MainActivity.mMap.clear();
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
                Internet.disconnectYourBus();
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
        public static boolean NewBus = false;

        public static String getSchoolFromRoute(String Route) {

            int index = BusRoutes.indexOf(Route);

            if (index != -1) {

                return ZonedSchools.get(index);
            }

            return null;
        }

        public static void updateBusPositions() {

            for (Buses bus : myBuses.values()) {

                if(bus.updates) {
                    bus.updatePosition();
                    bus.updates = false;
                    bus.counter = 15;
                }
            }
        }

        public static void recreateMarkers() {

            MainActivity.mMap.clear();
            for (Buses bus : myBuses.values()) {

                bus.createMarker();
            }
        }
    }

    public static class Bluetooth {

        public static BluetoothAdapter bluetoothAdapter;
        public static BluetoothDevice connectedDevice;
        public static BluetoothGatt bluetoothGatt;
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
            final BluetoothLeScanner BLEscanner = bluetoothAdapter.getBluetoothLeScanner();

            final ScanCallback BLEcallback = new ScanCallback() {

                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    Log.i("Bluetooth", result.getDevice().getName());


                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);

                    for (ScanResult result : results) {
//                        if (result.getDevice().getName() != null) {
                            Log.e("Bluetooth", "Devices found: " + result.getDevice().getAddress());
//                        }
                    }
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
                                Log.e("Bluetooth", devices.getAddress());

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
                            Log.e("Bluetooth", "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED");

                            break;

                        case ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED:
                            Log.e("Bluetooth", "SCAN_FAILED_FEATURE_UNSUPPORTED");

                            break;

                        case ScanCallback.SCAN_FAILED_INTERNAL_ERROR:
                            Log.e("Bluetooth", "SCAN_FAILED_INTERNAL_ERROR");

                            break;

                        default:
                            Log.e("Bluetooth", "ERROR");

                    }
                }
            };

            Log.d("Bluetooth", "Bluetooth Scan Initiated");

            BLEscanner.startScan(null, scanSettings.setReportDelay(1500).build(), BLEcallback);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {

                    BLEscanner.stopScan(BLEcallback);
                    Log.e("Bluetooth", "Bluetooth Scan Complete");

                    isSearching = false;
                }
            }, 7500);
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

        static public ArrayList<MenuItem> icons = new ArrayList<>();

        static public boolean RIDER() {

            return RIDER == Rider_Driver;
        }

        static public boolean DRIVER() {

            return DRIVER == Rider_Driver;
        }

        static public boolean ADMIN() {

            return ADMIN == Rider_Driver;
        }

        public static void ChangeToRiderMode(Context context) {

            if (!RIDER()) {

                Internet.refresh();
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
                } else {

                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    MainActivity.mMap.setMyLocationEnabled(true);
                }


                MainActivity.ModeJustChanged();
                BusInfo.disconnectFromBus(context);

                Markers.getMarkers(context);

                Toast.makeText(context, "You just reverted back to Rider Mode", Toast.LENGTH_SHORT).show();
            }
        }

        public static void ChangeToDriverMode(Context context) {

            if (!DRIVER()) {

                Internet.refresh();
                Rider_Driver = DRIVER;
                MainActivity.ModeJustChanged();
                SaveData.SaveAppMode(DRIVER);
                Icons(DRIVER);


                ArrayList<String> data = SaveData.ReadBusCompletedRoutes();

                if (data != null) {
                    InfoClasses.BusInfo.CompletedBusRoutes = SaveData.ReadBusCompletedRoutes();
                }

                Toast.makeText(context, "You just entered the secret Bus Driver Mode!", Toast.LENGTH_SHORT).show();

                InfoClasses.Login.key = SaveData.ReadKEY();
                Internet.login();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {

                        Internet.joinRoute_AsBus();
                        Internet.fetchYourRoutes(InfoClasses.BusInfo.BusNumber);
                    }
                    }, 1000);


                MainActivity.mMap.getUiSettings().setScrollGesturesEnabled(false);
                MainActivity.mMap.getUiSettings().setZoomGesturesEnabled(false);

                Markers.getMarkers(context);
                BusInfo.marker.setIcon(Markers.BusBitmap);


            }
        }

        public static void ChangeToAdminMode(Context context) {

            if (!ADMIN()) {

                Internet.refresh();
                Rider_Driver = ADMIN;
                BusInfo.disconnectFromBus(context);

                SaveData.SaveAppMode(2);
                Icons(ADMIN);

                InfoClasses.Login.key = SaveData.ReadKEY();

                Internet.login();

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Internet.join_AsAdmin();
                        Internet.retrieveAllLocations();
                        Internet.retrieveAllRoutes();
                    }
                }, 1000);

                MainActivity.ModeJustChanged();

                MainActivity.mMap.getUiSettings().setScrollGesturesEnabled(true);
                MainActivity.mMap.getUiSettings().setZoomGesturesEnabled(true);

                Markers.getMarkers(context);


                Toast.makeText(context, "You just entered the secret ADMIN MODE", Toast.LENGTH_SHORT).show();
            }
        }

        public static void Icons(int Mode) {

            if(icons.size() == 0){

                for(int x = 0; x < 7; x++){
                    icons.add(MainActivity.navigationView.getMenu().getItem(x));
                }
            }

            if (Mode == ADMIN) {
                changeVisibilityMenuItem(1, false);
                changeVisibilityMenuItem(2, false);
                changeVisibilityMenuItem(3, false);
                changeVisibilityMenuItem(4, true);
                changeVisibilityMenuItem(5, true);
                changeVisibilityMenuItem(6, true);
            }

            if (Mode == DRIVER) {
                changeVisibilityMenuItem(1, false);
                changeVisibilityMenuItem(2, false);
                changeVisibilityMenuItem(3, true);
                changeVisibilityMenuItem(4, false);
                changeVisibilityMenuItem(5, true);
                changeVisibilityMenuItem(6, true);
            }

            if (Mode == RIDER) {
                changeVisibilityMenuItem(1, true);
                changeVisibilityMenuItem(2, true);
                changeVisibilityMenuItem(3, false);
                changeVisibilityMenuItem(4, false);
                changeVisibilityMenuItem(5, false);
                changeVisibilityMenuItem(6, false);
            }

            MainActivity.navigationView.setCheckedItem(0);

        }

        private static void changeVisibilityMenuItem(int index, boolean visibility){
            if(icons.get(index).isVisible() != visibility) {
                icons.get(index).setVisible(visibility);

                Log.i("tag", icons.get(index).getTitle().toString() + ": " + (icons.get(index).isVisible() ? "vis" : "invis"));

            }
        }
    }
}