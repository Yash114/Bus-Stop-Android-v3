package com.gingergear.BusStopv3.ui.BusControl;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.gingergear.BusStopv3.Background_Update;
import com.gingergear.BusStopv3.InfoClasses;
import com.gingergear.BusStopv3.Internet;
import com.gingergear.BusStopv3.MainActivity;
import com.gingergear.BusStopv3.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class BusControlFragment extends androidx.fragment.app.Fragment {

    private BusControlModel homeViewModel;

    //objects
    private Button ActionButton;
    private TextView BusNumberDisplay;
    private TextView BusDriverNameDisplay;

    private static Button route_one;
    private static Button route_two;
    private static Button route_three;
    private static Button route_four;
    private static ArrayList<Button> buttons = new ArrayList<>();
    private static ArrayList<String> routes;

    public static View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(BusControlModel.class);
        root = inflater.inflate(R.layout.driver_bus_fragment, container, false);

        //Internet Stuff
        Internet.CreateWebSocketConnection();
        Internet.joinRoute_AsBus(InfoClasses.busInfo.BusNumber);
        Internet.fetchYourRoutes(InfoClasses.busInfo.BusNumber);

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

        route_one = root.findViewById(R.id.route1);
        route_two = root.findViewById(R.id.route2);
        route_three = root.findViewById(R.id.route3);
        route_four = root.findViewById(R.id.route4);

        buttons.add(route_one);
        buttons.add(route_two);
        buttons.add(route_three);
        buttons.add(route_four);

        StartRefresh();

        //Initialize Buttons
        ActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routes = InfoClasses.busInfo.AssignedBusRoutes;
                if (!InfoClasses.Bluetooth.isConnected) {
                    InfoClasses.busInfo.connectToBus();
                    ActionButton.setText("Loading...");

                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (InfoClasses.Bluetooth.isConnected) {
                                ActionButton.setText("Disconnect");

                            } else {
                                ActionButton.setText("Connect To your Bus");

                            }
                        }
                    }, 7000);
                } else {
                    InfoClasses.Bluetooth.disconnectBluetoothDevice();
                    ActionButton.setText("Connect To your Bus");
                }
            }
        });

        return root;
    }

    private void StartRefresh() {

        Timer timer = new Timer();
        TimerTask myTask = new TimerTask() {
            @Override
            public void run() {

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {

                        if (InfoClasses.busInfo.AssignedBusRoutes != null) {

                            boolean pass = false;

                            if (routes != InfoClasses.busInfo.AssignedBusRoutes) {
                                routes = InfoClasses.busInfo.AssignedBusRoutes;
                                pass = true;

                            } else {

                                for (int x = 0; x < routes.size(); x++) {

                                    if (!routes.get(x).equals(InfoClasses.busInfo.AssignedBusRoutes.get(x))) {
                                        pass = true;

                                    }
                                }
                            }

                            if (pass) {

                                if (routes.size() != 0) {

                                    for (int x = 0; x < routes.size(); x++) {

                                        BusControlFragment.buttons.get(x).setVisibility(View.VISIBLE);
                                        BusControlFragment.buttons.get(x).setText(routes.get(x));
                                        BusControlFragment.buttons.get(x).setOnClickListener(new View.OnClickListener() {

                                            @Override
                                            public void onClick(View v) {

                                                int index = 0;
                                                for (Button button : BusControlFragment.buttons) {

                                                    if (button == v) {
                                                        Intent intent;
                                                        intent = new Intent(getContext(), Background_Update.class);

                                                        InfoClasses.busInfo.CurrentRoute = routes.get(index);
                                                        Internet.joinRoute_AsBus(InfoClasses.busInfo.BusNumber, InfoClasses.busInfo.CurrentRoute);
                                                        getContext().startService(intent);
                                                    }

                                                    index += 1;
                                                }
                                            }
                                        });
                                    }
                                } else {

                                    Log.e("websocket", "took too long to respond");

                                }
                            }
                        }
                    }
                });
            }
        };
        timer.schedule(myTask, 100, 250);

    }
}