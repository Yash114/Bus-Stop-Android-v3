package com.gingertech.BusStopv3;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FirebaseNotifications extends FirebaseMessagingService {

    private static FirebaseFunctions mFunctions;
    private static String token;

    @Override
    public void onNewToken(String s) {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                        // Get new Instance ID token
                        token = task.getResult().getToken();

                        // Log and toast
                        Log.i("tag", token);

                    }

                });
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.e("notif", "I just received something");
    }

    public static void initialize(){

        mFunctions = FirebaseFunctions.getInstance();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                        // Get new Instance ID token
                        token = task.getResult().getToken();

                    }

                });
    }

    public static void addAsAdmin() {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("county", InfoClasses.county);

        mFunctions
                .getHttpsCallable("addDeviceToAdminGroup")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });
    }

    public static void removeAsAdmin() {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("county", InfoClasses.county);

        mFunctions
                .getHttpsCallable("removeDeviceFromAdminGroup")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });
    }

    public static void sendBusAlert(){

        // Create the arguments to the callable function.

        String time  = Calendar.getInstance().getTime().toString();
        String addy = InfoClasses.BusInfo.LastGatheredAddress;

        Internet.sendBusError(time, addy);
        InfoClasses.ToastMessages.sendingBusError.show();

        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("county", InfoClasses.county);
        data.put("currentRoute", InfoClasses.BusInfo.CurrentRoute);
        data.put("addy", addy);
        data.put("time", time );
        data.put("busNumber", InfoClasses.BusInfo.BusNumber);


        mFunctions
                .getHttpsCallable("saveAndSendBusAlert")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });

    }

    public static void sendBusAlert_noLocation(){

        // Create the arguments to the callable function.
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("county", InfoClasses.county);
        data.put("currentRoute", InfoClasses.BusInfo.CurrentRoute);
        data.put("addy", "unresolved");
        data.put("time", Calendar.getInstance().getTime().toString());
        data.put("busNumber", InfoClasses.BusInfo.BusNumber);

        InfoClasses.ToastMessages.sendingBusError_noloc.show();

        mFunctions
                .getHttpsCallable("saveAndSendBusAlert")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, String>() {
                    @Override
                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        // This continuation runs on either success or failure, but if the task
                        // has failed then getResult() will throw an Exception which will be
                        // propagated down.
                        String result = (String) task.getResult().getData();
                        return result;
                    }
                });

    }
}
