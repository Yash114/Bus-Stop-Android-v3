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
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;

public class FirebaseNotifications extends FirebaseMessagingService {

    private static FirebaseFunctions mFunctions;
    private static String token;

    private HashMap<String, String> notificationKeys = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        notificationKeys.put("Admin Alert", "876ytygrjhIUOEDV");
    }

    @Override
    public void onNewToken(String s) {

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {

                        // Get new Instance ID token
                        token = task.getResult().getToken();
                    }

                });
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String notificationType = remoteMessage.getData().get("notificationType");

        Looper.prepare();
        if(notificationType!= null){
            if(notificationType.equals(notificationKeys.get("Admin Alert"))){

                Internet.retrieveBusErrors();
            }
        }
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

        Internet.sendBusError(addy, time);
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
        String time  = Calendar.getInstance().getTime().toString();
        String addy = InfoClasses.BusInfo.LastGatheredAddress;

        Internet.sendBusError(addy, time);
        InfoClasses.ToastMessages.sendingBusError.show();

        data.put("token", token);
        data.put("county", InfoClasses.county);
        data.put("currentRoute", InfoClasses.BusInfo.CurrentRoute);
        data.put("addy", addy);
        data.put("time", time );
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

    public static void addToRoute() {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("county", InfoClasses.county);

        for (String x : InfoClasses.MyInfo.BusRoutes) {

            data.put("busRoute", x);

            mFunctions
                    .getHttpsCallable("addDeviceToRoute")
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

    public static void removeFromRoute() {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("county", InfoClasses.county);


        for (String x : InfoClasses.MyInfo.BusRoutes) {

            data.put("busRoute", x);

            mFunctions
                    .getHttpsCallable("removeDeviceFromRoute")
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

    public static void addABus() {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("county", InfoClasses.county);
        data.put("busRoute", InfoClasses.BusInfo.CurrentRoute);
        mFunctions
                .getHttpsCallable("createBusGroup")
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

    public static void sendAlertToRiders() {
        // Create the arguments to the callable function.
        Map<String, Object> data = new HashMap<>();
        data.put("county", InfoClasses.county);
        data.put("currentRoute", InfoClasses.BusInfo.CurrentRoute);
        data.put("busNumber", InfoClasses.BusInfo.BusNumber);

        mFunctions
                .getHttpsCallable("saveAndSendBusAlert_ForRiders")
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
