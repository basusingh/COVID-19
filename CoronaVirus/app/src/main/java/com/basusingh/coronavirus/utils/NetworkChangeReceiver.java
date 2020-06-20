package com.basusingh.coronavirus.utils;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.basusingh.coronavirus.Constant;

public class NetworkChangeReceiver extends BroadcastReceiver {

    SharedPreferences sharedPref;
    @Override
    public void onReceive(final Context context, final Intent intent) {

        sharedPref = context.getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);

        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

       try{
           final android.net.NetworkInfo wifi = connMgr
                   .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

           final android.net.NetworkInfo mobile = connMgr
                   .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

           if (wifi.isAvailable() || mobile.isAvailable()) {
               Log.e("Network receiver", "fired");
               final SharedPreferences sharedPref = context.getSharedPreferences(
                       Constant.app_pref, Context.MODE_PRIVATE);
               if(sharedPref.getBoolean(Constant.app_pref_status_live_tracking, false) || sharedPref.getBoolean(Constant.app_pref_status_district_tracking, false)){
                   if(DetectConnection.checkInternetConnection(context) && isLocationEnabled(context) && hasPermissions(context, PERMISSIONS) && !isLocationNotAvailable()){
                       Intent serviceIntent = new Intent(context, LiveDataForegroundService.class);
                       serviceIntent.putExtra("type", 3);
                       ContextCompat.startForegroundService(context, serviceIntent);
                   }
               }
           }
       } catch (Exception e){
           e.printStackTrace();
       }
    }


    private boolean isLocationNotAvailable(){
        return sharedPref.getString(Constant.user_country, null) == null;
    }

    String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    public static Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return  lm != null && lm.isLocationEnabled();
        } else {
            int mode = android.provider.Settings.Secure.getInt(context.getContentResolver(), android.provider.Settings.Secure.LOCATION_MODE,
                    android.provider.Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != android.provider.Settings.Secure.LOCATION_MODE_OFF);
        }
    }
}
