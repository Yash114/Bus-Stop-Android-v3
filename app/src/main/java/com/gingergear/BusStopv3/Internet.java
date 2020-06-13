package com.gingergear.BusStopv3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

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
import java.util.Hashtable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
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

    //Necessary data for reading from the WEBQUERY website
    private static String WebQuery_URL = "https://k14jsk25oh.execute-api.us-east-2.amazonaws.com/dev/webquery-request?";
    private static String WebQuery_SessionID;

    //Websocket Stuff
    private static String WebQuery_path = "wss://60xx5h0jma.execute-api.us-east-2.amazonaws.com/Novel";
    private static WebSocket ws;

    //Stores the zoned schools' data
    static ArrayList<String> School_Name = new ArrayList<>();
    static ArrayList<String> School_URL = new ArrayList<>();
    static ArrayList<String> Bus_Number = new ArrayList<>();

    //Success Array {getLocation, sendLocation, reverse, getZonedSchools, getBusData}
    public static int getLocation_Success = 0;
    public static int sendLocation_Success = 1;
    public static int reverseGeoCode_Success = 2;
    public static int getZonedSchool_Success = 3;
    public static int getBusData_Success = 4;

    //Dependant Variables
    private static Boolean Connected = true;
    public static String My_Pref_Name = "byebye";

    public Internet() {
    }

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

                    InfoClasses.MyInfo.Address = mAddress_info[0];
                    SaveData.SaveMyHomeAddy(mAddress_info[0]);

                    Log.i("intern", "Reverse Geocode JSON: Successful");
                    Log.i("intern", "Reverse Geocode JSON: I gathered your address is: " + mAddress_info[0]);

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
        Boolean GetAllBuses = false;

        Context context;

        public GetZonedSchools(Context c) {
            context = c;
        }

        @SuppressLint("WrongThread")
        @Override

        protected JSONObject doInBackground(String... strings) {

            if (Connected) {
                String theURL = WebQuery_URL + "Address=" + URLEncoder.encode(strings[0]) + "&County=Henry";

                Log.e("intern", ":" + theURL + ";");
                data = strings[0];

                try {

                    GetAllBuses = strings[1].equals("true");
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

                    for (int q = 0; q < 3; q++) {

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
                    Internet.GetBusData GBD = new Internet.GetBusData(context);
                    GBD.execute(ZonedSchools[i][0], ZonedSchools[i][1], Integer.toString(i));

                    Log.e("intern", ZonedSchools[i][1]);
                }


                if (School_Name.get(0) != null) {

                    Log.i("intern", "GetZonedSchools: Successful");
//                    RiderFrag.SummonSchoolButtons(mContext, Internet.this);

                    InfoClasses.MyInfo.ZonedSchools = School_Name;
                    InfoClasses.MyInfo.SchoolURL = School_URL;

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

    public static class GetBusData extends AsyncTask<String, Void, JSONObject> {

        Hashtable<String, String> TableReadings = new Hashtable<>();
        String SelectedSchool;
        boolean GetBusDataSuccess = false;

        int orderOfExecution = 0;
        Context mcontext;

        public GetBusData(Context context) {

            mcontext = context;
        }


        @SuppressLint("WrongThread")
        @Override
        protected JSONObject doInBackground(String... data) {

            if (Connected) {

                orderOfExecution = Integer.parseInt(data[2]);

                String urlStrings = WebQuery_URL + "SchoolURL=" + URLEncoder.encode(data[0]) + "&Cookie=" + WebQuery_SessionID + "&County=Henry";
                SelectedSchool = data[1];
                String line;

                Log.e("intern", urlStrings);

                try {

                    URL url = new URL(urlStrings);

                    HttpURLConnection urlConn = null;
                    urlConn = (HttpURLConnection) url.openConnection();

                    InputStream byteStream = urlConn.getInputStream();
                    Reader targetReader = new InputStreamReader(byteStream);

                    // open the url stream, wrap it an a few "readers"
                    BufferedReader reader = new BufferedReader(targetReader);

                    StringBuffer sb = new StringBuffer();


                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

                    String received = sb.toString();
                    JSONObject jObject = new JSONObject(received);
                    JSONObject jj = jObject.getJSONObject("ReturnedData");
                    String index;

                    for (int q = 0; q < 4; q++) {

                        index = String.valueOf(q);
                        TableReadings.put("stop time" + q, jj.getJSONObject("Stop Time").getString(index));
                        TableReadings.put("description" + q, jj.getJSONObject("Stop Description").getString(index));
                        TableReadings.put("distance" + q, jj.getJSONObject("Distance To Stop").getString(index));
                        TableReadings.put("bus number" + q, jj.getJSONObject("Bus Number").getString(index));
                        TableReadings.put("service id" + q, jj.getJSONObject("Service ID").getString(index));
                        TableReadings.put("run id" + q, jj.getJSONObject("Run ID").getString(index));


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (TableReadings.get("run id0") != null) {

                    String runID = TableReadings.get("run id0");

                    Log.i("intern", "GetBusData: Successful");
                    Log.i("intern", runID);
                    GetBusDataSuccess = true;

                }

                return null;
            } else {

                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            super.onPostExecute(jsonObject);


            if (GetBusDataSuccess) {
                InfoClasses.MyInfo.BusRoutes.set(orderOfExecution, TableReadings.get("run id0"));
                InfoClasses.MyInfo.ZonedSchools.set(orderOfExecution, SelectedSchool);
            }

        }
    }

    public static void sendLocations(LatLng latLng, String busRouteID, String busNumber) {

        if (SocketConnected) {
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

    public static void join_AsAdmin() {

        if (SocketConnected) {

            String county = "Henry";

            String dataOut = "{\"action\" : \"joinAdmin\" , \"data\" : {\"county\" : \"" +
                    county + "\"}}";

            Log.e("websocket", "Joined Admin");

            ws.send(dataOut);
        } else {

            Log.e("websocket", "ERROR");
            CreateWebSocketConnection();

        }

    }

    public static void retrieveAllRoutes() {

        if (SocketConnected) {

            String county = InfoClasses.county;

            String dataOut = "{\"action\" : \"getAllRoutes\" , \"data\" : {\"county\" : \"" +
                    county + "\"}}";

            ws.send(dataOut);
            Log.e("websocket", "Recieving all county routes");


        } else {

            Log.e("websocket", "ERROR");
            CreateWebSocketConnection();

        }

    }

    public static void retrieveAllLocations() {

        if (SocketConnected) {

            String county = InfoClasses.county;

            String dataOut = "{\"action\" : \"getBusLocations\", \"data\" : {\"county\" : \"" +
                    county + "\"}}";

            ws.send(dataOut);
            Log.e("websocket", "Recieving all county locations");

        } else {

            Log.e("websocket", "ERROR");
            CreateWebSocketConnection();


        }
    }

    public static void addBus(String busNumber, String driverName, ArrayList<String> routes){

        if (SocketConnected) {

            String county = InfoClasses.county;

            String dataOut = "{\"action\" : \"busEditor\" , \"data\" : {\"county\" : \"" +
                    county + "\" , \"busNumber\" : \"" +
                    busNumber + "\", \"route_one\" : \"" +
                    routes.get(0) + "\", \"route_two\" : \"" +
                    routes.get(1) + "\", \"route_three\" : \"" +
                    routes.get(2) + "\", \"name\" : \"" +
                    driverName + "\"}}";

            ws.send(dataOut);
            Log.e("websocket", "Created Bus " + busNumber);

        } else {

            Log.e("websocket", "ERROR");
            CreateWebSocketConnection();


        }
    }

    public static void joinRoute_AsBus(String busNumber, String busRouteID) {

        if (SocketConnected) {

            String county = InfoClasses.county;

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

    public static void joinRoute_AsBus(String busNumber) {

        if (SocketConnected) {

            String county = InfoClasses.county;

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

    public static void joinRoute_AsRider() {

        if (SocketConnected) {

            if (InfoClasses.MyInfo.BusRoutes.size() == 3) {
                String county = InfoClasses.county;
                ArrayList<String> busRouteID = (ArrayList<String>) InfoClasses.MyInfo.BusRoutes;

                String dataOut = "{\"action\" : \"joinRoute\" , \"data\" : {\"county\" : \"" +
                        county + "\" , \"route1\" : \"" +
                        busRouteID.get(0) + "\", \"route2\" : \"" +
                        busRouteID.get(1) + "\", \"route3\" : \"" +
                        busRouteID.get(2) + "\"}}";

                Log.e("websocket", dataOut);
                ws.send(dataOut);
            }
        } else {

            Log.e("websocket", "ERROR");
            CreateWebSocketConnection();

        }

    }

    public static void joinRoute_AsRider(String routeID) {

        if (SocketConnected) {

            String county = InfoClasses.county;
            ArrayList<String> busRouteID = (ArrayList<String>) InfoClasses.MyInfo.BusRoutes;

            String dataOut = "{\"action\" : \"joinRoute\" , \"data\" : {\"county\" : \"" +
                    county + "\" , \"route1\" : \"" +
                    routeID + "\", \"route2\" : \"" +
                    busRouteID.get(1) + "\", \"route3\" : \"" +
                    busRouteID.get(2) + "\"}}";

            Log.e("websocket", dataOut);
            ws.send(dataOut);
        } else {

            Log.e("websocket", "ERROR");
            CreateWebSocketConnection();

        }

    }

    public static void fetchYourRoutes(String busNumber) {

        if (SocketConnected) {

            String county = InfoClasses.county;

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

        if (SocketConnected && location != null) {

            String county = InfoClasses.county;

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

                if (text.contains("LatLng_data")) {
                    JSONObject object = jObject.getJSONObject("LatLng_data");

                    InfoClasses.Status.Status = InfoClasses.Status.NORMAL;

                    final LatLng BusLocation = new LatLng(object.getDouble("lat"), object.getDouble("lng"));
                    final String RouteID = object.getString("routeID");
                    final String BusNumber = object.getString("busNumber");

                    if (InfoClasses.Mode.RIDER()) {
                        if (InfoClasses.MyInfo.myBuses.containsKey(RouteID)) {

                            InfoClasses.Buses thisBus = InfoClasses.MyInfo.myBuses.get(RouteID);
                            thisBus.BusLocation = BusLocation;

                            if (!thisBus.BusNumber.equals(BusNumber)) {

                                //TODO show a notification or sum
                                thisBus.BusNumber = BusNumber;
                            }

                            Log.e("tag", "Updated bus info");

                        } else {

                            InfoClasses.Buses newBus = new InfoClasses().new Buses(BusNumber, BusLocation, InfoClasses.MyInfo.getSchoolFromRoute(RouteID));
                            InfoClasses.MyInfo.myBuses.put(RouteID, newBus);

                            Log.e("tag", "Created a new bus instance");
                        }

                        MainActivity.UpdatesAvailable = true;
                    } else {

                        if (InfoClasses.AdminInfo.CountyBuses.containsKey(BusNumber)) {

                            InfoClasses.Buses thisBus = InfoClasses.AdminInfo.CountyBuses.get(BusNumber);
                            thisBus.BusLocation = BusLocation;
                            thisBus.Route = RouteID;

                            if (!thisBus.BusNumber.equals(BusNumber)) {

                                //TODO show a notification or sum
                                thisBus.BusNumber = BusNumber;
                            }

                            Log.e("tag", "Updated bus info");
                            thisBus.Active = true;

                        }

                        MainActivity.UpdatesAvailable = true;
                    }
                }

                if (text.contains("busData")) {
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

                    InfoClasses.BusInfo.AssignedBusRoutes = myRoutes;
                    Log.i("websocket", "jo");


                }

                if (text.contains("allBusLocations")) {
                    JSONObject object = jObject.getJSONObject("allBusLocations");

                    for(int x = 0; x < object.getInt("number"); x++) {

                        JSONObject bb = object.getJSONObject("data").getJSONObject(Integer.toString(x));
                        Log.i("tag", bb.toString());
                        LatLng BusLocation = new LatLng(bb.getDouble("Lat"), bb.getDouble("Lng"));
                        String BusNumber = bb.getString("BusNumber");

                        if (!InfoClasses.AdminInfo.CountyBuses.containsKey(BusNumber)) {

                            InfoClasses.Buses newBus = new InfoClasses().new Buses(BusNumber, BusLocation);
                            InfoClasses.AdminInfo.CountyBuses.put(BusNumber, newBus);
                            InfoClasses.AdminInfo.AvailableBusNumbers.add(BusNumber);

                            Log.e("websockets", "Created a new bus instance");
                            MainActivity.UpdatesAvailable = true;

                        }
                    }
                }

                if (text.contains("AllRoutes")) {
                    JSONObject object = jObject.getJSONObject("AllRoutes");
                    InfoClasses.AdminInfo.AvailableRoutes.clear();
                    for(int x = 0; x < object.getInt("number"); x++) {

                        String bb = object.getJSONArray("data").getJSONArray(x).getString(1);
                        InfoClasses.AdminInfo.AvailableRoutes.add(bb);
                    }
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
            Log.e("websocket", "Error : " + t.getMessage());

        }
    }

    public static void CreateWebSocketConnection() {

        if (!SocketConnected) {
            Request request = new Request.Builder().url(WebQuery_path).build();
            EchoWebSocketListener listener = new EchoWebSocketListener();

            OkHttpClient client = new OkHttpClient();

            ws = client.newWebSocket(request, listener);
            client.dispatcher().executorService().shutdown();
            SocketConnected = true;
        }
    }

    public static void CloseWebSocket() {

        ws.cancel();
    }
}
