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

    public static void SaveBusRoute(List<String> routes, List<String> schools){

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

        stringSet.add(routes.get(0));
        stringSet.add(routes.get(1));
        stringSet.add(routes.get(2));

        Set<String> stringSet2 = new Set<String>() {
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

        stringSet2.add(schools.get(0));
        stringSet2.add(schools.get(1));
        stringSet2.add(schools.get(2));

        SharedPreferences.Editor editor = mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        editor.putStringSet("SavedUserRoutes", stringSet);
        editor.putStringSet("SavedUserSchools", stringSet2);

        editor.apply();
        Log.i("Save", "Successfully saved: Your default routes and schools");

    }

    public static ArrayList<String>[] ReadMySavedRoutes(){

        //remember this is returns a SET!! Use an iterator to retrieve data
        if(mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getStringSet("SavedUserRoutes", null) != null) {
            String[] stuff = (String[]) mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getStringSet("SavedUserRoutes", null).toArray();
            ArrayList<String> out = new ArrayList<>();

            String[] stuff1 = (String[]) mContext.getSharedPreferences(filename, Context.MODE_PRIVATE).getStringSet("SavedUserSchools", null).toArray();
            ArrayList<String> out1 = new ArrayList<>();

            Collections.addAll(out, stuff);
            Collections.addAll(out1, stuff1);

            ArrayList<String>[] bye = new ArrayList[]{out, out1};

            return bye;
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