package com.gingergear.BusStopv3.ui.Map;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.gingergear.BusStopv3.InfoClasses;
import com.gingergear.BusStopv3.MainActivity;
import com.gingergear.BusStopv3.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapFragment extends Fragment {

    static public LatLng Last_Bus_Current_Location;
    private static LatLng temp;

    //map Objects
    public static PolylineOptions mpolyline;
    public static String time = "~ ";

    private static Boolean unFocused = true;

    //the one true MainActivity
    public static MainActivity mainActivity;

    View root;

    private MapViewModel mapViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        temp = null;
        Last_Bus_Current_Location = null;

        mapViewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        root = inflater.inflate(R.layout.map_fragment, container, false);

        try {
            MainActivity.mapSupportFragment = (SupportMapFragment)
                    getChildFragmentManager().findFragmentById(R.id.map);

            Log.i("map", "created");

        } catch (NullPointerException e) { }

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        mainActivity.Instantiate_Map_Methods();
        MainActivity.UpdatesAvailable = true;
        MainActivity.markerSelected = false;
        if(InfoClasses.Status.ActiveFragment != -1) {
            MainActivity.updates(true);
        }
        InfoClasses.Status.ActiveFragment = InfoClasses.Status.Map;

    }

    public static boolean coordAuthenticatior(LatLng Coords) {

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
                                        Math.abs(Last_Bus_Current_Location.longitude - Coords.longitude) > 0.00001) {

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
        return false;
    }

    public static void RecreateMapObjects() {
        InfoClasses.MyInfo.marker = MainActivity.mMap.addMarker(new MarkerOptions()
                .icon(InfoClasses.Markers.MyBitmap)
                .position(new LatLng(0, 0))
                .title("Your Current Location")
                .snippet("Click me to set this as your address!")
                .visible(false));

        if(InfoClasses.MyInfo.savedLocation != null){
            InfoClasses.MyInfo.marker.setTitle(InfoClasses.MyInfo.Address);
            InfoClasses.MyInfo.marker.setSnippet("This is your saved address");
        }

        InfoClasses.MyInfo.marker.setTag("My Location");

        InfoClasses.BusInfo.marker = MainActivity.mMap.addMarker(new MarkerOptions()
                .icon(InfoClasses.Markers.BusBitmap)
                .position(new LatLng(0, 0))
                .visible(false));

        if(InfoClasses.Mode.DRIVER()){
            InfoClasses.BusInfo.marker.setTitle("My Current Bus Location");
            InfoClasses.BusInfo.marker.setSnippet("Currently performing route: " + InfoClasses.BusInfo.CurrentRoute);
        }

        InfoClasses.BusInfo.marker.setTag("Bus' Location");
//
        for(InfoClasses.Buses bus : InfoClasses.MyInfo.myBuses.values()){

            if(bus.marker == null){
                bus.createMarker();
            }
        }
//
//        try {
//            MainActivity.mMap.addPolyline(mpolyline);
//
//            MainActivity.mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
//
//                @Override
//                public void onPolylineClick(Polyline polyline) {
//
//                    Log.i("intern", "Please move to a residential area");
//                }
//            });
//
//        } catch (NullPointerException e) {
//        }

        temp = null;
        Last_Bus_Current_Location = null;
    }

    public static void unfocusMap() {

        if (!unFocused) {
            MainActivity.mMap.clear();
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(0, 0))
                    .zoom(0)
                    .build();

            MainActivity.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            unFocused = true;
        }
    }

    public static void updatePosition(final Marker myMarker, LatLng toPosition) {

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1000;

        final BounceInterpolator interpolator = new BounceInterpolator();
        myMarker.setVisible(true);

        double Lat = toPosition.latitude - myMarker.getPosition().latitude;
        double Lng = toPosition.longitude - myMarker.getPosition().longitude;
        final LatLng begin = myMarker.getPosition();
        final LatLng distanceCoor = new LatLng(Lat, Lng);
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);

                myMarker.setPosition(new LatLng(begin.latitude + elapsed * (distanceCoor.latitude / duration), begin.longitude + elapsed * (distanceCoor.longitude / duration)));

                if (t > 0.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }
}