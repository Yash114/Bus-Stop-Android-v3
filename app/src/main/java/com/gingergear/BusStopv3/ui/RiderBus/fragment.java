package com.gingergear.BusStopv3.ui.RiderBus;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
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

import java.sql.Array;
import java.util.Timer;
import java.util.TimerTask;

public class fragment extends Fragment {

    private model homeViewModel;
    private Button newBusButton;
    private Button FirstSchool;
    private Button SecondSchool;
    private Button ThirdSchool;

    private LinearLayout route1;
    private LinearLayout route2;
    private LinearLayout route3;
    private LinearLayout[] layouts = new LinearLayout[3];

    private TextView route1_school;
    private TextView route2_school;
    private TextView route3_school;
    private TextView[] textViews = new TextView[3];

    private CheckBox route1_check;
    private CheckBox route2_check;
    private CheckBox route3_check;
    private CheckBox[] checkBoxes = new CheckBox[3];

    private Button[] SchoolButtons = new Button[3];

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        InfoClasses.Status.ActiveFragment = InfoClasses.Status.Rider;

        homeViewModel = ViewModelProviders.of(this).get(model.class);
        View root = inflater.inflate(R.layout.rider_bus_fragment, container, false);

        FirstSchool = root.findViewById(R.id.busone);
        SecondSchool = root.findViewById(R.id.bustwo);
        ThirdSchool = root.findViewById(R.id.busthree);

        SchoolButtons[0] = FirstSchool;
        SchoolButtons[1] = SecondSchool;
        SchoolButtons[2] = ThirdSchool;

        route1 = root.findViewById(R.id.JoinedRoute1);
        route2 = root.findViewById(R.id.JoinedRoute2);
        route3 = root.findViewById(R.id.JoinedRoute3);
        layouts = new LinearLayout[]{route1, route2, route3};

        route1_school = root.findViewById(R.id.JoinedRoute1_text);
        route2_school = root.findViewById(R.id.JoinedRoute2_text);
        route3_school = root.findViewById(R.id.JoinedRoute3_text);
        textViews = new TextView[]{route1_school, route2_school, route3_school};

        route1_check = root.findViewById(R.id.JoinedRoute1_check);
        route1_check = root.findViewById(R.id.JoinedRoute2_check);
        route1_check = root.findViewById(R.id.JoinedRoute3_check);
        checkBoxes = new CheckBox[]{route1_check, route2_check, route3_check};

        if(InfoClasses.myInfo.BusRoutes.size() != 0){

            int index = 0;
            for(String routes : InfoClasses.myInfo.BusRoutes){

                layouts[0].setVisibility(View.VISIBLE);
                textViews[0].setText(InfoClasses.myInfo.ZonedSchools.get(index));
            }

            Internet.joinRoutes_AsRider(routes);
        }

        for(Button buttons : SchoolButtons){

            buttons.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int index = 0;
                    for(Button button : SchoolButtons){

                        if(button == v){

                            final Button clickedButton = button;

                            button.setText("Adding Your Bus...");
                            Internet.GetBusData GBD = new Internet.GetBusData(getContext());
                            Log.e("tag", InfoClasses.myInfo.SchoolURL.get(index));
                            GBD.execute(InfoClasses.myInfo.SchoolURL.get(index));
                            final String clickedSchool = InfoClasses.myInfo.ZonedSchools.get(index);

                            new Timer().schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    requireActivity().runOnUiThread(new Runnable() {
                                        public void run() {

                                            clickedButton.setText(clickedSchool);

                                            if(InfoClasses.myInfo.BusRoutes.size() != 0){

                                                Toast.makeText(getContext(), "Successfully added this bus route", Toast.LENGTH_SHORT).show();
                                                SaveData.SaveBusRoute(InfoClasses.myInfo.BusRoutes);

                                                Internet.joinRoute_AsRider(InfoClasses.myInfo.BusRoutes.get(0));
                                            } else {

                                                Toast.makeText(getContext(), "Server Error", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }, 5000);
                        }

                        index += 1;
                    }
                }
            });
        }

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

                                if(InfoClasses.myInfo.ZonedSchools != null){

                                    int index = 0;
                                    for(Button button : SchoolButtons){

                                        button.setVisibility(View.VISIBLE);
                                        button.setText(InfoClasses.myInfo.ZonedSchools.get(index));

                                        index += 1;
                                    }
                                }
                            }
                        });
                    }
                }, 5000);
            }
        });

        return root;
    }
}