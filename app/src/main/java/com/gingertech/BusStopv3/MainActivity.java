package com.gingertech.BusStopv3;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.gingertech.BusStopv3.ui.AdminPanel.AdminPanelFragment;
import com.gingertech.BusStopv3.ui.BusControl.BusControlFragment;
import com.gingertech.BusStopv3.ui.Comms.AdminComms;
import com.gingertech.BusStopv3.ui.Login.LoginFragment;
import com.gingertech.BusStopv3.ui.Map.MapFragment;
import com.gingertech.BusStopv3.ui.RiderBus.RiderFragment;
import com.gingertech.BusStopv3.ui.Settings.SettingsFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private AppBarConfiguration mAppBarConfiguration;

    //Save Methods
    public static SaveData saveData;

    //fragment thing
    public static FragmentManager fragmentManager;
    public static MenuInflater menuInflater;
    public static MapFragment mapFragment;
    public static ImageButton panicButton;


    //things with DriverFrag
    private static ArrayList<String> routes;

    //UI variables
    public static NavigationView navigationView;
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    LocationRequest mLocationRequest;

    //Map Stuff
    public static GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static SupportMapFragment mapSupportFragment;
    public static boolean markerSelected = false;
    public static boolean UpdatesAvailable = true;

    public static LatLng focus;
    private static float zoom = 10.3f;

    private static int numberOfClicks = 0;

    public static ArrayList<Integer> items = new ArrayList<>();
    public static ArrayList<Boolean> activeArray = new ArrayList<>(Arrays.asList(true, false, false, false, false, false, false));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Internet.CreateWebSocketConnection();
        InfoClasses.ToastMessages.init(this);
        FirebaseNotifications.initialize();
        StartRefresh();

        items.clear();
        items.add(R.id.nav_map);
        items.add(R.id.nav_settings);
        items.add(R.id.nav_myBuses);
        items.add(R.id.nav_busSettings);
        items.add(R.id.nav_adminDashBoard);
        items.add(R.id.nav_Text);
        items.add(R.id.nav_Logout);

        if (InfoClasses.Mode.Rider_Driver != -1) {

            switch (InfoClasses.Mode.Rider_Driver) {

                case 0:
                    InfoClasses.Mode.ChangeToRiderMode(this);
                    break;

                case 1:
                    InfoClasses.Mode.ChangeToDriverMode(this);
                    break;

                case 2:
                    InfoClasses.Mode.ChangeToAdminMode(this);
                    break;

            }
        }

        menuInflater = getMenuInflater();

        checkLocationPermission();

        fragmentManager = getSupportFragmentManager();
        Internet.CreateWebSocketConnection();

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        final ImageButton navButton = findViewById(R.id.navButton);
        final ImageButton mapButton = findViewById(R.id.mapButton);
        panicButton = findViewById(R.id.panicButton);

        panicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (InfoClasses.Mode.DRIVER()) {

                    Vibrator j = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    j.vibrate(100);

                    panicButton.setClickable(false);

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                public void run() {

                                    panicButton.setClickable(true);
                                }
                            });
                        }
                    }, 3000);

                    if (InfoClasses.BusInfo.BusLocation == null) {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (InfoClasses.BusInfo.BusLocation != null) {
                                    Internet.ReverseGeoCode RGC = new Internet.ReverseGeoCode();
                                    RGC.execute(InfoClasses.BusInfo.BusLocation.latitude, InfoClasses.BusInfo.BusLocation.longitude);

                                    new Timer().schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            FirebaseNotifications.sendBusAlert();

                                        }
                                    }, 500);
                                } else {

                                    FirebaseNotifications.sendBusAlert_noLocation();

                                }
                            }
                        }, 1000);
                    } else {

                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Internet.ReverseGeoCode RGC = new Internet.ReverseGeoCode();
                                RGC.execute(InfoClasses.BusInfo.BusLocation.latitude, InfoClasses.BusInfo.BusLocation.longitude);

                                if (InfoClasses.BusInfo.BusLocation != null) {
                                    new Timer().schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            FirebaseNotifications.sendBusAlert();


                                        }
                                    }, 500);
                                }
                            }
                        }, 500);
                    }
                }
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                drawer.openDrawer(Gravity.LEFT);
                hideKeyboard();
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard();

                if (!InfoClasses.Status.Map()) {
                    ChangeToMapView();
                } else {

                    if (InfoClasses.Mode.ADMIN()) {
                        focus = InfoClasses.countyCenters.get(InfoClasses.county);

                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(focus)
                                .zoom(10.3f) // Gets the correct zoom factor from the distance vairabe
                                .bearing(0)                // Sets the orientation of the camera to east
                                .build();

                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    }
                }
            }
        });
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                drawer.closeDrawers();
                String Title = item.getTitle().toString();

                Fragment ContainerViewID = new MapFragment();
                int fragmentID = R.id.nav_map;

                boolean go = true;
                switch (Title) {

                    case ("Main Map"):
                        ChangeToMapView();

                        break;

                    case ("Settings"):
                        ContainerViewID = new SettingsFragment();
                        fragmentID = R.id.nav_settings;

                        break;

                    case ("My Buses"):
                        ContainerViewID = new RiderFragment();
                        fragmentID = R.id.nav_myBuses;

                        break;

                    case ("My Bus Control"):
                        ContainerViewID = new BusControlFragment();
                        fragmentID = R.id.nav_busSettings;

                        break;

                    case ("Admin Dashboard Settings"):
                        ContainerViewID = new AdminPanelFragment();
                        fragmentID = R.id.nav_adminDashBoard;

                        break;

                    case ("Comms."):
                        if (InfoClasses.Mode.ADMIN()) {
                            ContainerViewID = new AdminComms();
                            fragmentID = R.id.nav_adminDashBoard;
                        } else {

                        }

                        break;

                    default:

                        FragmentTransaction trans = fragmentManager.beginTransaction();
                        trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                        trans.replace(R.id.nav_host_fragment, new MapFragment()).commit();


                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {

                                        navigationView.setCheckedItem(R.id.nav_map);
                                        InfoClasses.Mode.ChangeToRiderMode(getApplicationContext());
                                    }
                                });
                            }
                        }, 100);


                        go = false;
                }


                if (go) {
                    FragmentTransaction trans = fragmentManager.beginTransaction();
                    trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    trans.replace(R.id.nav_host_fragment, ContainerViewID).commit();
                    navigationView.setCheckedItem(fragmentID);
                }

                return false;
            }
        });


        FragmentTransaction trans = fragmentManager.beginTransaction();
        trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        trans.replace(R.id.nav_host_fragment, new MapFragment()).commit();

        navigationView.setCheckedItem(R.id.nav_map);

        View gifDrawable = findViewById(R.id.GIF);

        gifDrawable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!InfoClasses.Status.Login()) {

                    if (numberOfClicks == 0) {
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {

                                        numberOfClicks = 0;
                                    }
                                });
                            }
                        }, 3000);
                    }

                    numberOfClicks += 1;
                } else {

                    FragmentTransaction trans = fragmentManager.beginTransaction();
                    trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    trans.replace(R.id.nav_host_fragment, new MapFragment()).commit();
                }
            }
        });

        gifDrawable.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (numberOfClicks == 6) {
                    FragmentTransaction trans = fragmentManager.beginTransaction();
                    trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    trans.replace(R.id.nav_host_fragment, new LoginFragment()).commit();
                    numberOfClicks = 0;

                }
                return false;
            }
        });

        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                if (InfoClasses.Status.Login()) {

                    drawer.closeDrawer(drawerView);
                    FragmentTransaction trans = fragmentManager.beginTransaction();
                    trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    trans.replace(R.id.nav_host_fragment, new MapFragment()).commit();
                }
            }
        });

        for (int x = 0; x < items.size(); x++) {
            MainActivity.navigationView.getMenu().findItem(MainActivity.items.get(x)).setVisible(false);
        }


        MapFragment.mainActivity = this;

        //location permission check
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "The permission to get BLE location data is required for proper bus tracking for riders and drivers", Toast.LENGTH_SHORT).show();
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notify";
            String description = "discript";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("343", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        if (InfoClasses.Mode.Rider_Driver == InfoClasses.Mode.DRIVER) {
            Internet.joinRoute_AsBus();
            Internet.fetchYourRoutes(InfoClasses.BusInfo.BusNumber);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void Instantiate_Save_Methods() {

        saveData = new SaveData(getApplicationContext());

        final int temp = SaveData.ReadAppMode();

        String county = SaveData.ReadCounty();

        if (county != null) {

            InfoClasses.commitCounty(county);
        }

        if (temp != -1) {

            if (temp == 2) {

                InfoClasses.Mode.ChangeToAdminMode(getApplicationContext());
            }

            if (temp == 1) {

                InfoClasses.Mode.ChangeToDriverMode(getApplicationContext());

            }

            if (temp == 0) {

                InfoClasses.Mode.ChangeToRiderMode(getApplicationContext());
            }
        } else {

            InfoClasses.Mode.ChangeToRiderMode(getApplicationContext());

        }
    }

    public void Instantiate_Map_Methods() {

        try {

            mapSupportFragment.getMapAsync(this);

        } catch (NullPointerException e) {

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        //When the device is connected to the Google Maps API
        mLocationRequest = new LocationRequest();

        //Set Max refresh interval
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(100);

        //Set Power Usage
        mLocationRequest.setPriority(LocationRequest.PRIORITY_NO_POWER);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        InfoClasses.Markers.getMarkers(getBaseContext());
    }

    @Override
    public void onLocationChanged(Location location) {

        if (InfoClasses.Mode.RIDER()) {
            if (InfoClasses.MyInfo.savedLocation == null) {
                InfoClasses.MyInfo.CurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            }
        }

        if (InfoClasses.Status.Login()) {
            InfoClasses.MyInfo.CurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            Log.i("tag", InfoClasses.MyInfo.CurrentLocation.toString());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }

                        if (InfoClasses.Mode.RIDER() && InfoClasses.MyInfo.savedLocation == null) {

                            mMap.setMyLocationEnabled(true);
                        }

                    }
                } else {
                    Toast.makeText(this, "permission denied, Please re-enable to use this app properly",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Log.e("map", "Maps: ERROR");

    }

    protected synchronized void buildGoogleApiClient() {

        //Build the Google API client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Creates a public instance of the Google map
        mMap = googleMap;
        Instantiate_Save_Methods();

        mMap.isTrafficEnabled();
        //Sets various UI options of the Google map
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (InfoClasses.Mode.ADMIN()) {
            mMap.getUiSettings().setScrollGesturesEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
        }

        try {
            //If the map is successfully loaded set the style to a defined style
            //This style is located in the raw/style_json file
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e("map", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("map", "Can't find style. Error: ", e);
        }


        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
            }
        }

        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.


            return;
        }


        mMap.setMyLocationEnabled(false);

        MainActivity.mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (!InfoClasses.Mode.RIDER()) {

                    markerSelected = false;
                    focus = InfoClasses.countyCenters.get(InfoClasses.county);
                } else {

                }
            }
        });

        if (InfoClasses.Mode.RIDER()) {

            if (InfoClasses.MyInfo.savedLocation == null) {
                mMap.setMyLocationEnabled(true);
            }

            MainActivity.mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {

                    if (InfoClasses.Status.Map()) {

                        if (marker.getTag() == InfoClasses.MyInfo.marker.getTag()) {

                            if (InfoClasses.MyInfo.savedLocation == null) {

                                InfoClasses.MyInfo.CurrentLocation = InfoClasses.MyInfo.marker.getPosition();
                                InfoClasses.MyInfo.savedLocation = InfoClasses.MyInfo.marker.getPosition();

                                SaveData.SaveMyHomePos(InfoClasses.MyInfo.marker.getPosition());

                                Internet.ReverseGeoCode RGC = new Internet.ReverseGeoCode();
                                RGC.execute(InfoClasses.MyInfo.CurrentLocation.latitude, InfoClasses.MyInfo.CurrentLocation.longitude);

                                Toast.makeText(getBaseContext(), "You just set your default address to this position", Toast.LENGTH_LONG).show();

                                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                }
                                mMap.setMyLocationEnabled(false);

                                InfoClasses.MyInfo.marker.setTitle("Loading...");
                                InfoClasses.MyInfo.marker.setSnippet("This is your saved address");

                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                if (InfoClasses.MyInfo.Address != null) {
                                                    InfoClasses.MyInfo.marker.setTitle(InfoClasses.MyInfo.Address);
                                                    InfoClasses.MyInfo.marker.showInfoWindow();
                                                } else {

                                                    InfoClasses.MyInfo.marker.setTitle("Error Getting Address");
                                                    InfoClasses.MyInfo.marker.showInfoWindow();
                                                }
                                            }
                                        });
                                    }
                                }, 1000);

                            } else {
                                Toast.makeText(getBaseContext(), "This is already your current location", Toast.LENGTH_LONG).show();
                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                v.vibrate(100);
                            }
                            markerSelected = false;
                        } else {
                            markerSelected = true;
                            focus = marker.getPosition();
                        }
                        return false;
                    }

                    return false;
                }
            });

        }
        updates(true);

        if (InfoClasses.Mode.RIDER()) {


        } else if (InfoClasses.Mode.DRIVER()) {

            InfoClasses.BusInfo.marker = googleMap.addMarker(new MarkerOptions()
                    .icon(InfoClasses.BusInfo.CurrentRoute == null ? InfoClasses.Markers.BusBitmapGREY : InfoClasses.Markers.BusBitmap)
                    .position(InfoClasses.countyLocation())
                    .title("My Current Bus Location")
                    .snippet("Currently performing route: " + InfoClasses.BusInfo.CurrentRoute)
                    .visible(true));

        } else {

            InfoClasses.AdminInfo.recreateMarkers();
        }

        MapFragment.RecreateMapObjects();

    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    private void StartRefresh() {


        Timer timer = new Timer();
        TimerTask myTask = new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    public void run() {
                        updates(false);
                    }
                });
            }
        };
        timer.schedule(myTask, 1000, 1000);

    }

    private static double distanceBetweenLocations(LatLng x, LatLng y) {

        double q = x.latitude - y.latitude;
        double p = x.longitude - y.longitude;

        return Math.sqrt(Math.pow(q, 2) + Math.pow(p, 2)) * 250000;
    }

    static public void ModeJustChanged() {


        for (int x = 0; x < items.size(); x++) {
            MainActivity.navigationView.getMenu().findItem(MainActivity.items.get(x)).setVisible(true);

            MainActivity.navigationView.getMenu().findItem(MainActivity.items.get(x)).setVisible(MainActivity.activeArray.get(x));
        }

        MapFragment.RecreateMapObjects();
        MapFragment.unfocusMap();
        UpdatesAvailable = true;
    }

    public static void ChangeToMapView() {
        if (!InfoClasses.Status.Map()) {
            FragmentTransaction trans = fragmentManager.beginTransaction();
            trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            trans.replace(R.id.nav_host_fragment, new MapFragment()).commit();
            MainActivity.UpdatesAvailable = true;
            navigationView.setCheckedItem(R.id.nav_map);

            InfoClasses.Status.ActiveFragment = InfoClasses.Status.Map;
            updates(true);
        }
    }

    public static void OpenLOGIN() {

        InfoClasses.Login.reset();
        SaveData.SaveKEY("");

        FragmentTransaction trans = fragmentManager.beginTransaction();
        trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        trans.replace(R.id.nav_host_fragment, new LoginFragment()).commit();
    }

    public void hideKeyboard() {

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        InfoClasses.BusInfo.disconnectFromBus(this);
        super.onDestroy();
    }

    public static void updates(boolean bypass) {

        if (mMap != null) {
            zoom = mMap.getCameraPosition().zoom;

            if (!InfoClasses.Mode.DRIVER()) {
                if (panicButton.getVisibility() != View.GONE)
                    panicButton.setVisibility(View.GONE);
            }
            if (InfoClasses.Mode.DRIVER()) {

                if (!InfoClasses.Bluetooth.isConnected) {
                    if (panicButton.getVisibility() != View.GONE)
                        panicButton.setVisibility(View.GONE);
                } else {

                    if (panicButton.getVisibility() != View.VISIBLE)
                        panicButton.setVisibility(View.VISIBLE);
                }

                if (InfoClasses.BusInfo.AssignedBusRoutes != null) {

                    boolean pass = false;

                    if (routes != InfoClasses.BusInfo.AssignedBusRoutes) {
                        routes = InfoClasses.BusInfo.AssignedBusRoutes;
                        pass = true;

                    } else {

                        for (int x = 0; x < routes.size(); x++) {

                            if (!routes.get(x).equals(InfoClasses.BusInfo.AssignedBusRoutes.get(x))) {
                                pass = true;

                            }
                        }
                    }

                    if (pass) {

                        //The Routes Have updated!!!
                    }
                }
                if (UpdatesAvailable || bypass) {
                    UpdatesAvailable = false;
                }

                if (InfoClasses.Bluetooth.isConnected) {

                    MapFragment.updatePosition();

                    if (MapFragment.coordAuthenticatior(InfoClasses.BusInfo.BusLocation)) {

                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(InfoClasses.BusInfo.BusLocation)
                                .zoom(17) // Gets the correct zoom factor from the distance vairabe
                                .bearing(90)                // Sets the orientation of the camera to east
                                .tilt(65)                   // Sets the tilt of the camera to 65 degrees
                                .build();                   // Creates a CameraPosition from the builder

                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


                    }
                } else {

                    MapFragment.unfocusMap();
                }

            } else if (InfoClasses.Mode.RIDER()) {

                if (InfoClasses.MyInfo.savedLocation == null) {
                    if (MapFragment.coordAuthenticatior(InfoClasses.MyInfo.CurrentLocation)) {
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(InfoClasses.MyInfo.CurrentLocation)
                                .zoom(17) // Gets the correct zoom factor from the distance vairabe
                                .bearing(90)                // Sets the orientation of the camera to east
                                .tilt(65)                   // Sets the tilt of the camera to 65 degrees
                                .build();                   // Creates a CameraPosition from the builder

                        InfoClasses.MyInfo.marker.setPosition(InfoClasses.MyInfo.CurrentLocation);
                        InfoClasses.MyInfo.marker.setVisible(true);
                        InfoClasses.MyInfo.marker.showInfoWindow();
                        InfoClasses.MyInfo.marker.setIcon(InfoClasses.Markers.MyBitmap);
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    }

                } else {
                    for (String route : InfoClasses.MyInfo.myBuses.keySet()) {

                        InfoClasses.Buses bus = InfoClasses.MyInfo.myBuses.get(route);
                        bus.counter -= 1;
                        if (bus.counter == 0) {

                            bus.counter = 15;
                            bus.Active = false;
                            bus.marker.setVisible(false);
                            InfoClasses.MyInfo.myBuses.remove(route);
                        } else {

                            bus.Active = true;
                        }
                    }

                    if (UpdatesAvailable || bypass) {

                        UpdatesAvailable = false;
                        if (InfoClasses.MyInfo.NewBus || bypass) {
                            InfoClasses.MyInfo.NewBus = false;
                            InfoClasses.MyInfo.marker.setVisible(true);
                            InfoClasses.MyInfo.marker.setPosition(InfoClasses.MyInfo.savedLocation);
                        }

                        InfoClasses.MyInfo.updateBusPositions();

                        ArrayList<LatLng> positions = new ArrayList<>();
                        for (InfoClasses.Buses bus : InfoClasses.MyInfo.myBuses.values()) {

                            if (bus.Active) {
                                positions.add(bus.BusLocation);
                            }
                        }

                        double longestLength = 200;

                        for (LatLng p : positions) {

                            double i = distanceBetweenLocations(InfoClasses.MyInfo.CurrentLocation, p);

                            if (i > longestLength) {
                                longestLength = i;
                            }
                        }

                        if (!markerSelected) {
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(InfoClasses.MyInfo.savedLocation)
                                    .zoom((float) -(Math.log(longestLength * 19 / 591657550.5) / Math.log(2))) // Gets the correct zoom factor from the distance vairabe
                                    .bearing(90)                // Sets the orientation of the camera to east
                                    .build();                   // Creates a CameraPosition from the builder
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        }
                    }
                }
            } else if (InfoClasses.Mode.ADMIN()) {

                if (UpdatesAvailable || bypass) {

                    UpdatesAvailable = false;

                    if (bypass || InfoClasses.AdminInfo.updateAllLocations) {
                        InfoClasses.AdminInfo.updateAllLocations = false;
                        InfoClasses.AdminInfo.recreateMarkers();

                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(focus)
                                .zoom(10.3f) // Gets the correct zoom factor from the distance vairabe
                                .bearing(0)                // Sets the orientation of the camera to east
                                .build();                   // Creates a CameraPosition from the builder
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    } else {

                        if (markerSelected) {
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(focus)
                                    .zoom(zoom) // Gets the correct zoom factor from the distance vairabe
                                    .bearing(0)                // Sets the orientation of the camera to east
                                    .build();                   // Creates a CameraPosition from the builder
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    }

                    InfoClasses.AdminInfo.updateBusPositions();
                }
            }
        }
    }
}
