package com.gingergear.BusStopv3.ui.AdminPanel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.gingergear.BusStopv3.InfoClasses;
import com.gingergear.BusStopv3.Internet;
import com.gingergear.BusStopv3.MainActivity;
import com.gingergear.BusStopv3.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class AdminPanelFragment extends Fragment implements AdapterView.OnItemSelectedListener, TextWatcher {

    private model homeViewModel;
    private Spinner searchModeSpinner;
    private AutoCompleteTextView searchBar;
    private String searchMode;
    private Button searchButton;
    private LinearLayout BusSettingsLayout;

    private TextView BusInfo;

    private Button ViewOnMap;
    private Button TextButton;
    private Button EditRouteInfo;
    private Button EditBusLoginInfo;
    private Button Refresh;

    private LinearLayout AddBusLayout;
    private EditText BusNumberInput;
    private EditText BusNameInput;

    private AutoCompleteTextView BusRoute1Input;
    private AutoCompleteTextView BusRoute2Input;
    private AutoCompleteTextView BusRoute3Input;
    private ArrayList<AutoCompleteTextView> BusRouteEditors = new ArrayList<>();

    private CheckBox CheckBox1;
    private CheckBox CheckBox2;
    private CheckBox CheckBox3;
    private CheckBox CheckBox4;
    private CheckBox CheckBox5;
    private ArrayList<CheckBox> CheckBoxes = new ArrayList<>();

    private String InputNumber;
    private String InputName;
    private String InputRoute1;
    private String InputRoute2;
    private String InputRoute3;

    private Button AddSubmitButton;

    private ArrayAdapter<String> busRouteList;

    private View root;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        InfoClasses.Status.ActiveFragment = InfoClasses.Status.Admin;
        InfoClasses.Mode.ChangeToAdminMode(getContext());

        Internet.retrieveAllRoutes();
        Internet.retrieveAllLocations();

        root = inflater.inflate(R.layout.adminsettings_fragment, container, false);

        busRouteList = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, InfoClasses.AdminInfo.AvailableRoutes);

        BusSettingsLayout = root.findViewById(R.id.busAdminControl);
        AddBusLayout = root.findViewById(R.id.AddNewBus);

        NewBusObjects();

        searchBar = root.findViewById(R.id.search);
        searchButton = root.findViewById(R.id.searchButton);

        ArrayList<String> searchTerms = InfoClasses.AdminInfo.AvailableBusNumbers;

        for(String route : InfoClasses.AdminInfo.R2N.keySet())
            searchTerms.add(route);

        ArrayAdapter<String> busNumberList = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, searchTerms);

        searchBar.setAdapter(busNumberList);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard();
                searchBar.clearFocus();

                final String Input = searchBar.getText().toString();
                Log.i("tag", Input);

                if (InfoClasses.AdminInfo.CountyBuses.containsKey(Input)) {

                    Log.i("tag", "found that Bus");
                    Internet.fetchYourRoutes(Input);

                    OpenBusSettings(Input);
                } else if (InfoClasses.AdminInfo.R2N.get(Input) != null) {

                    Log.i("tag", "found that Bus");

                    String GatheredRouteNumber = InfoClasses.AdminInfo.R2N.get(Input);

                    Internet.fetchYourRoutes(GatheredRouteNumber);
                    OpenBusSettings(GatheredRouteNumber);
                } else {

                    BusSettingsLayout.setVisibility(View.GONE);
                }

            }
        });

        searchModeSpinner = root.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.search_modes, android.R.layout.simple_spinner_item);
        searchModeSpinner.setAdapter(adapter);
        searchModeSpinner.setOnItemSelectedListener(this);


        searchBar = root.findViewById(R.id.search);
        searchBar.addTextChangedListener(this);
        searchBar.setImeActionLabel("Custom text", KeyEvent.KEYCODE_ENTER);
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE)) {
                    ;
                    searchBar.clearFocus();
                    searchBar.setText("");
                }

                return false;
            }
        });

        return root;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        searchMode = parent.getItemAtPosition(position).toString();
        searchBar.setHint("Search for " + searchMode);

        if (searchMode.equals("Add New Bus")) {

            searchBar.setVisibility(View.GONE);
            searchButton.setVisibility(View.GONE);
            AddBusLayout.setVisibility(View.VISIBLE);

        } else {

            searchBar.setVisibility(View.VISIBLE);
            searchButton.setVisibility(View.VISIBLE);
            AddBusLayout.setVisibility(View.GONE);
            exitAddBus();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

        searchMode = "Bus Number";
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void NewBusObjects() {

        AddBusLayout.setVisibility(View.VISIBLE);
        BusSettingsLayout.setVisibility(View.GONE);

        BusNameInput = root.findViewById(R.id.nameInput);
        BusNumberInput = root.findViewById(R.id.numberInput);

        BusRoute1Input = root.findViewById(R.id.routesInput1);
        BusRoute2Input = root.findViewById(R.id.routesInput2);
        BusRoute3Input = root.findViewById(R.id.routesInput3);

        BusRouteEditors.add(BusRoute1Input);
        BusRouteEditors.add(BusRoute2Input);
        BusRouteEditors.add(BusRoute3Input);
//
        CheckBox1 = root.findViewById(R.id.checkbox1);
        CheckBox2 = root.findViewById(R.id.checkbox2);
        CheckBox3 = root.findViewById(R.id.checkbox3);
        CheckBox4 = root.findViewById(R.id.checkbox4);
        CheckBox5 = root.findViewById(R.id.checkbox5);

        CheckBoxes.add(CheckBox1);
        CheckBoxes.add(CheckBox2);
        CheckBoxes.add(CheckBox3);
        CheckBoxes.add(CheckBox4);
        CheckBoxes.add(CheckBox5);

        AddSubmitButton = root.findViewById(R.id.submitAdd);

        BusNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                InputNumber = BusNumberInput.getText().toString();

                if (InputNumber.length() < 2) {

                    CheckBoxes.get(0).setChecked(false);
                } else {
                    CheckBoxes.get(0).setChecked(true);
                }
            }
        });

        BusNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                InputName = BusNameInput.getText().toString();

                if (InputNumber.length() < 2) {

                    CheckBoxes.get(1).setChecked(false);
                } else {
                    CheckBoxes.get(1).setChecked(true);
                }
            }
        });

        for (final AutoCompleteTextView view : BusRouteEditors) {

            view.setAdapter(busRouteList);
            view.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                    if (view == BusRouteEditors.get(0)) {

                        InputRoute1 = BusRouteEditors.get(0).getText().toString();

                        if (InputRoute1.equals("")) {
                            CheckBoxes.get(2).setChecked(false);
                        } else {
                            CheckBoxes.get(2).setChecked(true);
                        }
                    }
                    if (view == BusRouteEditors.get(1)) {

                        InputRoute2 = BusRouteEditors.get(1).getText().toString();

                        if (InputRoute2.equals("")) {
                            CheckBoxes.get(3).setChecked(false);
                        } else {
                            CheckBoxes.get(3).setChecked(true);
                        }
                    }
                    if (view == BusRouteEditors.get(2)) {

                        InputRoute3 = BusRouteEditors.get(2).getText().toString();

                        if (InputRoute3.equals("")) {
                            CheckBoxes.get(4).setChecked(false);
                        } else {
                            CheckBoxes.get(4).setChecked(true);
                        }
                    }
                }
            });
        }

        AddSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean youCanPass = true;

                for (CheckBox c : CheckBoxes) {

                    if (!c.isChecked()) {

                        youCanPass = false;
                    }
                }

                if (youCanPass) {

                    ArrayList<String> routes = new ArrayList<>();
                    routes.add(InputRoute1);
                    routes.add(InputRoute2);
                    routes.add(InputRoute3);
                    Internet.addBus(InputNumber, InputName, routes);
                    routes.clear();

                    Toast.makeText(getContext(), "Successfully Added Bus: " + InputNumber, Toast.LENGTH_SHORT);

                    exitAddBus();
                } else {

                    Toast.makeText(getContext(), "Please add all data before Submitting", Toast.LENGTH_SHORT);
                    Vibrator q = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    q.vibrate(300);
                }
            }
        });
    }

    private void OpenBusSettings(final String BusNumber) {

        BusInfo = root.findViewById(R.id.BusInfoText);
        ViewOnMap = root.findViewById(R.id.FindBusMap);
        Refresh = root.findViewById(R.id.refresh);
        EditRouteInfo = root.findViewById(R.id.UpdateRoutes);

        AddBusLayout.setVisibility(View.GONE);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                requireActivity().runOnUiThread(new Runnable() {
                    public void run() {

                        if (InfoClasses.BusInfo.BusDriver != null) {
                            BusSettingsLayout.setVisibility(View.VISIBLE);
                            ArrayList<String> Routes = InfoClasses.BusInfo.AssignedBusRoutes;
                            final String Name = InfoClasses.BusInfo.BusDriver;
                            InfoClasses.Buses theBus = InfoClasses.AdminInfo.CountyBuses.get(BusNumber);

                            Boolean Active = theBus.Active;
                            String Active_Text = Active ? "Active (" + theBus.Route + ")" : "not Active";
                            BusInfo.setText("Bus Number: " + BusNumber + "\n" + "Driver Name: " + Name + "\n" + "Active: " + Active_Text);

                            BusSettingsLayout.setVisibility(View.VISIBLE);

                            ViewOnMap.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    MainActivity.focus = InfoClasses.AdminInfo.CountyBuses.get(BusNumber).BusLocation;
                                    MainActivity.ChangeToMapView();

                                }
                            });

                            EditRouteInfo.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    searchModeSpinner.setSelection(1, true);

                                    NewBusObjects();
                                    BusSettingsLayout.setVisibility(View.GONE);
                                    BusNameInput.setText(Name);
                                    BusNumberInput.setText(BusNumber);

                                    CheckBoxes.get(0).setChecked(true);
                                    CheckBoxes.get(1).setChecked(true);


                                    for (int x = 0; x < 3; x++) {
                                        BusRouteEditors.get(x).setText(InfoClasses.BusInfo.AssignedBusRoutes.get(x));
                                        CheckBoxes.get(x + 2).setChecked(true);

                                    }
                                }
                            });

                            Refresh.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    BusSettingsLayout.setVisibility(View.GONE);
                                    OpenBusSettings(BusNumber);
                                }
                            });
                        } else {

                            Toast.makeText(getContext(), "Connection Error", Toast.LENGTH_SHORT);
                        }
                    }
                });
            }
        }, 2000);
    }

    private void exitAddBus() {

        InputName = "";
        InputNumber = "";
        InputRoute1 = "";
        InputRoute2 = "";
        InputRoute3 = "";

        BusNameInput.setText("");
        BusNumberInput.setText("");

        for (CheckBox c : CheckBoxes) {

            c.setChecked(false);
        }

        for (AutoCompleteTextView a : BusRouteEditors) {

            a.setText("");
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
    }
}