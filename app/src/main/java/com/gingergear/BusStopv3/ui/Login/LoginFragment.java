package com.gingergear.BusStopv3.ui.Login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.gingergear.BusStopv3.R;


public class LoginFragment extends Fragment {

    View root;
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.login_fragment, container, false);
        return root;
    }
}
