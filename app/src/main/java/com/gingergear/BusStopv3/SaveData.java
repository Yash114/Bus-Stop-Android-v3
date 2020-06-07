package com.gingergear.BusStopv3;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class SaveData {

    private static Context mContext;
    private static String File_Name;
    static String filename = "39r8ybciu1oni0ev2g979ebcu1w";

    static void init() {
    }


    public SaveData(Context context, String Filename) {

        mContext = context;
    }

    public static void SaveMyHomePos(LatLng latLng) {

        SharedPreferences.Editor editor = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        editor.putString("HomePos-lat", Double.toString(latLng.latitude));
        editor.putString("HomePos-lng", Double.toString(latLng.longitude));

        editor.apply();
    }

    public static LatLng readMySavedPos() {

        String lat = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getString("HomePos-lat", null);
        String lng = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getString("HomePos-lng", null);

        if (lat != null && lng != null) {

            return new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        }

        return null;
    }

    public static void SaveMyHomeAddy(String addy) {

        SharedPreferences.Editor editor = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        editor.putString("Address", addy);

        editor.apply();
    }

    public static String readMySavedAddy() {

        return mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getString("Address", null);
    }
}