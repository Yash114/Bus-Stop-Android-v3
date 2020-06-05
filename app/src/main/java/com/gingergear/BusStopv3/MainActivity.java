package com.gingergear.BusStopv3;

import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                Toast.makeText(this, "The permission to get BLE location data is required", Toast.LENGTH_SHORT).show();
            }else{
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }else{
            Toast.makeText(this, "Location permissions already granted", Toast.LENGTH_SHORT).show();
        }

        Instantiate_Save_Methods("BusStopStorage");
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
}