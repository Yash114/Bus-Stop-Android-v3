package com.gingergear.BusStopv3.ui.Settings;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
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

import com.gingergear.BusStopv3.InfoClasses;
import com.gingergear.BusStopv3.MainActivity;
import com.gingergear.BusStopv3.R;
import com.gingergear.BusStopv3.SaveData;
import com.gingergear.BusStopv3.ui.Map.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.Objects;

public class fragment extends Fragment {

    private model homeViewModel;
    private Button resetAddressButton;
    private TextView statusBar;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        InfoClasses.Status.ActiveFragment = InfoClasses.Status.Setting;

        homeViewModel = ViewModelProviders.of(this).get(model.class);
        View root = inflater.inflate(R.layout.settings_fragment, container, false);

        statusBar = root.findViewById(R.id.statusBar);
        resetAddressButton = root.findViewById(R.id.ResetAddress);

        if(InfoClasses.myInfo.savedLocation != null) {

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

                    MainActivity.mMap.setMyLocationEnabled(true);
                    SaveData.SaveMyHomePos(new LatLng(0, 0));
                    InfoClasses.myInfo.savedLocation = null;
                    InfoClasses.myInfo.CurrentLocation = null;

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