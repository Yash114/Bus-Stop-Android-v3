package com.gingergear.BusStopv3.ui.AdminPanel;

import android.app.Activity;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.gingergear.BusStopv3.InfoClasses;
import com.gingergear.BusStopv3.R;

import java.util.ArrayList;

public class fragment extends Fragment implements AdapterView.OnItemSelectedListener, TextWatcher {

    private model homeViewModel;
    private Spinner searchModeSpinner;
    private EditText searchBar;
    private String searchMode;
    private Button searchButton;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        InfoClasses.Status.ActiveFragment = InfoClasses.Status.Admin;
        InfoClasses.Mode.ChangeToAdminMode(getContext());

        homeViewModel = ViewModelProviders.of(this).get(model.class);
        View root = inflater.inflate(R.layout.adminsettings_fragment, container, false);

        searchBar = root.findViewById(R.id.search);

        searchButton = root.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hideKeyboard(getActivity());
                searchBar.setText("");
                searchBar.clearFocus();
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
                    hideKeyboard(getActivity());
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

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}