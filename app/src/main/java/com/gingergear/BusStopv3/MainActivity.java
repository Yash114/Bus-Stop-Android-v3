package com.gingergear.BusStopv3;

import android.Manifest;
import android.app.ActionBar;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.gingergear.BusStopv3.ui.BusControl.BusControlFragment;
import com.gingergear.BusStopv3.ui.Map.MapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.gingergear.BusStopv3.SaveData.Save;
import static com.gingergear.BusStopv3.SaveData.SaveBus;
import static com.gingergear.BusStopv3.SaveData.SaveMyBus;
import static com.gingergear.BusStopv3.SaveData.readMySavedPos;
import static com.gingergear.BusStopv3.SaveData.readSaved;
import static com.gingergear.BusStopv3.SaveData.readMySavedBus;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;

    //Save Methods
    public static SaveData saveData;

    //fragment thing
    public static FragmentManager fragmentManager;
    public static MenuInflater menuInflater;
    public static MapFragment mapFragment;


    //things with DriverFrag
    private static ArrayList<String> routes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuInflater = getMenuInflater();

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

        if(InfoClasses.Mode.Rider_Driver == InfoClasses.Mode.DRIVER){
            Internet.joinRoute_AsBus(InfoClasses.busInfo.BusNumber);
            Internet.fetchYourRoutes(InfoClasses.busInfo.BusNumber);
        }

        Instantiate_Save_Methods("BusStopStorage");
        StartRefresh();

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

    private void Instantiate_Save_Methods(String fileName) {

        saveData = new SaveData(this, fileName);

        try {
            if (readSaved("mode") != null) {
                InfoClasses.Mode.Rider_Driver = readSaved("mode").equals("rider") ? 1 : 0;
            }

            if (SaveData.readSavedBus("BusNumber") != null && InfoClasses.Mode.Rider_Driver == 1) {
                InfoClasses.busInfo.BusNumber = SaveData.readSavedBus("BusNumber");

            }

            if (SaveData.readSavedBus("MyBusNumber") != null && InfoClasses.Mode.Rider_Driver == 0) {
                InfoClasses.busInfo.BusNumber = SaveData.readMySavedBus("MyBusNumber");

            }

            if (SaveData.readMySavedPos() != null && InfoClasses.Mode.Rider_Driver == 1) {
                InfoClasses.myInfo.CurrentLocation = readMySavedPos();

            }

        } catch (NullPointerException e) {
        }
    }

    public static void GoToMap() {

        fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, new MapFragment()).commit();


    }

    private void StartRefresh() {

        Timer timer = new Timer();
        TimerTask myTask = new TimerTask() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    public void run() {

                        if (InfoClasses.busInfo.AssignedBusRoutes != null) {

                            boolean pass = false;

                            if (routes != InfoClasses.busInfo.AssignedBusRoutes) {
                                routes = InfoClasses.busInfo.AssignedBusRoutes;
                                pass = true;

                            } else {

                                for (int x = 0; x < routes.size(); x++) {

                                    if (!routes.get(x).equals(InfoClasses.busInfo.AssignedBusRoutes.get(x))) {
                                        pass = true;

                                    }
                                }
                            }

                            if (pass) {

                                //The Routes Have updated!!!
                            }
                        }

                        if (InfoClasses.Mode.Rider_Driver == InfoClasses.Mode.DRIVER) {
                            if (InfoClasses.busInfo.BusLocation != null && MapFragment.coordAuthenticatior(InfoClasses.busInfo.BusLocation)) {
                                MapFragment.RecreateMapObjects();

                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(InfoClasses.busInfo.BusLocation)
                                        .zoom(17) // Gets the correct zoom factor from the distance vairabe
                                        .bearing(90)                // Sets the orientation of the camera to east
                                        .tilt(65)                   // Sets the tilt of the camera to 65 degrees
                                        .build();                   // Creates a CameraPosition from the builder

                                MapFragment.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                MapFragment.theBus.setPosition(InfoClasses.busInfo.BusLocation);
                            }
                        }
                    }

                });
            }
        };
        timer.schedule(myTask, 100, 250);

    }

    @Override
    protected void onDestroy() {
        InfoClasses.busInfo.disconnectFromBus(this);
        super.onDestroy();
    }
}