package com.gingergear.BusStopv3.ui.RiderBus;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gingergear.BusStopv3.InfoClasses;
import com.gingergear.BusStopv3.Internet;
import com.gingergear.BusStopv3.MainActivity;
import com.gingergear.BusStopv3.R;
import com.gingergear.BusStopv3.SaveData;
import com.gingergear.BusStopv3.ui.Map.MapFragment;

import java.util.Timer;
import java.util.TimerTask;

public class fragment extends Fragment {

    private model homeViewModel;
    private Button newBusButton;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        InfoClasses.Status.ActiveFragment = InfoClasses.Status.Rider;

        homeViewModel = ViewModelProviders.of(this).get(model.class);
        View root = inflater.inflate(R.layout.rider_bus_fragment, container, false);

        newBusButton = root.findViewById(R.id.newBusButton);
        newBusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Internet.GetZonedSchools RGC = new Internet.GetZonedSchools(getContext());
                RGC.execute(InfoClasses.myInfo.Address);

                newBusButton.setText("Loading...");

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        requireActivity().runOnUiThread(new Runnable() {
                            public void run() {

                                newBusButton.setText("Add a new bus");
                            }
                        });
                    }
                }, 3000);
            }
        });

        return root;
    }
}