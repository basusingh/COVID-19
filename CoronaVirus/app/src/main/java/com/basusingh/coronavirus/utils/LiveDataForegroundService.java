package com.basusingh.coronavirus.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basusingh.coronavirus.Constant;
import com.basusingh.coronavirus.IndiaStateList;
import com.basusingh.coronavirus.MainActivity;
import com.basusingh.coronavirus.R;
import com.basusingh.coronavirus.database.districtsubscription.StateDataDatabaseClient;
import com.basusingh.coronavirus.database.districtsubscription.StateDataItems;
import com.basusingh.coronavirus.database.tracker.TrackerDatabaseClient;
import com.basusingh.coronavirus.database.tracker.TrackerItems;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class LiveDataForegroundService extends Service {

    int NOTIFICATION_ID = 1010;
    TrackerItems items = new TrackerItems();
    private Handler mHandler, mDistrictHandler;
    private boolean isRunning = false, isLiveDataActive = false;
    SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        mHandler = new Handler();
        mDistrictHandler = new Handler();
        isRunning = true;
        sharedPreferences = getSharedPreferences(Constant.app_pref, Context.MODE_PRIVATE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try{
            mHandler.removeCallbacks(mLoadData);
            mDistrictHandler.removeCallbacks(mLoadDistrictData);
        } catch (Exception e){
            e.printStackTrace();
        }
        if(sharedPreferences.getBoolean(Constant.app_pref_status_live_tracking, false)){
            if(sharedPreferences.getString(Constant.user_country, null) != null){
                if(!isLiveDataActive){
                    isLiveDataActive = true;
                }
                startRepeatingTask();
            } else {
                isLiveDataActive = false;
            }
        } else {
            stopRepeatingTask();
        }
        if(sharedPreferences.getBoolean(Constant.app_pref_status_district_tracking, false)){
            if(!isLiveDataActive){
                showMessageNotification("Live district tracking");
            }
            startDistrictRepeatingTask();
        } else if(!isLiveDataActive){
            stopDistrictRepeatingTask();
            stopSelf();
        }
        return START_STICKY;
    }

    private void startDistrictRepeatingTask(){
        mLoadDistrictData.run();
    }

    private void stopDistrictRepeatingTask(){
        mDistrictHandler.removeCallbacks(mLoadDistrictData);
    }

    void startRepeatingTask() {
        mLoadData.run();
    }


    void stopRepeatingTask() {
        isLiveDataActive = false;
        mHandler.removeCallbacks(mLoadData);
    }

    Runnable mLoadDistrictData = new Runnable() {
        @Override
        public void run() {
            try {
                loadDistrictData();
            } finally {
                mDistrictHandler.postDelayed(mLoadDistrictData,900000);
            }
        }
    };

    Runnable mLoadData = new Runnable() {
        @Override
        public void run() {
            try {
                isLiveDataActive = true;
                showMessageNotification("Loading live data");
                loadCountryData();
            } finally {
                mHandler.postDelayed(mLoadData,900000);
            }
        }
    };

    private void loadDistrictData(){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.DATA_GET_INDIA_STATE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        decodeDistrictData(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", "Get district");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadDistrictData();
                            }
                        }, 30000);
                    }
                }) {
        };

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }


    private void decodeDistrictData(final String response){
        new LoadDistrictDataTask().execute(response);
    }

    private class LoadDistrictDataTask extends AsyncTask<String, Void, Boolean> {
        List<StateDataItems> offlineList = new ArrayList<>();
        List<String> districtList = new ArrayList<>();
        protected Boolean doInBackground(String... urls) {
            offlineList = StateDataDatabaseClient.getInstance(getApplicationContext()).getStateDataAppDatabase().stateDataDao().getAll();
            for(int i = 0; i<offlineList.size(); i++){
                StateDataItems items = offlineList.get(i);
                districtList.add(items.getDistrictName());
            }
            Log.e("Starting decode", "District--------");
            try{
                JSONObject o = new JSONObject(urls[0]);
                Iterator<String> keys = o.keys();
                for(int i = 0; i<o.length(); i++){
                    String str_Name = keys.next();
                    JSONObject o2 = o.getJSONObject(str_Name);
                    JSONObject o3 = o2.getJSONObject("districtData");
                    Iterator<String> keys1 = o3.keys();
                    for(int j = 0; j<o3.length(); j++){
                        String dtc_Name = keys1.next();
                        if(districtList.contains(dtc_Name)){
                            for(int k = 0; k<offlineList.size(); k++){
                                boolean newCaseAvailable = false;
                                boolean newDeathsAvailable = false;
                                StateDataItems stateDataItems = offlineList.get(k);
                                if(stateDataItems.getDistrictName().equalsIgnoreCase(dtc_Name) && stateDataItems.getStateName().equalsIgnoreCase(str_Name)){
                                    JSONObject o4 = o3.getJSONObject(dtc_Name);
                                    int newCase = Integer.parseInt(o4.getString("confirmed"));
                                    int newDeceased = Integer.parseInt(o4.getString("deceased"));
                                    int oldCase = Integer.parseInt(stateDataItems.getConfirmed());
                                    int oldDeceased = Integer.parseInt(stateDataItems.getDeceased());
                                    Log.e("District", stateDataItems.getDistrictName());
                                    Log.e("New Case", String.valueOf(newCase));
                                    Log.e("Old Case", String.valueOf(oldCase));
                                    Log.e("New Deaths", String.valueOf(newDeceased));
                                    Log.e("old Deaths", String.valueOf(oldDeceased));
                                    if(newCase>oldCase){
                                        newCaseAvailable = true;
                                    }
                                    if(newDeceased>oldDeceased){
                                        newDeathsAvailable = true;
                                    }
                                    if(newCaseAvailable && newDeathsAvailable){
                                        StateDataDatabaseClient.getInstance(getApplicationContext()).getStateDataAppDatabase().stateDataDao().delete(stateDataItems);
                                        stateDataItems.setConfirmed(o4.getString("confirmed"));
                                        stateDataItems.setDeceased(o4.getString("deceased"));
                                        StateDataDatabaseClient.getInstance(getApplicationContext()).getStateDataAppDatabase().stateDataDao().insert(stateDataItems);
                                        sendDistrictNewNotification("New case(s) and death(s) detected", stateDataItems.getDistrictName() + ", " + stateDataItems.getStateName(), stateDataItems.getDistrictName() + ", " + stateDataItems.getStateName() + "\n" + "Total cases: " + stateDataItems.getConfirmed() + "\n" + "Total deaths: " + stateDataItems.getDeceased());
                                    } else if(newCaseAvailable){
                                        StateDataDatabaseClient.getInstance(getApplicationContext()).getStateDataAppDatabase().stateDataDao().deleteByDistrictAndState(stateDataItems.getDistrictName(), stateDataItems.getStateName());
                                        stateDataItems.setConfirmed(o4.getString("confirmed"));
                                        stateDataItems.setDeceased(o4.getString("deceased"));
                                        StateDataDatabaseClient.getInstance(getApplicationContext()).getStateDataAppDatabase().stateDataDao().insert(stateDataItems);
                                        sendDistrictNewNotification("New case(s) detected", stateDataItems.getDistrictName() + ", " + stateDataItems.getStateName(), stateDataItems.getDistrictName() + ", " + stateDataItems.getStateName() + "\n" + "Total cases: " + stateDataItems.getConfirmed() + "\n" + "Total deaths: " + stateDataItems.getDeceased());
                                    } else if(newDeathsAvailable){
                                        StateDataDatabaseClient.getInstance(getApplicationContext()).getStateDataAppDatabase().stateDataDao().deleteByDistrictAndState(stateDataItems.getDistrictName(), stateDataItems.getStateName());
                                        stateDataItems.setConfirmed(o4.getString("confirmed"));
                                        stateDataItems.setDeceased(o4.getString("deceased"));
                                        StateDataDatabaseClient.getInstance(getApplicationContext()).getStateDataAppDatabase().stateDataDao().insert(stateDataItems);
                                        sendDistrictNewNotification("New death(s) detected", stateDataItems.getDistrictName() + ", " + stateDataItems.getStateName(), stateDataItems.getDistrictName() + ", " + stateDataItems.getStateName() + "\n" + "Total cases: " + stateDataItems.getConfirmed() + "\n" + "Total deaths: " + stateDataItems.getDeceased());
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                return true;
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(Boolean result) {

        }
    }


    private void sendDistrictNewNotification(String title, String message, String expandedTitle){
        Intent intent = new Intent(getApplicationContext(), IndiaStateList.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        String channelId = Constant.CHANNEL_NAME_ALL;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(expandedTitle))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        Constant.CHANNEL_DISTRICT_DATA,
                        NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }
            Random rand = new Random();
            notificationManager.notify(rand.nextInt(10000) /* ID of notification */, notificationBuilder.build());
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private void loadCountryData(){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.DATA_GET_ALL_COUNTRY_ALTERNATE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        decodeCountryData(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", "Get all country");
                        checkOfflineData();
                    }
                }) {
        };

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void decodeCountryData(final String response){
        new LoadCountryDataTask().execute(response);
    }

    private class LoadCountryDataTask extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... urls) {
            boolean result = false;
            final SharedPreferences sharedPreferences = getSharedPreferences(Constant.app_pref, Context.MODE_PRIVATE);
            try{
                JSONObject o = new JSONObject(urls[0]);
                JSONArray a = o.getJSONArray("Countries");
                for(int i = 1; i<a.length(); i++){
                    JSONObject o2 = a.getJSONObject(i);
                    if(o2.getString("CountryCode").equalsIgnoreCase(sharedPreferences.getString(Constant.user_country_code, "null")) || o2.getString("Country").equalsIgnoreCase(sharedPreferences.getString(Constant.user_country, "null"))){
                        items.setOurid(String.valueOf(i));
                        items.setTitle(o2.getString("Country"));
                        items.setCode(o2.getString("CountryCode"));
                        items.setTotal_cases(o2.getString("TotalConfirmed"));
                        items.setTotal_recovered(o2.getString("TotalRecovered"));
                        items.setTotal_deaths(o2.getString("TotalDeaths"));
                        items.setTotal_new_case_today(o2.getString("NewConfirmed"));
                        items.setTotal_new_deaths_today(o2.getString("NewDeaths"));
                        items.setTotal_active_cases(String.valueOf(o2.getInt("TotalConfirmed") - o2.getInt("TotalRecovered")));
                        result = true;
                        break;
                    }
                }
                return result;
            } catch (Exception e){
                e.printStackTrace();
                return result;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            try{
                if(!result || items.getTitle() == null || items.getTitle().equalsIgnoreCase("null")){
                    checkOfflineData();
                } else {
                    showFinalNotification();
                }
            } catch (Exception e){
                e.printStackTrace();
                checkOfflineData();
            }
        }
    }


    private void checkOfflineData(){
        new LoadOfflineData().execute();
    }


    private class LoadOfflineData extends AsyncTask<Void, Void, Boolean> {
        List<TrackerItems> list = new ArrayList<>();
        protected Boolean doInBackground(Void... urls) {
            boolean result = false;
            try{
                list = TrackerDatabaseClient.getInstance(getApplicationContext()).getTrackerAppDatabase().trackerDao().getAll();
                if(!list.isEmpty()){
                    for(int i = 0; i<list.size(); i++){
                        TrackerItems trackerItems = list.get(i);
                        if(trackerItems.getCode().equalsIgnoreCase(sharedPreferences.getString(Constant.user_country_code, "null")) || trackerItems.getTitle().equalsIgnoreCase(sharedPreferences.getString(Constant.user_country, "null"))){
                            items = trackerItems;
                            result = true;
                            break;
                        }
                    }
                }
                return result;
            } catch (Exception e){
                e.printStackTrace();
                return result;
            }
        }

        protected void onPostExecute(Boolean result) {
            if(!result){
                showMessageNotification("Live Tracking.");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadCountryData();
                    }
                }, 15000);
            } else {
                showFinalNotification();
            }
        }
    }

    private void showMessageNotification(String message){
        Notification notification = new NotificationCompat.Builder(this, Constant.CHANNEL_NAME_LIVE_DATA)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(message)
                .setOnlyAlertOnce(true).build();
        startForeground(1, notification);
    }


    private void showFinalNotification(){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |  Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        RemoteViews collapsedView = new RemoteViews(getPackageName(), R.layout.view_live_data_collapsed);
        collapsedView.setTextViewText(R.id.total_cases, items.getTotal_cases());
        collapsedView.setTextViewText(R.id.total_deaths, items.getTotal_deaths());
        collapsedView.setTextViewText(R.id.total_recovered, items.getTotal_recovered());
        collapsedView.setTextViewText(R.id.title, "Live Data: " + items.getTitle());

        RemoteViews expandedView = new RemoteViews(getPackageName(), R.layout.view_live_data_expanded);
        expandedView.setTextViewText(R.id.total_cases, items.getTotal_cases());
        expandedView.setTextViewText(R.id.total_deaths, items.getTotal_deaths());
        expandedView.setTextViewText(R.id.total_recovered, items.getTotal_recovered());
        expandedView.setTextViewText(R.id.new_cases_today, items.getTotal_new_case_today());
        expandedView.setTextViewText(R.id.new_deaths_today, items.getTotal_new_deaths_today());
        expandedView.setTextViewText(R.id.title, "Live Data: " + items.getTitle());

        Intent cancelIntent = new Intent(getBaseContext(), NotificationCancelReceiver.class);
        cancelIntent.putExtra("notificationId", NOTIFICATION_ID);
        PendingIntent dismissIntent = PendingIntent.getBroadcast(getBaseContext(), 0, cancelIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, Constant.CHANNEL_NAME_LIVE_DATA)
                .setContentTitle("Live Data: " + items.getTitle())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setCustomContentView(collapsedView)
                //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomBigContentView(expandedView)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_cancel, "Stop tracking", dismissIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        startForeground(1, notification);

        //.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    Constant.CHANNEL_NAME_LIVE_DATA,
                    Constant.CHANNEL_NAME_LIVE_DATA,
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        stopRepeatingTask();
        super.onDestroy();
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
