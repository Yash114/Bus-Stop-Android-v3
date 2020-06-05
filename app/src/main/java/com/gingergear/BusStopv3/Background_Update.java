package com.gingergear.BusStopv3;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.gms.maps.model.LatLng;


public class Background_Update extends IntentService {

    public final String FORSERVICE_NOTIFICATION_ID = "1";
    public String LocationString = "";


    public Background_Update() {
        super("Background_Update");
    }

    @SuppressLint("WrongThread")
    @Override
    protected void onHandleIntent(Intent intent) {

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent resultIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "343")
                .setSmallIcon(R.drawable.bus)
                .setColor(16776960)
                .setContentTitle("Bus Stop")
                .setContentText("Your Bus: " + InfoClasses.busInfo.BusNumber + " is currently in route")
                .setLargeIcon(getBitmapFromVectorDrawable(this, R.mipmap.ic_launcher_round))
                .setContentIntent(resultIntent)
                .setPriority(NotificationCompat.DEFAULT_ALL);

        builder.build().flags = Notification.FLAG_ONGOING_EVENT;

        startForeground(343, builder.build());
        Internet.CreateWebSocketConnection();

        int counter = 0;
        LatLng Temp = null;

        int Rider_Driver = InfoClasses.Mode.Rider_Driver;
        int DRIVER = 0;
        int RIDER = 1;

        if (Rider_Driver == DRIVER) {
            if (InfoClasses.Status.Status != InfoClasses.Status.PAUSED && InfoClasses.Status.Status != InfoClasses.Status.Disconnected && InfoClasses.Status.Status != InfoClasses.Status.DONE) {

                InfoClasses.Bluetooth.bluetoothGatt  = InfoClasses.Bluetooth.connectedDevice.connectGatt(this, false, new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                        super.onConnectionStateChange(gatt, status, newState);

//                        DriverFrag.CURRENTLYCONNECTED = BluetoothProfile.STATE_CONNECTED == newState;

                        Log.e("Bluetooth", InfoClasses.Bluetooth.isConnected ? "Connected" : "Not Connected");

                        if (InfoClasses.Bluetooth.isConnected) {

                            InfoClasses.Bluetooth.bluetoothGatt.discoverServices();

                            return;

                        }
                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        super.onServicesDiscovered(gatt, status);


                        if (status == 0) {

                            Log.e("Bluetooth", "got something");
                            Log.e("Bluetooth", "onServicesDiscovered received: " + BluetoothGatt.GATT_SUCCESS);

                            for (BluetoothGattService i : gatt.getServices()) {

                                if (i.getUuid().toString().equals("0000ffe0-0000-1000-8000-00805f9b34fb")) {

                                    for (BluetoothGattCharacteristic j : i.getCharacteristics()) {
                                        Boolean b = gatt.setCharacteristicNotification(j, true);
                                        Log.e("Bluetooth", b ? "yes" : "no");
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                        super.onCharacteristicChanged(gatt, characteristic);

                        byte[] bytes = characteristic.getValue();
                        String buffer = "";
                        for (int u = 0; u < bytes.length; u++) {

                            buffer += (char) bytes[u];
                        }

                        if (buffer.length() >= 10) {

                            LocationString = buffer;

                            Log.i("Bluetooth", "You just recieved: " + buffer);
                            Log.e("Bluetooth", characteristic.getUuid().toString());

                        }
                    }

                });
            }
        }

        while (counter <= 20) {

            if (Rider_Driver == DRIVER) {
                if (InfoClasses.Status.Status != InfoClasses.Status.PAUSED && InfoClasses.Status.Status != InfoClasses.Status.Disconnected && InfoClasses.Status.Status != InfoClasses.Status.DONE) {

                    if (InfoClasses.Bluetooth.isConnected) {

                        if (!LocationString.equals("")) {

                            counter = 0;

                            try {
                                LatLng buf = new LatLng(Double.parseDouble(
                                        LocationString.substring(0, LocationString.indexOf(','))),
                                        Double.parseDouble(LocationString.substring(LocationString.indexOf(',') + 1)));

                                LocationString = "";

                                if (InfoClasses.busInfo.BusLocation != null && buf != null) {
                                    if (Math.abs(InfoClasses.busInfo.BusLocation.latitude - buf.latitude) < 0.2 &&
                                            Math.abs(InfoClasses.busInfo.BusLocation.longitude - buf.longitude) < 0.2) {


                                        if (Math.abs(InfoClasses.busInfo.BusLocation.latitude - buf.latitude) > 0.000001 ||
                                                Math.abs(InfoClasses.busInfo.BusLocation.longitude - buf.longitude) > 0.000001) {
                                            if (buf != InfoClasses.busInfo.BusLocation) {

                                                    InfoClasses.busInfo.BusLocation = buf;

                                                    Internet.sendLocations(buf, InfoClasses.busInfo.CurrentRoute, InfoClasses.busInfo.BusNumber);


                                                    if (InfoClasses.Status.Status != InfoClasses.Status.NORMAL)
                                                        InfoClasses.Status.Status = InfoClasses.Status.NORMAL;
                                            }
                                        }
                                    }

                                } else {

                                    if (buf != null) {
                                        if (Temp == null) {
                                            Temp = buf;

                                        } else {

                                            if (Math.abs(buf.latitude - Temp.latitude) < 0.0001 && Math.abs(buf.longitude - Temp.longitude) < 0.0001) {                                                 InfoClasses.busInfo.BusLocation = buf;

                                                    Internet.sendLocations(buf, InfoClasses.busInfo.CurrentRoute, InfoClasses.busInfo.BusNumber);

                                                    if (InfoClasses.Status.Status != InfoClasses.Status.NORMAL)
                                                        InfoClasses.Status.Status = InfoClasses.Status.NORMAL;

                                            } else {
                                                Temp = null;
                                            }
                                        }

                                    }

                                }
                            } catch (StringIndexOutOfBoundsException i) {

                            } catch (NumberFormatException b) {

                            }
                        } else {

                            counter += 1;

                        }
                    }

                } else if (InfoClasses.Status.Status == InfoClasses.Status.PAUSED) {
                    stopSelf();

                } else {
                    stopSelf();
                }
            }

            try {

                Thread.sleep(150);
            } catch (InterruptedException e) {
            }
        }

        InfoClasses.Status.Status = InfoClasses.Status.Disconnected;
        Internet.CloseWebSocket();

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(400);

        InfoClasses.Bluetooth.disconnectBluetoothDevice();

        stopSelf();
        Thread.currentThread().interrupt();

    }


    private static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}
