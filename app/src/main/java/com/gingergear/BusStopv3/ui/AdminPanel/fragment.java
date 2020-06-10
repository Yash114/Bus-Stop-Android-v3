package com.gingergear.BusStopv3.ui.AdminPanel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gingergear.BusStopv3.InfoClasses;
import com.gingergear.BusStopv3.R;

public class fragment extends Fragment {

    private model homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        InfoClasses.Status.ActiveFragment = InfoClasses.Status.Admin;
        InfoClasses.Mode.ChangeToAdminMode(getContext());

        homeViewModel = ViewModelProviders.of(this).get(model.class);
        View root = inflater.inflate(R.layout.adminsettings_fragment, container, false);
        final TextView textView = root.findViewById(R.id.text_adminSettings);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}