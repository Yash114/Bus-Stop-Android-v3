package com.gingergear.BusStopv3;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@SuppressWarnings("ConstantConditions")
public class SaveData {

    private static Context mContext;
    private static String File_Name;
    static String filename = "39r8ybciu1oni0ev2g979ebcu1w";

    public SaveData(Context context) {

        mContext = context;
    }

    public static void SaveMyHomePos(LatLng latLng) {

        SharedPreferences.Editor editor = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        editor.putString("HomePos-lat", Double.toString(latLng.latitude));
        editor.putString("HomePos-lng", Double.toString(latLng.longitude));

        editor.apply();
        Log.i("Save", "Successfully saved: your home coordinates");

    }

    public static LatLng ReadMySavedPos() {

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
        Log.i("Save", "Successfully saved: " + addy + " as you address");
    }

    public static String ReadMySavedAddy() {

        return mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getString("Address", null);
    }

    public static void SaveBusRoutes(){

        Set<String> stringSet = new HashSet<>();

        stringSet.add(InfoClasses.myInfo.BusRoutes.get(0));
        stringSet.add(InfoClasses.myInfo.BusRoutes.get(1));
        stringSet.add(InfoClasses.myInfo.BusRoutes.get(2));

        Set<String> stringSet2 = new HashSet<>();

        stringSet2.add(InfoClasses.myInfo.ZonedSchools.get(0));
        stringSet2.add(InfoClasses.myInfo.ZonedSchools.get(1));
        stringSet2.add(InfoClasses.myInfo.ZonedSchools.get(2));

        SharedPreferences.Editor editor = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        editor.putStringSet("SavedUserRoutes", stringSet);
        editor.putStringSet("SavedUserSchools", stringSet2);

        editor.apply();
        Log.i("Save", "Successfully saved: Your default routes and schools");

    }

    public static ArrayList<String>[] ReadMySavedRoutes(){

        //remember this is returns a SET!! Use an iterator to retrieve data
        if(mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getStringSet("SavedUserRoutes", null) != null) {

            ArrayList<String> out = new ArrayList<>(mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getStringSet("SavedUserRoutes", null));
            ArrayList<String> out1 = new ArrayList<>(mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getStringSet("SavedUserSchools", null));

            return new ArrayList[]{out, out1};
        }

        return null;
    }

    public static void SaveAppMode(int mode){

        SharedPreferences.Editor editor = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        editor.putInt("mode", mode);

        editor.apply();
        Log.i("Save", "Successfully saved: " + mode + "as you default mode");

    }

    public static int ReadAppMode() {

        if(mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).contains("mode") ){
            return mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getInt("mode", -1);
        }

        return -1;
    }
}