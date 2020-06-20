package com.basusingh.coronavirus.utils;

import android.Manifest;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.basusingh.coronavirus.Constant;
import com.google.android.gms.common.internal.Constants;

public class JobSchedulerLaunchLiveTracker extends JobService{

    SharedPreferences sharedPref;
    private static final String TAG = JobSchedulerLaunchLiveTracker.class.getSimpleName();
    JobParameters parameters;

    public JobSchedulerLaunchLiveTracker() {
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

    private boolean isLocationNotAvailable(){
        return sharedPref.getString(Constant.user_country, null) == null;
    }


    @Override
    public boolean onStartJob(JobParameters params) {
        sharedPref = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);
        Log.e(TAG, "Live Tracker Service created");
        parameters = params;
        if(sharedPref.getBoolean(Constant.app_pref_status_live_tracking, false) || sharedPref.getBoolean(Constant.app_pref_status_district_tracking, false)){
            try{
                if(DetectConnection.checkInternetConnection(getApplicationContext()) && isLocationEnabled(getApplicationContext()) && hasPermissions(this, PERMISSIONS) && !isLocationNotAvailable()){
                    startLiveTracking();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        finishAndReschedule();
        return true;
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

    private void finishAndReschedule(){
        jobFinished(parameters, true);
    }
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "onStopJob");
        return false;
    }

    private void startLiveTracking(){
        Intent serviceIntent = new Intent(getApplicationContext(), LiveDataForegroundService.class);
        serviceIntent.putExtra("type", 0);
        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
    }
}
