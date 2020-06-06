package com.gingergear.BusStopv3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.gingergear.BusStopv3.ui.BusControl.BusControlFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class Internet {

    static public String code = "0";
    static public LatLng Bus_Location;
    static public LatLng My_Location;
    static public Boolean SocketConnected = false;

    //Context storing variable
    Context mContext;

    //Full Address, Full Address w/ "+", streetNumber, streetname
    static String[] mAddress_info = new String[4];

    //Necessary data for GeoCoding  
    private static String locationIQ_URL = "https://us1.locationiq.com/v1/reverse.php";
    private static String getLocationIQ_KEY = "5b89c4a4581b3a";

    //Necessary data for reading the Bus's Location
    private static String websitePath = "https://www.gakapparel.com/BUSstuff/gg.php";

    //Necessary data for reading from the WEBQUERY website
    private static String WebQuery_URL = "https://api.busstopapi.com/webquery-request?";
    private String WebQuery_SessionID;

    //Websocket Stuff
    private static String WebQuery_path = "wss://60xx5h0jma.execute-api.us-east-2.amazonaws.com/dev";
    private static WebSocket ws;

    //Stores the zoned schools' data
    static ArrayList<String> School_Name = new ArrayList<>();
    static ArrayList<String> School_URL = new ArrayList<>();
    static ArrayList<String> Bus_Number = new ArrayList<>();

    //Success Array {getLocation, sendLocation, reverse, getZonedSchools, getBusData}
    public static Boolean[] Success = {false, false, false, false, false};
    public static int getLocation_Success = 0;
    public static int sendLocation_Success = 1;
    public static int reverseGeoCode_Success = 2;
    public static int getZonedSchool_Success = 3;
    public static int getBusData_Success = 4;

    //Dependant Variables
    private static Boolean Connected = true;
    public static String My_Pref_Name = "byebye";

    public static void sendLocations(LatLng latLng, String busRouteID, String busNumber){

        if(SocketConnected) {
            String county = "Henry";

            String dataOut = "{\"action\" : \"updateData\" , \"data\" : {\"county\" : \"" +
                    county + "\" , \"routeID\" : \"" +
                    busRouteID + "\" , \"lat\" : \"" +
                    latLng.latitude + "\" , \"lng\" : \"" +
                    latLng.longitude + "\" , \"busNumber\" : \"" +
                    busNumber + "\"}}";

            Log.e("websocket", dataOut);
            ws.send(dataOut);
        } else {

            Log.e("websocket", "ERROR");
            CreateWebSocketConnection();


        }

    }

    public static void joinRoute_AsBus(String busNumber, String busRouteID){

        if(SocketConnected) {

            String county = "Henry";

            String dataOut = "{\"action\" : \"joinRoute\" , \"data\" : {\"county\" : \"" +
                    county + "\" , \"routeID\" : \"" +
                    busRouteID + "\", \"busNumber\" : \"" +
                    busNumber + "\", \"exalt\" : \"1\"}}";

            ws.send(dataOut);
            Log.e("websocket", "Connected to route " + busRouteID);

        } else {

            Log.e("websocket", "ERROR");
            CreateWebSocketConnection();


        }

    }

    public static void joinRoute_AsBus(String busNumber){

        if(SocketConnected) {

            String county = "Henry";

            String dataOut = "{\"action\" : \"joinRoute\" , \"data\" : {\"county\" : \"" +
                    county + "\" , \"busNumber\" : \"" +
                    busNumber + "\", \"exalt\" : \"1\"}}";

            ws.send(dataOut);
            Log.e("websocket", "Connected yuor bus");

        } else {

            Log.e("websocket", "ERROR");
            CreateWebSocketConnection();


        }

    }

    public static void joinRoute_AsRider(String busRouteID){

        if(SocketConnected) {

            Log.e("websocket", busRouteID);
            String county = "Henry";

            String dataOut = "{\"action\" : \"joinRoute\" , \"data\" : {\"county\" : \"" +
                    county + "\" , \"routeID\" : \"" +
                    busRouteID + "\"}}";

            ws.send(dataOut);
        } else {

            Log.e("websocket", "ERROR");
            CreateWebSocketConnection();

        }

    }

    public static void fetchYourRoutes(String busNumber){

        if(SocketConnected) {

            String county = "Henry";

            String dataOut = "{\"action\" : \"joinBus\" , \"data\" : {\"county\" : \"" +
                    county + "\" , \"busNumber\" : \"" +
                    busNumber + "\"}}";

            ws.send(dataOut);
        } else {

            Log.e("websocket", "ERROR");
            CreateWebSocketConnection();

        }

    }

    public static void disconnectYourBus(String busNumber, LatLng location) {

        if (SocketConnected) {

            String county = "Henry";

            String dataOut = "{\"action\" : \"disconnectBus\" , \"data\" : {\"county\" : \"" +
                    county + "\" , \"busNumber\" : \"" +
                    busNumber + "\" , \"lat\" : \"" +
                    location.latitude + "\" , \"lng\" : \"" +
                    location.longitude + "\"}}";

            ws.send(dataOut);
        } else {

            Log.e("websocket", "ERROR");
            CreateWebSocketConnection();

        }
    }

    public static final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.i("websocket", "opened");

        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {

            Log.i("websocket", text);
            try {
                JSONObject jObject = new JSONObject(text);

                if(text.contains("LatLng_data")) {
                    JSONObject object = jObject.getJSONObject("LatLng_data");

                    LatLng output = new LatLng(object.getDouble("lat"), object.getDouble("lng"));
                    Log.i("websocket", "retreved: " + output.toString());
                    InfoClasses.busInfo.BusLocation = output;
                    InfoClasses.busInfo.BusNumber = object.getString("busNumber");
                    InfoClasses.Status.Status = InfoClasses.Status.NORMAL;

                }

                if(text.contains("busData")) {
                    JSONObject object = jObject.getJSONObject("busData");

                    ArrayList<String> myRoutes = new ArrayList<String>();

                    if (!object.getString("route_one").equals("None")) {
                        myRoutes.add(object.getString("route_one"));
                    }

                    if (!object.getString("route_two").equals("None")) {
                        myRoutes.add(object.getString("route_two"));
                    }

                    if (!object.getString("route_three").equals("None")) {
                        myRoutes.add(object.getString("route_three"));
                    }

                    if (!object.getString("additional_route").equals("None")) {
                        myRoutes.add(object.getString("additional_route"));
                    }

                    InfoClasses.busInfo.AssignedBusRoutes = myRoutes;
                    Log.i("websocket", "jo");


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {


        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            Log.i("websocket", "Closing : " + code + " / " + reason);
            SocketConnected = false;

        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.i("websocket", "Error : " + t.getMessage());
            SocketConnected = false;

        }
    }

    public static void CreateWebSocketConnection(){

        if(!SocketConnected) {
            Request request = new Request.Builder().url(WebQuery_path).build();
            EchoWebSocketListener listener = new EchoWebSocketListener();

            OkHttpClient client = new OkHttpClient();

            ws = client.newWebSocket(request, listener);
            client.dispatcher().executorService().shutdown();
            SocketConnected = true;
        }
    }

    public static void CloseWebSocket(){

        ws.cancel();
    }
}
