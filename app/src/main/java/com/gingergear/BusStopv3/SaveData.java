package com.gingergear.BusStopv3;

import android.content.Context;
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
    static String filename;
    static String filename_Bus;
    static String filename_MyBus;
    static String filename_MyPos;




    public SaveData(Context context, String Filename){

        mContext = context;
        filename_Bus = Filename + "_bus.txt";
        filename_MyBus = Filename + "_my_bus.txt";
        filename_MyPos = Filename + "_my_pos.txt";


        filename = Filename + ".txt";
    }

    public static void Save(String SessionID) {

        FileOutputStream FOS = null;
        File_Name = filename;

        try {

            JSONObject Jsonobj = new JSONObject();
            Jsonobj.put("SessionID", SessionID);

            String data = Jsonobj.toString();
            Log.i("Save", "Saved: " + SessionID + " as SessionID");

            FOS = mContext.openFileOutput(File_Name, Context.MODE_PRIVATE);
            FOS.write(data.getBytes());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (FOS != null) {

                try {
                    FOS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void Save(Boolean RiderORdriver) {

        FileOutputStream FOS = null;
        File_Name = filename;

        try {

            JSONObject Jsonobj = new JSONObject();
            Jsonobj.put("mode", RiderORdriver ? "rider" : "driver");

            String data = Jsonobj.toString();

            FOS = mContext.openFileOutput(File_Name, Context.MODE_PRIVATE);
            FOS.write(data.getBytes());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (FOS != null) {

                try {
                    FOS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void Save(String BusCode, String BusNumber) {

        FileOutputStream FOS = null;
        File_Name = filename;

        try {

            JSONObject Jsonobj = new JSONObject();
            Jsonobj.put("busCode", BusCode);
            Jsonobj.put("busNumber", BusNumber);

            String data = Jsonobj.toString();

            FOS = mContext.openFileOutput(File_Name, Context.MODE_PRIVATE);
            FOS.write(data.getBytes());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (FOS != null) {

                try {
                    FOS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void Save(Boolean RiderORdriver, String BusCode, String BusNumber) {

        FileOutputStream FOS = null;
        File_Name = filename;

        try {

            JSONObject Jsonobj = new JSONObject();
            Jsonobj.put("mode", RiderORdriver ? "rider" : "driver");
            Jsonobj.put("busCode", BusCode);
            Jsonobj.put("busNumber", BusNumber);

            String data = Jsonobj.toString();

            FOS = mContext.openFileOutput(File_Name, Context.MODE_PRIVATE);
            FOS.write(data.getBytes());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (FOS != null) {

                try {
                    FOS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void SaveBus(Boolean savedBus, String URL, String BusNumber) {

        FileOutputStream FOS = null;
        File_Name = filename_Bus;

        try {

            if(savedBus) {
                JSONObject Jsonobj = new JSONObject();
                Jsonobj.put("SchoolURL", URL);
                Jsonobj.put("BusNumber", BusNumber);


                String data = Jsonobj.toString();
                Log.i("Save", "Saved: " + URL + " as your SchoolURL");
                Log.i("Save", "Saved: " + BusNumber + " as your Default Bus");

                try {
                    Looper.prepare();
                } catch (RuntimeException e){}
                Toast.makeText(mContext, BusNumber + " is now your default bus", Toast.LENGTH_SHORT).show();

                FOS = mContext.openFileOutput(File_Name, Context.MODE_PRIVATE);
                FOS.write(data.getBytes());
            } else {

                File f = new File(File_Name);
                f.delete();
            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (FOS != null) {

                try {
                    FOS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void SaveMyBus(Boolean savedBus, String BusNumber){
        FileOutputStream FOS = null;
        File_Name = filename_MyBus;

        try {

            if(savedBus) {
                JSONObject Jsonobj = new JSONObject();
                Jsonobj.put("MyBusNumber", BusNumber);

                String data = Jsonobj.toString();
                Log.i("Save", "Saved: " + BusNumber + " as your Bus");

                FOS = mContext.openFileOutput(File_Name, Context.MODE_PRIVATE);
                FOS.write(data.getBytes());
            } else {

                File f = new File(File_Name);
                f.canExecute();
            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (FOS != null) {

                try {
                    FOS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void SaveMyHomepos(LatLng latLng){
        FileOutputStream FOS = null;
        File_Name = filename_MyPos;

        try {
                JSONObject Jsonobj = new JSONObject();
                Jsonobj.put("Latitude", latLng.latitude);
                Jsonobj.put("Longitude", latLng.longitude);


                String data = Jsonobj.toString();
                Log.i("Save", "Saved: your Long.. as: " + latLng.longitude);
                Log.i("Save", "Saved: your Lat.. as: " + latLng.latitude);


                FOS = mContext.openFileOutput(File_Name, Context.MODE_PRIVATE);
                FOS.write(data.getBytes());


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (FOS != null) {

                try {
                    FOS.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static String readSaved(String dataName) {

        FileInputStream FIS;

        try {

            FIS = mContext.openFileInput(filename);

            InputStreamReader ISR = new InputStreamReader(FIS);
            BufferedReader BR = new BufferedReader(ISR);
            StringBuilder SB = new StringBuilder();
            String gatheredText;

            while ((gatheredText = BR.readLine()) != null) {
                SB.append(gatheredText).append("\n");
            }

            JSONObject JSONdata = new JSONObject(SB.toString());

            String out = JSONdata.getString(dataName);

            Log.e("Save", "Retrieved " + "\"" +  out + "\"" + " in " + "\"" + dataName + "\"");

            return out;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;
    }
    public static String readSavedBus(String dataName) {

        FileInputStream FIS;

        try {

            FIS = mContext.openFileInput(filename_Bus);

            InputStreamReader ISR = new InputStreamReader(FIS);
            BufferedReader BR = new BufferedReader(ISR);
            StringBuilder SB = new StringBuilder();
            String gatheredText;

            while ((gatheredText = BR.readLine()) != null) {
                SB.append(gatheredText).append("\n");
            }

            JSONObject JSONdata = new JSONObject(SB.toString());

            String out = JSONdata.getString(dataName);

            Log.e("Save", "Retrieved " + "\"" +  out + "\"" + " in " + "\"" + dataName + "\"");

            return out;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;
    }
    public static String readMySavedBus(String dataName) {

        FileInputStream FIS;

        try {

            FIS = mContext.openFileInput(filename_MyBus);

            InputStreamReader ISR = new InputStreamReader(FIS);
            BufferedReader BR = new BufferedReader(ISR);
            StringBuilder SB = new StringBuilder();
            String gatheredText;

            while ((gatheredText = BR.readLine()) != null) {
                SB.append(gatheredText).append("\n");
            }

            JSONObject JSONdata = new JSONObject(SB.toString());

            String out = JSONdata.getString(dataName);

            Log.e("Save", "Retrieved " + "\"" +  out + "\"" + " in " + "\"" + dataName + "\"");

            return out;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;
    }
    public static LatLng readMySavedPos() {

        FileInputStream FIS;

        try {

            FIS = mContext.openFileInput(filename_MyPos);

            InputStreamReader ISR = new InputStreamReader(FIS);
            BufferedReader BR = new BufferedReader(ISR);
            StringBuilder SB = new StringBuilder();
            String gatheredText;

            while ((gatheredText = BR.readLine()) != null) {
                SB.append(gatheredText).append("\n");
            }

            JSONObject JSONdata = new JSONObject(SB.toString());

            String out = JSONdata.getString("Latitude");
            String out1 = JSONdata.getString("Longitude");

            Log.e("Save", "Retrieved " + "\"" +  out + "\"" + " in " + "\"" + "Latitude" + "\"");
            Log.e("Save", "Retrieved " + "\"" +  out1 + "\"" + " in " + "\"" + "Longitude" + "\"");


            return new LatLng(Double.parseDouble(out), Double.parseDouble(out1));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;
    }

}
