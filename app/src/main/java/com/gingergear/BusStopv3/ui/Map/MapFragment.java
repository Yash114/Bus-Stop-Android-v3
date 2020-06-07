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
import android.widget.Toast;

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

public class MapFragment extends Fragment {

    static public LatLng Last_Bus_Current_Location;
    private static LatLng temp;

    //map Objects
    public static PolylineOptions mpolyline;
    public static String time = "~ ";
    public static Marker theBus;
    public static Marker me;

    private static BitmapDescriptor myIcon;
    private static BitmapDescriptor busIcon;

    private static Boolean unFocused = true;

    //the one true MainActivity
    public static MainActivity mainActivity;

    View root;

    private MapViewModel mapViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        InfoClasses.Status.ActiveFragment = InfoClasses.Status.Map;

        temp = null;
        Last_Bus_Current_Location = null;

        mapViewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        root = inflater.inflate(R.layout.map_fragment, container, false);

        myIcon = BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(getContext(), R.drawable.kid));
        busIcon = BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(getContext(), R.drawable.bus));

        try {
            MainActivity.mapSupportFragment = (SupportMapFragment)
                    getChildFragmentManager().findFragmentById(R.id.map);

            Log.i("map", "created");

        } catch (NullPointerException e) { }

        mainActivity.Instantiate_Map_Methods();

        return root;
    }


    public static boolean coordAuthenticatior(LatLng Coords) {

        if ((InfoClasses.Bluetooth.isConnected && InfoClasses.Status.inRoute && InfoClasses.Mode.Rider_Driver == InfoClasses.Mode.DRIVER) || InfoClasses.Mode.Rider_Driver == InfoClasses.Mode.RIDER) {
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

        MainActivity.mMap.clear();

        me = MainActivity.mMap.addMarker(new MarkerOptions()
                .icon(myIcon)
                .position(InfoClasses.myInfo.CurrentLocation)
                .title("Your Current Location")
                .snippet("Click me to set this as your address!")
                .visible(true));

        if(InfoClasses.myInfo.savedLocation != null){
            MapFragment.me.setTitle(InfoClasses.myInfo.Address);
            MapFragment.me.setSnippet("This is your saved address");
        }

        me.setTag("My Location");
        me.showInfoWindow();

        theBus = MainActivity.mMap.addMarker(new MarkerOptions()
                .icon(busIcon)
                .position(new LatLng(84.2f, 84.2f))
                .visible(true));

        theBus.setTag("Bus' Location");
        try {
            MainActivity.mMap.addPolyline(mpolyline);

            MainActivity.mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {

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
            MainActivity.mMap.clear();
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(0, 0))
                    .zoom(0)
                    .build();

            MainActivity.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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