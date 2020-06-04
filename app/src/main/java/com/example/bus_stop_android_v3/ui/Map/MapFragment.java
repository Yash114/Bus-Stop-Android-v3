package com.example.bus_stop_android_v3.ui.Map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.bus_stop_android_v3.MainActivity;
import com.example.bus_stop_android_v3.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapFragment extends Fragment   implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //Main Objects
    static public int NORMAL = 0;
    static public int Not_Joined = -1;
    static public int Connection_notConnected = -2;
    static public int Disconnected = -3;
    static public int PAUSED = -4;
    static public int DONE = 1;
    static public int Status = Not_Joined;
    static public int DRIVER = 0;
    static public int RIDER = 1;
    static public int Rider_Driver = 1;
    static public LatLng My_Current_Location;
    static public LatLng Busses_Current_Location;
    static public LatLng Last_Bus_Current_Location;

    //map Objects
    public static GoogleMap mMap;
    public static PolylineOptions mpolyline;
    public static String time = "~ ";

    //UI variables
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    LocationRequest mLocationRequest;

    private GoogleApiClient mGoogleApiClient;

    public static int unFocused = 1;
    View root;

    private MapViewModel mapViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapViewModel =
                ViewModelProviders.of(this).get(MapViewModel.class);
        root = inflater.inflate(R.layout.map_fragment, container, false);

        Instantiate_Map_Methods();
        return root;
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
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        Log.i("map", "Maps: Success");

    }

    @Override
    public void onLocationChanged(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        My_Current_Location = latLng;

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
                    if (ContextCompat.checkSelfPermission(getContext(),
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
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
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
        mMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        try {
            //If the map is successfully loaded set the style to a defined style
            //This style is located in the raw/style_json file
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.style_json));

            if (!success) {
                Log.e("map", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("map", "Can't find style. Error: ", e);
        }


        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
            }
        } else {
            buildGoogleApiClient();
        }

        if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.


            return;
        }

        //Allow the map to access the devices location
        mMap.setMyLocationEnabled(false);
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    private void Instantiate_Map_Methods() {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        try {
            SupportMapFragment mapFragment = (SupportMapFragment)
                    getChildFragmentManager().findFragmentById(R.id.map);

            mapFragment.getMapAsync(this);
            Log.i("map", "created");
        } catch (NullPointerException e){

        }
    }
}