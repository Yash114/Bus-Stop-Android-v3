package com.gingergear.BusStopv3.ui.Login;

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
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.gingergear.BusStopv3.InfoClasses;
import com.gingergear.BusStopv3.Internet;
import com.gingergear.BusStopv3.MainActivity;
import com.gingergear.BusStopv3.R;
import com.gingergear.BusStopv3.ui.Map.MapFragment;

import java.util.Timer;
import java.util.TimerTask;


public class LoginFragment extends Fragment {

    View root;

    private EditText username;
    private EditText password;
    private Button submitButton;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        InfoClasses.Status.ActiveFragment = InfoClasses.Status.LOGIN;
        root = inflater.inflate(R.layout.login_fragment, container, false);

        InfoClasses.Mode.ChangeToRiderMode(getContext());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        MainActivity.mMap.setMyLocationEnabled(true);

        if(InfoClasses.MyInfo.CurrentLocation != null) {
            Internet.getCounty RGC = new Internet.getCounty();
            RGC.execute(InfoClasses.MyInfo.CurrentLocation.latitude, InfoClasses.MyInfo.CurrentLocation.longitude);
        }

        username = root.findViewById(R.id.username);
        password = root.findViewById(R.id.password);
        submitButton = root.findViewById(R.id.submitPASS);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String User = username.getText().toString();
                String Pass = password.getText().toString();

                InfoClasses.Login.Username = User;
                InfoClasses.Login.Password = Pass;

                Internet.login();
                InfoClasses.Login.reset();

                submitButton.setText("Loading...");
                submitButton.setClickable(false);

                username.setText("");
                password.setText("");

                Log.i("Bluetooth", InfoClasses.county);

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        requireActivity().runOnUiThread(new Runnable() {
                            public void run() {

                                submitButton.setText("SUBMIT");
                                submitButton.setClickable(true);

                                int mode = InfoClasses.Login.gatheredMode;
                                if(mode != 0){

                                    InfoClasses.Markers.getMarkers(getContext());
                                    if(mode == 1){

                                        InfoClasses.Mode.ChangeToDriverMode(getContext());
                                        Toast.makeText(getContext(), "You just logged into DRIVER mode", Toast.LENGTH_SHORT);

                                    } else {

                                        InfoClasses.Mode.ChangeToAdminMode(getContext());
                                        Toast.makeText(getContext(), "You just logged into ADMIN mode", Toast.LENGTH_SHORT);

                                    }
                                    MainActivity.updates(true);
                                    MainActivity.ChangeToMapView();
                                } else {

//                                    InfoClasses.Mode.ChangeToRiderMode(getContext());
                                    Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                                    v.vibrate(300);
                                    Toast.makeText(getContext(), InfoClasses.Login.ERROR, Toast.LENGTH_LONG).show();
                                }

                            }
                        });
                    }
                }, 1000);
            }
        });


        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
