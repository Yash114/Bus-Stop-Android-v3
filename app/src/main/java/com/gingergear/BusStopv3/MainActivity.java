package com.gingergear.BusStopv3;

import android.Manifest;
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
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.gingergear.BusStopv3.ui.Map.MapFragment;
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
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static com.gingergear.BusStopv3.SaveData.ReadMySavedPos;

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


    //things with DriverFrag
    private static ArrayList<String> routes;

    //UI variables
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    LocationRequest mLocationRequest;

    //Map Stuff
    public static GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    public static SupportMapFragment mapSupportFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuInflater = getMenuInflater();

        Instantiate_Save_Methods();
        Instantiate_Map_Methods();
        checkLocationPermission();

        fragmentManager = getSupportFragmentManager();
        Internet.CreateWebSocketConnection();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_map)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        MapFragment.mainActivity = this;

        //location permission check
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "The permission to get BLE location data is required", Toast.LENGTH_SHORT).show();
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
            Internet.joinRoute_AsBus(InfoClasses.DriverBus.BusNumber);
            Internet.fetchYourRoutes(InfoClasses.DriverBus.BusNumber);
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

    private void Instantiate_Save_Methods() {

        saveData = new SaveData(getApplicationContext());

        if (SaveData.ReadAppMode() != -1) {

            InfoClasses.Mode.Rider_Driver = SaveData.ReadAppMode();
            Log.i("tag", InfoClasses.Mode.Rider_Driver == 0 ? "RIDER" : "DRIVER");
        }

        if (InfoClasses.Mode.DRIVER()) {

        }

        if (InfoClasses.Mode.RIDER()) {

            LatLng savedPos = ReadMySavedPos();

            if (savedPos != null) {
                if (savedPos.latitude != 0) {

                    InfoClasses.myInfo.savedLocation = savedPos;
                    InfoClasses.myInfo.CurrentLocation = savedPos;
                    InfoClasses.myInfo.Address = SaveData.ReadMySavedAddy();

                    Log.i("tag", InfoClasses.myInfo.Address);

                }
            }

            //TODO implement the adding in buses with the routes and bus numbers
            //TODO And test to see if this now composite save functions work

            ArrayList<String> routes = Objects.requireNonNull(SaveData.ReadMySavedRoutes())[0];
            if(routes != null) {
                int x = 0;
                InfoClasses.myInfo.BusRoutes.clear();
                for(String s : routes) {
                    x += 1;
                    InfoClasses.myInfo.BusRoutes.add(s);
                }
                for(int y = 0; y < x; y++){
                    InfoClasses.myInfo.BusRoutes.add("null");

                }
            }

            ArrayList<String> buses = Objects.requireNonNull(SaveData.ReadMySavedRoutes())[1];
            if(routes != null) {
                int x = 0;
                InfoClasses.myInfo.ZonedSchools.clear();
                for(String s : routes) {
                    x += 1;
                    InfoClasses.myInfo.ZonedSchools.add(s);
                }
                for(int y = 0; y < x; y++){
                    InfoClasses.myInfo.ZonedSchools.add("null");

                }
            }

        }
    }

    public void Instantiate_Map_Methods() {

        try {

            mapSupportFragment.getMapAsync(this);
            Log.i("map", "created");

        } catch (NullPointerException e) {

        }
    }

    public static void GoToMap() {

        fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, new MapFragment()).commit();


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

        Log.i("map", "Maps: Success");

    }

    @Override
    public void onLocationChanged(Location location) {

        if (InfoClasses.Mode.RIDER()) {
            if (InfoClasses.myInfo.savedLocation == null) {
                InfoClasses.myInfo.CurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            }
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

                    }
                } else {
//                    Toast.makeText(this, "permission denied",
//                            Toast.LENGTH_LONG).show();
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

        //Sets various UI options of the Google map
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

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
        } else {
            buildGoogleApiClient();
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

        if (InfoClasses.Mode.RIDER()) {

            if (InfoClasses.myInfo.savedLocation == null) {
                mMap.setMyLocationEnabled(true);
            }


            MainActivity.mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (marker.getTag() == MapFragment.me.getTag()) {

                        if (InfoClasses.myInfo.savedLocation == null) {

                            InfoClasses.myInfo.CurrentLocation = MapFragment.me.getPosition();
                            InfoClasses.myInfo.savedLocation = MapFragment.me.getPosition();

                            SaveData.SaveMyHomePos(MapFragment.me.getPosition());

                            Internet.ReverseGeoCode RGC = new Internet.ReverseGeoCode();
                            RGC.execute(InfoClasses.myInfo.CurrentLocation.latitude, InfoClasses.myInfo.CurrentLocation.longitude);

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

                            MapFragment.me.setTitle("Loading...");
                            MapFragment.me.setSnippet("This is your saved address");

                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            MapFragment.me.setTitle(InfoClasses.myInfo.Address);
                                            MapFragment.me.showInfoWindow();
                                        }
                                    });
                                }
                            }, 700);

                        } else {
                            Toast.makeText(getBaseContext(), "This is already your current location", Toast.LENGTH_LONG).show();
                            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            v.vibrate(100);
                        }

                    }
                    return false;
                }
            });
        }

        StartRefresh();

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

                        if (InfoClasses.Mode.DRIVER()) {
                            if (InfoClasses.DriverBus.AssignedBusRoutes != null) {

                                boolean pass = false;

                                if (routes != InfoClasses.DriverBus.AssignedBusRoutes) {
                                    routes = InfoClasses.DriverBus.AssignedBusRoutes;
                                    pass = true;

                                } else {

                                    for (int x = 0; x < routes.size(); x++) {

                                        if (!routes.get(x).equals(InfoClasses.DriverBus.AssignedBusRoutes.get(x))) {
                                            pass = true;

                                        }
                                    }
                                }

                                if (pass) {

                                    //The Routes Have updated!!!
                                }
                            }
                            if (InfoClasses.DriverBus.BusLocation != null && MapFragment.coordAuthenticatior(InfoClasses.DriverBus.BusLocation)) {
                                MapFragment.RecreateMapObjects();

                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(InfoClasses.DriverBus.BusLocation)
                                        .zoom(17) // Gets the correct zoom factor from the distance vairabe
                                        .bearing(90)                // Sets the orientation of the camera to east
                                        .tilt(65)                   // Sets the tilt of the camera to 65 degrees
                                        .build();                   // Creates a CameraPosition from the builder

                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                MapFragment.theBus.setPosition(InfoClasses.DriverBus.BusLocation);
                                MapFragment.theBus.setVisible(true);
                                MapFragment.theBus.showInfoWindow();


                            }
                        } else if (InfoClasses.Mode.RIDER()) {

                            if (MapFragment.coordAuthenticatior(InfoClasses.myInfo.CurrentLocation)) {

                                MapFragment.RecreateMapObjects();

                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(InfoClasses.myInfo.CurrentLocation)
                                        .zoom(17) // Gets the correct zoom factor from the distance vairabe
                                        .bearing(90)                // Sets the orientation of the camera to east
                                        .tilt(65)                   // Sets the tilt of the camera to 65 degrees
                                        .build();                   // Creates a CameraPosition from the builder

                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                MapFragment.me.setPosition(InfoClasses.myInfo.CurrentLocation);
                                MapFragment.me.setVisible(true);
                                MapFragment.me.showInfoWindow();
                            }
                        }
                    }

                });
            }
        };
        timer.schedule(myTask, 100, 250);

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

    static void ModeJustChanged() {

        MapFragment.RecreateMapObjects();
        MapFragment.unfocusMap();
    }

    @Override
    protected void onDestroy() {
        InfoClasses.DriverBus.disconnectFromBus(this);
        super.onDestroy();
    }
}