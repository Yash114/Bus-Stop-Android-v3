package com.gingertech.BusStopv3;

import android.app.IntentService;
import android.app.Notification;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
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

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.e("Bluetooth", " hi");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "343")
                .setSmallIcon(R.drawable.bus)
                .setColor(16776960)
                .setContentTitle("Bus Stop")
                .setContentText("Your Bus: " + InfoClasses.BusInfo.BusNumber + " is currently in route")
                .setLargeIcon(getBitmapFromVectorDrawable(this, R.mipmap.ic_launcher_round))
                .setPriority(NotificationCompat.DEFAULT_ALL);

        builder.build().flags = Notification.FLAG_ONGOING_EVENT;

        startForeground(343, builder.build());
        Internet.CreateWebSocketConnection();

        int counter = 0;
        LatLng Temp = null;

        if (InfoClasses.Mode.DRIVER()) {

            InfoClasses.Bluetooth.bluetoothGatt = InfoClasses.Bluetooth.connectedDevice.connectGatt(this, true, new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

                    super.onConnectionStateChange(gatt, status, newState);


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


        while (counter <= 20) {

                if (InfoClasses.Bluetooth.isConnected) {

                    if (!LocationString.equals("")) {

                        counter = 0;

                        try {
                            LatLng buf = new LatLng(Double.parseDouble(
                                    LocationString.substring(0, LocationString.indexOf(','))),
                                    Double.parseDouble(LocationString.substring(LocationString.indexOf(',') + 1)));

                            LocationString = "";

//
//                            if(MapFragment.coordAuthenticatior(buf)) {
                                Internet.sendLocations(buf, InfoClasses.BusInfo.CurrentRoute, InfoClasses.BusInfo.BusNumber);
                                InfoClasses.BusInfo.BusLocation = buf;
//                            }


                        } catch (StringIndexOutOfBoundsException i) {

                        } catch (NumberFormatException b) {

                        }
                    }
                } else {
                    stopSelf();

                }

            try {

                Thread.sleep(150);
            } catch (InterruptedException e) {
            }
            counter -= 1;
        }

        InfoClasses.Status.Status = InfoClasses.Status.Disconnected;
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(400);

        InfoClasses.BusInfo.disconnectFromBus(this);
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

    @Override
    public void onDestroy() {
        Thread.currentThread().interrupt();
        super.onDestroy();
    }
}
