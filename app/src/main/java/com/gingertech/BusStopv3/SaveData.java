package com.gingertech.BusStopv3;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("ConstantConditions")
public class SaveData {

    private static Context mContext;
    static String filename = "39r8ybciu1oni0ev2g979ebu1w";

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

    public static void SaveBusRoutes() {

        Set<String> stringSet = new HashSet<>();

        stringSet.add(InfoClasses.MyInfo.BusRoutes.get(0));
        stringSet.add(InfoClasses.MyInfo.BusRoutes.get(1));
        stringSet.add(InfoClasses.MyInfo.BusRoutes.get(2));

        Set<String> stringSet2 = new HashSet<>();

        stringSet2.add(InfoClasses.MyInfo.ZonedSchools.get(0));
        stringSet2.add(InfoClasses.MyInfo.ZonedSchools.get(1));
        stringSet2.add(InfoClasses.MyInfo.ZonedSchools.get(2));

        SharedPreferences.Editor editor = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        editor.putStringSet("SavedUserRoutes", stringSet);
        editor.putStringSet("SavedUserSchools", stringSet2);

        editor.apply();
        Log.i("Save", "Successfully saved: Your default routes and schools");

    }

    public static void SaveCompletedBusRoutes() {

        Set<String> stringSet = new HashSet<>();

        stringSet.addAll(InfoClasses.BusInfo.CompletedBusRoutes);

        SharedPreferences.Editor editor = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        editor.putStringSet("CompletedBusRoutes", stringSet);

        editor.apply();
        Log.i("Save", "Successfully saved: Your default routes and schools");

    }

    public static ArrayList<String> ReadBusCompletedRoutes() {

        if (mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getStringSet("CompletedBusRoutes", null) != null) {
            ArrayList<String> out = new ArrayList<>(mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getStringSet("CompletedBusRoutes", null));
            return out;
        }

        return null;
    }

    public static ArrayList[] ReadMySavedRoutes() {

        if (mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getStringSet("SavedUserRoutes", null) != null) {
            if (mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getStringSet("SavedUserSchools", null) != null) {

                ArrayList<String> out = new ArrayList<>(mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getStringSet("SavedUserRoutes", null));
                ArrayList<String> out1 = new ArrayList<>(mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getStringSet("SavedUserSchools", null));

                return new ArrayList[]{out, out1};
            }
        }

        return null;
    }

    public static void SaveAppMode(int mode) {

        SharedPreferences.Editor editor = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        editor.putInt("TheSavedMode", mode);

        editor.apply();
        Log.i("Save", "Successfully saved: " + mode + " as you default mode");

    }

    public static int ReadAppMode() {

        int value = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getInt("TheSavedMode", -1);
        Log.e("Save", String.valueOf(value));
        return value;
    }


    public static void SaveKEY(String x) {

        SharedPreferences.Editor editor = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        editor.putString("KEY", x);

        Log.i("tag", x + " :");


        editor.apply();
    }

    public static String ReadKEY() {

        String j = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getString("KEY", null);
        Log.i("tag", j + " :");
        return j;
    }
    public static void SaveCounty() {

        SharedPreferences.Editor editor = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        editor.putString("countyStuff", InfoClasses.county);

        editor.apply();
    }

    public static String ReadCounty() {

        String j = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getString("countyStuff", null);
        InfoClasses.county = j;
        return j;
    }

}