package com.gingertech.BusStopv3.ui.Comms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.gingertech.BusStopv3.R;

public class BusComms  extends androidx.fragment.app.Fragment{

    public View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.driver_bus_fragment, container, false);

        return root;
    }
}
