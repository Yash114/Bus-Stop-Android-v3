package com.gingergear.BusStopv3.ui.BusControl;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
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
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProviders;

import com.gingergear.BusStopv3.Background_Update;
import com.gingergear.BusStopv3.InfoClasses;
import com.gingergear.BusStopv3.Internet;
import com.gingergear.BusStopv3.MainActivity;
import com.gingergear.BusStopv3.R;
import com.gingergear.BusStopv3.SaveData;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class BusControlFragment extends androidx.fragment.app.Fragment {

    private BusControlModel homeViewModel;

    //objects
    private Button ActionButton;
    private Button EndRouteButton;
    private Button ResetRouteButton;
    private TextView BusNumberDisplay;
    private TextView BusDriverNameDisplay;

    private Button route_one;
    private Button route_two;
    private Button route_three;
    private Button route_four;
    public ArrayList<Button> buttons = new ArrayList<>();

    private LinearLayout route_onel;
    private LinearLayout route_twol;
    private LinearLayout route_threel;
    private LinearLayout route_fourl;
    public ArrayList<LinearLayout> layouts = new ArrayList<>();

    private CheckBox route_onec;
    private CheckBox route_twoc;
    private CheckBox route_threec;
    private CheckBox route_fourc;
    public ArrayList<CheckBox> checkBoxes = new ArrayList<>();

    private static Boolean active = true;

    public static View root;

    public static Intent intent;

    private static Timer timer;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        InfoClasses.Status.ActiveFragment = InfoClasses.Status.Driver;
        MainActivity.UpdatesAvailable = true;
        InfoClasses.Mode.ChangeToDriverMode(getContext());

        homeViewModel = ViewModelProviders.of(this).get(BusControlModel.class);
        root = inflater.inflate(R.layout.driver_bus_fragment, container, false);

        intent = new Intent(getContext(), Background_Update.class);
        active = true;

        //Initialize Bluetooth
        InfoClasses.Bluetooth.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!InfoClasses.Bluetooth.bluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }

        //Initialize XML objects
        ActionButton = root.findViewById(R.id.ActionButton);
        BusNumberDisplay = root.findViewById(R.id.BusNumberDisplay);
        BusDriverNameDisplay = root.findViewById(R.id.BusDriverName);

        ResetRouteButton = root.findViewById(R.id.ResetRoutes);
        EndRouteButton = root.findViewById(R.id.EndRoute);

        route_one = root.findViewById(R.id.route1);
        route_two = root.findViewById(R.id.route2);
        route_three = root.findViewById(R.id.route3);
        route_four = root.findViewById(R.id.route4);

        route_onel = root.findViewById(R.id.route1_layout);
        route_twol = root.findViewById(R.id.route2_layout);
        route_threel = root.findViewById(R.id.route3_layout);
        route_fourl = root.findViewById(R.id.route4_layout);

        route_onec = root.findViewById(R.id.route1_check);
        route_twoc = root.findViewById(R.id.route2_check);
        route_threec = root.findViewById(R.id.route3_check);
        route_fourc = root.findViewById(R.id.route4_check);

        buttons.add(route_one);
        buttons.add(route_two);
        buttons.add(route_three);
        buttons.add(route_four);

        layouts.add(route_onel);
        layouts.add(route_twol);
        layouts.add(route_threel);
        layouts.add(route_fourl);

        checkBoxes.add(route_onec);
        checkBoxes.add(route_twoc);
        checkBoxes.add(route_threec);
        checkBoxes.add(route_fourc);

        BusDriverNameDisplay.setText("Welcome " + InfoClasses.BusInfo.BusDriver);
        BusNumberDisplay.setText("#" + InfoClasses.BusInfo.BusNumber);

        if (InfoClasses.BusInfo.AssignedBusRoutes != null) {

            if (InfoClasses.Status.ActiveFragment == InfoClasses.Status.Driver) {

                if (InfoClasses.BusInfo.AssignedBusRoutes.size() != 0) {

                    boolean alreadyDidOne = false;

                    for (int x = 0; x < InfoClasses.BusInfo.AssignedBusRoutes.size(); x++) {

                        Log.e("tag", InfoClasses.BusInfo.AssignedBusRoutes.get(x));

                        layouts.get(x).setVisibility(View.VISIBLE);
                        buttons.get(x).setText(InfoClasses.BusInfo.AssignedBusRoutes.get(x));

                        boolean proceed = false;
                        if(InfoClasses.BusInfo.AssignedBusRoutes.size() != 0) {
                            for (String completedRoutes : InfoClasses.BusInfo.CompletedBusRoutes) {
                                if (completedRoutes.equals(InfoClasses.BusInfo.AssignedBusRoutes.get(x))) {
                                    alreadyDidOne = true;
                                    proceed = true;
                                    break;
                                }
                            }

                            if (proceed) {
                                checkBoxes.get(x).setChecked(true);
                            }
                        }
                    }

                    if(alreadyDidOne) {
                        ResetRouteButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        } else {

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    requireActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            if (InfoClasses.BusInfo.AssignedBusRoutes != null) {

                                if (InfoClasses.BusInfo.AssignedBusRoutes.size() != 0) {

                                    boolean alreadyDidOne = false;

                                    for (int x = 0; x < InfoClasses.BusInfo.AssignedBusRoutes.size(); x++) {

                                        Log.e("tag", InfoClasses.BusInfo.AssignedBusRoutes.get(x));

                                        layouts.get(x).setVisibility(View.VISIBLE);
                                        buttons.get(x).setText(InfoClasses.BusInfo.AssignedBusRoutes.get(x));

                                        boolean proceed = false;
                                        if (InfoClasses.BusInfo.AssignedBusRoutes.size() != 0) {
                                            for (String completedRoutes : InfoClasses.BusInfo.CompletedBusRoutes) {
                                                if (completedRoutes.equals(InfoClasses.BusInfo.AssignedBusRoutes.get(x))) {
                                                    alreadyDidOne = true;
                                                    proceed = true;
                                                    break;
                                                }
                                            }

                                            if (proceed) {
                                                checkBoxes.get(x).setChecked(true);
                                            }
                                        }
                                    }

                                    if (alreadyDidOne) {
                                        ResetRouteButton.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }
                    });
                }
            }, 1000);
        }

        if(InfoClasses.BusInfo.CurrentRoute != null){
            EndRouteButton.setVisibility(View.VISIBLE);
        }


        //Initialize Buttons
        ActionButton.setText(InfoClasses.Bluetooth.isConnected ? "Disconnect" : "Connect To your Bus");
        ActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!InfoClasses.Bluetooth.isConnected) {
                    InfoClasses.BusInfo.connectToBus();
                    ActionButton.setClickable(false);
                    EndRouteButton.setVisibility(View.INVISIBLE);
                    ActionButton.setText("Loading...");

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            requireActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    ActionButton.setClickable(true);

                                    if (InfoClasses.Bluetooth.isConnected) {
                                        ActionButton.setText("Disconnect");
                                        Internet.disconnectYourBus();

                                        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                                        v.vibrate(100);
                                    } else {
                                        ActionButton.setText("Connect To your Bus");

                                        Toast.makeText(getContext(), "Error Connecting to Bus " + InfoClasses.BusInfo.BusNumber, Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }, 7500);
                } else {

                    InfoClasses.BusInfo.disconnectFromBus(getContext());
                    EndRouteButton.callOnClick();

                    InfoClasses.Bluetooth.disconnectBluetoothDevice(getContext());
                    ActionButton.setText("Connect To your Bus");

                    Vibrator q = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    q.vibrate(100);
                }
            }
        });

        ResetRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int x = 0; x < 4; x++){
                    checkBoxes.get(x).setChecked(false);
                    buttons.get(x).setClickable(true);

                    InfoClasses.BusInfo.CompletedBusRoutes.clear();
                    SaveData.SaveCompletedBusRoutes();

                    EndRouteButton.setVisibility(View.INVISIBLE);

                    ResetRouteButton.setVisibility(View.GONE);
                }
            }
        });

        EndRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String completedRoute = InfoClasses.BusInfo.CurrentRoute;

                if(completedRoute != null) {
                    Log.e("tag", completedRoute);
                    InfoClasses.BusInfo.CompletedBusRoutes.add(completedRoute);
                    SaveData.SaveCompletedBusRoutes();

                    int index = InfoClasses.BusInfo.AssignedBusRoutes.lastIndexOf(InfoClasses.BusInfo.CurrentRoute);
                    InfoClasses.BusInfo.CurrentRoute = null;

                    checkBoxes.get(index).setChecked(true);
                    buttons.get(index).setClickable(false);
                    ResetRouteButton.setVisibility(View.VISIBLE);


                }
                EndRouteButton.setVisibility(View.INVISIBLE);


                InfoClasses.Status.inRoute = false;
            }
        });

        for(Button button : buttons){
            timer = new Timer();

            button.setOnClickListener(new View.OnClickListener() {

                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {

                    if(InfoClasses.Bluetooth.isConnected) {
                        int index = 0;
                        for (Button button : buttons) {
                            if (button == v) {

                                String currentRoute = InfoClasses.BusInfo.CurrentRoute;
                                Boolean precede = false;

                                if(currentRoute == null){
                                    precede = true;
                                } else if(!InfoClasses.BusInfo.CurrentRoute.equals(InfoClasses.BusInfo.AssignedBusRoutes.get(index))){
                                    precede = true;
                                }
                                if (precede) {

                                    if (!InfoClasses.BusInfo.CompletedBusRoutes.contains(InfoClasses.BusInfo.AssignedBusRoutes.get(index))) {
                                        EndRouteButton.setVisibility(View.VISIBLE);
                                        InfoClasses.BusInfo.CurrentRoute = InfoClasses.BusInfo.AssignedBusRoutes.get(index);
                                        Internet.joinRoute_AsBus(InfoClasses.BusInfo.BusNumber, InfoClasses.BusInfo.CurrentRoute);
                                        getContext().startService(BusControlFragment.intent);

                                        InfoClasses.BusInfo.marker.setTitle("My Current Bus Location");
                                        InfoClasses.BusInfo.marker.setSnippet("Currently performing route: " + InfoClasses.BusInfo.CurrentRoute);
                                        InfoClasses.BusInfo.marker.setPosition(new LatLng(0,0));
                                        InfoClasses.BusInfo.marker.setVisible(false);
                                        InfoClasses.BusInfo.marker.setTag("myBus");

                                        if (InfoClasses.BusInfo.AssignedBusRoutes.size() != 0) {

                                            boolean alreadyDidOne = false;

                                            for (int x = 0; x < InfoClasses.BusInfo.AssignedBusRoutes.size(); x++) {

                                                Log.e("tag", InfoClasses.BusInfo.AssignedBusRoutes.get(x));

                                                layouts.get(x).setVisibility(View.VISIBLE);
                                                buttons.get(x).setText(InfoClasses.BusInfo.AssignedBusRoutes.get(x));

                                                boolean proceed = false;
                                                if(InfoClasses.BusInfo.AssignedBusRoutes.size() != 0) {
                                                    for (String completedRoutes : InfoClasses.BusInfo.CompletedBusRoutes) {
                                                        if (completedRoutes.equals(InfoClasses.BusInfo.AssignedBusRoutes.get(x))) {
                                                            alreadyDidOne = true;
                                                            proceed = true;
                                                            break;
                                                        }
                                                    }

                                                    if (proceed) {
                                                        checkBoxes.get(x).setChecked(true);
                                                    }
                                                }
                                            }

                                            if(alreadyDidOne) {
                                                ResetRouteButton.setVisibility(View.VISIBLE);
                                            }
                                        }

                                        Toast.makeText(getContext(), "Started Route: " + InfoClasses.BusInfo.CurrentRoute, Toast.LENGTH_LONG).show();
                                        InfoClasses.Status.inRoute = true;
                                        timer.cancel();
                                        active = false;

                                    } else {

                                        Toast.makeText(getContext(), "You already completed this route!", Toast.LENGTH_SHORT).show();

                                    }
                                } else {

                                    Toast.makeText(getContext(), "You are currently in this route!", Toast.LENGTH_SHORT).show();

                                }
                            }


                            index += 1;
                        }
                    } else {
                        Toast.makeText(getContext(), "Connect to your bus first", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }

        return root;
    }



    @Override
    public void onDestroy() {
        active = false;
        super.onDestroy();
    }
}