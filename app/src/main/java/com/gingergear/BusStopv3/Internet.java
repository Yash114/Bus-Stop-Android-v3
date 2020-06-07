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
    private static String WebQuery_SessionID;

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

    public Internet(){ }

    static class ReverseGeoCode extends AsyncTask<Double, Void, JSONObject> {

        public String houseNumber = "";
        public String County = "";
        public String Street = "";
        public String Addyy = "";

        @SuppressLint("WrongThread")
        @Override
        protected JSONObject doInBackground(Double... coordinates) {

            if (Connected) {

                String str = locationIQ_URL + "?key=" + getLocationIQ_KEY + "&lat=" + coordinates[0] + "&lon=" + coordinates[1] + "&format=json";

                HttpURLConnection urlConn;
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(str);
                    urlConn = (HttpURLConnection) url.openConnection();

                    bufferedReader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));

                    StringBuilder stringBuffer = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuffer.append(line);
                    }

                    Log.i("intern", "Reverse Geocode: Successful");

                    urlConn.disconnect();

                    return new JSONObject(stringBuffer.toString());

                } catch (Exception ex) {

                    Log.e("intern", "reverseGeocode:" + ex.getMessage());

                    return null;
                } finally {

                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        return null;

                    }
                }
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject response) {
            if (response != null) {
                try {

                    houseNumber = response.getJSONObject("address").getString("house_number");
                    Street = response.getJSONObject("address").getString("road");
                    County = response.getJSONObject("address").getString("county");

                    char[] charArray = Street.toCharArray();
                    String[] addy1 = {"", "", "", "", ""};

                    Boolean Switch = false;
                    int u = 0;

                    if (houseNumber.contains(",")) {

                        houseNumber = houseNumber.substring(0, houseNumber.indexOf(","));
                    }

                    for (int i = 0; i < charArray.length; i++) {

                        if (charArray[i] != ' ') {

                            addy1[u] = addy1[u] + charArray[i];
                        } else {

                            u += 1;
                        }
                    }

                    for (int i = 0; i <= u; i++) {

                        Addyy = Addyy + "+" + addy1[i];
                    }

                    Addyy = houseNumber + Addyy;

                    mAddress_info[0] = houseNumber + " " + Street;
                    mAddress_info[1] = Addyy;
                    mAddress_info[2] = houseNumber;
                    mAddress_info[3] = Street;

                    InfoClasses.myInfo.Address = mAddress_info[0];
                    SaveData.SaveMyHomeAddy(mAddress_info[0]);

                    Log.i("intern", "Reverse Geocode JSON: Successful");
                    Log.i("intern", "Reverse Geocode JSON: I gathered your address is: " + mAddress_info[0]);

                    Success[reverseGeoCode_Success] = true;

                } catch (JSONException ex) {
                    Log.e("intern", "Reverse Geocode JSON: ERROR");
                    Log.e("intern", ex.getMessage());

                }
            }
        }
    }

    public static class GetZonedSchools extends AsyncTask<String, Void, JSONObject> {

        String[][] ZonedSchools = new String[5][2];

        int Retry = 0;
        String data;

        Boolean Marker = false;
        Boolean dumbBool = false;

        Context context;

        public GetZonedSchools(Context c){
            context = c;
        }

        @SuppressLint("WrongThread")
        @Override

        protected JSONObject doInBackground(String... strings) {

            if (Connected) {
                String theURL = WebQuery_URL + "Address=" + URLEncoder.encode(strings[0]) + "&County=Henry";

                Log.e("intern", ":"+theURL+";");
                data = strings[0];

                try {
                    URL url = new URL(theURL);

                    HttpURLConnection urlConn;
                    urlConn = (HttpURLConnection) url.openConnection();

                    InputStream byteStream = urlConn.getInputStream();
                    Reader targetReader = new InputStreamReader(byteStream);

                    // open the url stream, wrap it an a few "readers"
                    BufferedReader reader = new BufferedReader(targetReader);

                    String line;
                    StringBuffer sb = new StringBuffer();


                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

                    String data = sb.toString();
                    JSONObject jObject = new JSONObject(data);

                    for(int q = 0; q < 3; q++) {

                        String number = String.valueOf(q);

                        ZonedSchools[q][0] = jObject.getJSONObject(number).getString("SchoolURL");
                        ZonedSchools[q][1] = jObject.getJSONObject(number).getString("School");

                        Log.i("intern", ZonedSchools[q][1]);
                        Log.i("intern", ZonedSchools[q][0]);
                    }

                    WebQuery_SessionID = jObject.getString("cookie");

                    if (ZonedSchools[0][0] != null) {

                        Marker = true;
                    }

                    reader.close();
                    urlConn.disconnect();
                    // close our reader

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Log.e("intern", "GetZonedSchools :  malformed URL!");
                    Log.e("intern", e.getMessage());

                    Retry = 5;
                    dumbBool = true;

                } catch (FileNotFoundException e) {

                    Log.e("intern", "GetZonedSchools: The website is under maintenance");
                    Log.e("intern", e.getMessage());

                    Retry = 5;
                    dumbBool = true;


                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("intern", "GetZonedSchools: ERROR");
                    Log.e("intern", e.getMessage());

                    if (e.getMessage().equals("Host unreachable")) {

                        Log.e("intern", "GetZonedSchools: You have a proxy blocking this website or it is unavaliable");
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }


                return null;
            } else {

                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            if (Marker) {

                for (int i = 0; i < 3; i++) {

                    School_Name.add(ZonedSchools[i][1]);
                    School_URL.add(ZonedSchools[i][0]);

                    Log.e("intern", ZonedSchools[i][1]);
                }


                if (School_Name.get(0) != null) {

                    Log.i("intern", "GetZonedSchools: Successful");
//                    RiderFrag.SummonSchoolButtons(mContext, Internet.this);

                    Success[getZonedSchool_Success] = true;
                    InfoClasses.myInfo.ZonedSchools = School_Name;
                    InfoClasses.myInfo.SchoolURL = School_URL;

                } else {

                    Log.e("intern", "GetZonedSchools: ERROR");
                    Log.e("intern", "GetZonedSchools: You are not in a residential area");

                    Toast.makeText(context, "Please move to a residential area", Toast.LENGTH_LONG).show();
                }

            } else {

                Log.e("intern", "GetZonedSchools: ERROR");
                Log.e("intern", "GetZonedSchools: The servers are down");

                Toast.makeText(context, "Server Error", Toast.LENGTH_LONG).show();

//                RiderFrag.endSearch();
            }

            return;
        }
    }

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
