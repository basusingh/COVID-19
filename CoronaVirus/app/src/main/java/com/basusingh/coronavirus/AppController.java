package com.basusingh.coronavirus;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Constraints;
import androidx.work.Logger;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.basusingh.coronavirus.utils.JobSchedulerCheckAppVersion;
import com.basusingh.coronavirus.utils.JobSchedulerLaunchLiveTracker;
import com.basusingh.coronavirus.utils.LocationProviderChangedReceiver;
import com.basusingh.coronavirus.utils.NetworkChangeReceiver;
import com.basusingh.coronavirus.utils.NotificationCancelReceiver;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class AppController extends Application {

    //Getting tag it will be used for displaying log and it is optional
    public static final String TAG = AppController.class.getSimpleName();

    //Creating a volley request queue object
    private RequestQueue mRequestQueue;

    //Creating class object
    private static AppController mInstance;

    //class instance will be initialized on app launch
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                try{
                    Log.e("Token", task.getResult().getToken());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerBroadcast();
        }

        final SharedPreferences sharedPref = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);

        if(!sharedPref.getBoolean(Constant.IS_VERSION_CHECK_JOB_SCHEDULED, false)){
           try{
               JobScheduler jobScheduler = (JobScheduler)getApplicationContext()
                       .getSystemService(Context.JOB_SCHEDULER_SERVICE);
               ComponentName componentName = new ComponentName(this,
                       JobSchedulerCheckAppVersion.class);
               JobInfo jobInfoObj = new JobInfo.Builder(1665, componentName)
                       .setPeriodic(21600000).setPersisted(true)
                       .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                       .build();

               JobInfo liveTracker = new JobInfo.Builder(2200, new ComponentName(this, JobSchedulerLaunchLiveTracker.class))
                       .setPeriodic(21600000).setPersisted(true)
                       .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                       .build();


               jobScheduler.schedule(liveTracker);
               jobScheduler.schedule(jobInfoObj);

               SharedPreferences.Editor editor = sharedPref.edit();
               editor.putBoolean(Constant.IS_VERSION_CHECK_JOB_SCHEDULED, true);
               editor.apply();

           } catch (Exception e){
               e.printStackTrace();
           }
        }
    }


    private void registerBroadcast(){
        BroadcastReceiver br = new LocationProviderChangedReceiver();
        IntentFilter filter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        this.registerReceiver(br, filter);

        BroadcastReceiver br1 = new NotificationCancelReceiver();
        IntentFilter filter1 = new IntentFilter("com.basusingh.coronavirus.android.action.broadcast");
        this.registerReceiver(br1, filter1);


        BroadcastReceiver br2 = new NetworkChangeReceiver();
        IntentFilter filter2 = new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        this.registerReceiver(br2, filter2);

    }


    //Public static method to get the instance of this class
    public static synchronized AppController getInstance() {
        return mInstance;
    }

    //This method would return the request queue
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }


    //This method would add the requeust to the queue for execution
    public <T> void addToRequestQueue(Request<T> req) {
        //Setting a tag to the request
        req.setTag(TAG);

        //calling the method to get the request queue and adding the requeust the the queuue
        getRequestQueue().add(req);
    }

    //method to cancle the pending requests
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

}
