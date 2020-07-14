package com.gingertech.BusStopv3.ui.Settings;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.gingertech.BusStopv3.InfoClasses;
import com.gingertech.BusStopv3.MainActivity;
import com.gingertech.BusStopv3.R;
import com.gingertech.BusStopv3.SaveData;
import com.google.android.gms.maps.model.LatLng;

public class SettingsFragment extends Fragment {

    private model homeViewModel;
    private Button resetAddressButton;
    private TextView statusBar;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        InfoClasses.Status.ActiveFragment = InfoClasses.Status.Setting;
        InfoClasses.Mode.ChangeToRiderMode(getContext());

        homeViewModel = ViewModelProviders.of(this).get(model.class);
        View root = inflater.inflate(R.layout.settings_fragment, container, false);

        statusBar = root.findViewById(R.id.statusBar);
        resetAddressButton = root.findViewById(R.id.ResetAddress);

        if(InfoClasses.MyInfo.savedLocation != null) {

            resetAddressButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }

                    InfoClasses.MyInfo.ZonedSchools.clear();
                    InfoClasses.MyInfo.ZonedSchools.add("null");
                    InfoClasses.MyInfo.ZonedSchools.add("null");
                    InfoClasses.MyInfo.ZonedSchools.add("null");

                    InfoClasses.MyInfo.BusRoutes.clear();
                    InfoClasses.MyInfo.BusRoutes.add("null");
                    InfoClasses.MyInfo.BusRoutes.add("null");
                    InfoClasses.MyInfo.BusRoutes.add("null");
                    SaveData.SaveBusRoutes();

                    InfoClasses.MyInfo.myBuses.clear();

                    MainActivity.mMap.setMyLocationEnabled(true);
                    InfoClasses.MyInfo.savedLocation = null;
                    InfoClasses.MyInfo.CurrentLocation = null;
                    SaveData.SaveMyHomePos(new LatLng(0, 0));
                    SaveData.SaveMyHomeAddy("");



                    statusBar.setText("Address is missing");

                    Toast.makeText(getContext(), "Save an address first!", Toast.LENGTH_SHORT).show();
                    Vibrator j = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    j.vibrate(300);

                    resetAddressButton.setClickable(false);

                }
            });
        } else {
            statusBar.setText("Address is missing");

            Toast.makeText(getContext(), "Save an address first!", Toast.LENGTH_SHORT).show();
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(300);

            resetAddressButton.setClickable(false);
        }

        return root;
    }
}