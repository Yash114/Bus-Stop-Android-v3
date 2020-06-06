package com.gingergear.BusStopv3.ui.Map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.gingergear.BusStopv3.InfoClasses;
import com.gingergear.BusStopv3.MainActivity;
import com.gingergear.BusStopv3.R;
import com.gingergear.BusStopv3.ui.BusControl.BusControlFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
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
    private static LatLng temp;

    //map Objects
    public static GoogleMap mMap;
    public static PolylineOptions mpolyline;
    public static String time = "~ ";
    public static Marker theBus;
    public static Marker me;

    private static BitmapDescriptor myIcon;
    private static BitmapDescriptor busIcon;

    //UI variables
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    LocationRequest mLocationRequest;

    private GoogleApiClient mGoogleApiClient;

    private static Boolean unFocused = true;

    View root;

    private MapViewModel mapViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        InfoClasses.Status.ActiveFragment = InfoClasses.Status.Map;

        mapViewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        root = inflater.inflate(R.layout.map_fragment, container, false);

        Instantiate_Map_Methods();

        myIcon = BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(getContext(), R.drawable.kid));
        busIcon = BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(getContext(), R.drawable.bus));

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

    public static boolean coordAuthenticatior(LatLng Coords) {

        if((InfoClasses.Bluetooth.isConnected && InfoClasses.Status.inRoute && InfoClasses.Mode.Rider_Driver == InfoClasses.Mode.DRIVER) || InfoClasses.Mode.Rider_Driver == InfoClasses.Mode.RIDER) {
            if (Coords != null) {
                if (temp == null) {
                    if (Last_Bus_Current_Location != null) {
                        if (Last_Bus_Current_Location != Coords) {

//                        if (getDirectionsData.getStatus() != AsyncTask.Status.RUNNING) {
//
//                            getDirectionsData.execute(My_Current_Location, Busses_Current_Location);
//                            getDirectionsData = internet.new GetDirectionsData();
//                        }

                            if (Math.abs(Coords.latitude - Last_Bus_Current_Location.latitude) < 0.2 &&
                                    Math.abs(Coords.longitude - Last_Bus_Current_Location.longitude) < 0.2) {

                                if (Math.abs(Last_Bus_Current_Location.latitude - Coords.latitude) > 0.00001 ||
                                        Math.abs(Last_Bus_Current_Location.longitude - Coords.longitude) > 0.000010) {

                                    if (Coords != Last_Bus_Current_Location) {
                                        Last_Bus_Current_Location = Coords;
                                        unFocused = false;
                                        return true;
                                    }
                                }
                            }
                        }
                    } else {

                        temp = Coords;
                        Last_Bus_Current_Location = Coords;
                        return false;

                    }
                } else if (Math.abs(Coords.latitude - temp.latitude) < 0.2 &&
                        Math.abs(Coords.longitude - temp.longitude) < 0.2) {

                    temp = null;
                    Last_Bus_Current_Location = Coords;
                    unFocused = false;
                    return true;

                }

            }
        } else {
            unfocusMap();
        }

        return false;
    }

    public static void RecreateMapObjects() {

        mMap.clear();

        me = mMap.addMarker(new MarkerOptions()
                .icon(myIcon)
                .position(new LatLng(84.2f, 84.2f))
                .visible(true));

        theBus = mMap.addMarker(new MarkerOptions()
                .icon(busIcon)
                .position(new LatLng(84.2f, 84.2f))
                .visible(true));

        try {
            mMap.addPolyline(mpolyline);

            mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {

                @Override
                public void onPolylineClick(Polyline polyline) {

                    Log.i("intern", "Please move to a residential area");
                }
            });

        } catch (NullPointerException e) {
        }
    }

    public static void unfocusMap() {

        if (!unFocused) {
            mMap.clear();
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(0, 0))
                    .zoom(0)
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            unFocused = true;
        }
    }

    private static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
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