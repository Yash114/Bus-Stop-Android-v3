package com.gingertech.BusStopv3.ui.Comms;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gingertech.BusStopv3.InfoClasses;
import com.gingertech.BusStopv3.Internet;
import com.gingertech.BusStopv3.R;
import com.gingertech.BusStopv3.ui.BusControl.BusControlModel;

import org.w3c.dom.Text;

import java.util.HashMap;

public class AdminComms extends androidx.fragment.app.Fragment {

    private RecyclerView ERROR_BUSES;
    public View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        InfoClasses.Status.ActiveFragment = InfoClasses.Status.Text;
        root = inflater.inflate(R.layout.admin_comms, container, false);

        ERROR_BUSES = root.findViewById(R.id.problematicBuses);

        int length = InfoClasses.AdminInfo.busErrors.size();
        if(length > 0) {
            String[][] dataSource = new String[length][3];
            int x = 0;
            for (String[] data : InfoClasses.AdminInfo.busErrors.values()) {

                dataSource[x] = data;
                x += 1;
            }

            ERROR_BUSES.setAdapter(new MyAdapter(getContext(), dataSource));
            ERROR_BUSES.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        return root;
    }
}

class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {


    Context context;
    String[][] busData;


    public MyAdapter(Context ct, String[][] buses){

        context = ct;
        busData = buses;
    }

    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.problem_buses_row, parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {

        holder.busNumber.setText("#" + busData[position][0]);
        holder.busAddress.setText(busData[position][1]);
        holder.showTime.setText(busData[position][2]);


    }

    @Override
    public int getItemCount() {
        return busData == null ? 0 : busData.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView busNumber;
        TextView busAddress;
        TextView showTime;

        ImageButton completed;

        public MyViewHolder(@NonNull final View itemView) {
            super(itemView);

            showTime = itemView.findViewById(R.id.timeShow);
            busNumber = itemView.findViewById(R.id.BusNumber);
            busAddress = itemView.findViewById(R.id.BusAddy);
            completed = itemView.findViewById(R.id.doneButton);

            completed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String BusNumber = busNumber.getText().toString();

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            InfoClasses.AdminInfo.busErrors.remove(BusNumber);
                            Internet.removeBusError(BusNumber);
                            itemView.refreshDrawableState();
                        }
                    });
                    builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

                    builder.setTitle("Have you contacted bus " + BusNumber + " through radio?");
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            });
        }
    }

}

