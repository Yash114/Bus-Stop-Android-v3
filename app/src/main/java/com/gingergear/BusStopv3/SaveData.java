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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@SuppressWarnings("ConstantConditions")
public class SaveData {

    private static Context mContext;
    private static String File_Name;
    static String filename = "39r8ybciu1oni0ev2g979ebcu1w";

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

    public static void SaveBusRoute(List<String> routes){

        Set<String> stringSet = new Set<String>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(@Nullable Object o) {
                return false;
            }

            @NonNull
            @Override
            public Iterator<String> iterator() {
                return null;
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @NonNull
            @Override
            public <T> T[] toArray(@NonNull T[] a) {
                return null;
            }

            @Override
            public boolean add(String s) {
                return false;
            }

            @Override
            public boolean remove(@Nullable Object o) {
                return false;
            }

            @Override
            public boolean containsAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public boolean addAll(@NonNull Collection<? extends String> c) {
                return false;
            }

            @Override
            public boolean retainAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public boolean removeAll(@NonNull Collection<?> c) {
                return false;
            }

            @Override
            public void clear() {

            }
        };

        stringSet.add(routes.get(0) == null ? "~" : routes.get(0));
        stringSet.add(routes.get(0) == null ? "~" : routes.get(0));
        stringSet.add(routes.get(0) == null ? "~" : routes.get(0));

        SharedPreferences.Editor editor = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        editor.putStringSet("SavedUserRoutes", stringSet);

        editor.apply();
    }

    public static ArrayList<String> ReadMySavedRoutes(){

        //remember this is returns a SET!! Use an iterator to retrieve data
        if(mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getStringSet("SavedUserRoutes", null) != null) {
            String[] stuff = (String[]) mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getStringSet("SavedUserRoutes", null).toArray();
            ArrayList<String> out = new ArrayList<>();

            Collections.addAll(out, stuff);

            return out;
        }

        return null;
    }
}