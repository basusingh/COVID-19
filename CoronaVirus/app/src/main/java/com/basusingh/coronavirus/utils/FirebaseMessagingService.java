package com.basusingh.coronavirus.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basusingh.coronavirus.BuildConfig;
import com.basusingh.coronavirus.Constant;
import com.basusingh.coronavirus.FullScreenMessageViewer;
import com.basusingh.coronavirus.Help;
import com.basusingh.coronavirus.MainActivity;
import com.basusingh.coronavirus.R;
import com.basusingh.coronavirus.database.help.HelpDatabaseClient;
import com.basusingh.coronavirus.database.help.HelpItems;
import com.basusingh.coronavirus.database.socialdistancinglist.SocialDistancingListDatabaseClient;
import com.basusingh.coronavirus.database.socialdistancinglist.SocialDistancingListItems;
import com.basusingh.coronavirus.database.socialdistancingnews.SocialDistancingNewsDatabaseClient;
import com.basusingh.coronavirus.database.socialdistancingnews.SocialDistancingNewsItems;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        sendTokenToServer(token);
    }

    private void sendTokenToServer(final String token){

        final SharedPreferences sharedPref = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);

        if(sharedPref.getBoolean(Constant.app_pref_not_sign_up, true)){
            return;
        }

        final String mName =  sharedPref.getString(Constant.app_pref_user_name, "check");
        final String mEmail = sharedPref.getString(Constant.app_pref_user_email, "check");
        final String mPhone = sharedPref.getString(Constant.app_pref_user_phone, "check");
        final String mGender = sharedPref.getString(Constant.app_pref_user_gender, "check");
        final String mDevice = Build.VERSION.SDK_INT + "+" + Build.DEVICE + "+" + Build.MODEL + "+" + Build.PRODUCT;

        final String location = getUserLocation(sharedPref);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.SERVER_BASE_URL + "login" + "/" + mName + "/" + mEmail + "/" + mPhone + "/" + mGender + "/" + token + "/" + mDevice + "/" + location + "/" + BuildConfig.VERSION_CODE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject o = new JSONObject(response);
                            if(o.getString("error").equalsIgnoreCase("true")){
                                sendTokenToServer(token);
                            } else {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean(Constant.app_pref_not_sign_up, false);
                                editor.apply();
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", "Update FCM");
                    }
                }) {
        };

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }



    private String getUserLocation(SharedPreferences sharedPref){
        if(sharedPref.getString(Constant.user_country, null) == null){
            return "not_registered_yet";
        }

        String divider = "+ ";
        String address = sharedPref.getString(Constant.user_address, "null") + divider;
        String city = sharedPref.getString(Constant.user_city, "null") + divider;
        String state = sharedPref.getString(Constant.user_state, "null") + divider;
        String country = sharedPref.getString(Constant.user_country, "null") + divider;
        String postal = sharedPref.getString(Constant.user_postal_code, "null") + divider;
        String knownname = sharedPref.getString(Constant.user_known_name, "null");

        return (address + city + state + country + postal + knownname);
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());
            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                decodeMessage(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void decodeMessage(JSONObject json){
        try{
            JSONObject data = json.getJSONObject("data");
            String title = data.getString("title");
            String message = data.getString("message");
            String url = data.getString("url");
            String extra_info = data.getString("extra_info");
            String version_name = data.getString("version_name");
            String type = data.getString("type");
            switch (type){
                case "in_focus_new":
                    updateInFocus(title, url);
                    break;
                case "social_distancing_link_new":
                    updateSocialDistancingLink(title, message);
                    break;
                case "social_distancing_news_new":
                    Log.e("Found", "-----------");
                    updateSocialDistancingNews(title, message);
                    break;
                case "helpline_nos_new":
                    updateHelplineNos(title, message);
                    break;
                case "app_update_new":
                    updateApp(title, message, url, version_name);
                    break;
                case "online_status":
                    updateOnline();
                    break;
                case "update_user_location":
                    updateUserLocation();
                    break;
                case "full_image_message":
                    openFullScreenMessageViewer(title, message, url);
                    break;
                case "app_block":
                    blockApp(title, message, version_name);
                    break;
                case "text_noti":
                    Log.e("Success", "Yay-=-----");
                    Log.e("Title", title);
                    break;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void blockApp(String title, String message, String version){
        if(BuildConfig.VERSION_CODE < Integer.parseInt(version)){
            final SharedPreferences sharedPref = getSharedPreferences(
                    Constant.app_pref, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Constant.app_pref_app_blocked_from_usage, true);
            editor.putString(Constant.app_pref_app_blocked_title, title);
            editor.putString(Constant.app_pref_app_blocked_message, message);
            editor.apply();
        }
    }

    private void openFullScreenMessageViewer(String title, String message, String url){
        sendNotification(title, message, decodeURL(url), 4, Constant.CHANNEL_NAME_ALL);
    }

    private void updateUserLocation(){

        final SharedPreferences sharedPref = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);

        if(sharedPref.getString(Constant.user_country, null) == null){
            return;
        }

        String divider = "+ ";
        String address = sharedPref.getString(Constant.user_address, "null") + divider;
        String city = sharedPref.getString(Constant.user_city, "null") + divider;
        String state = sharedPref.getString(Constant.user_state, "null") + divider;
        String country = sharedPref.getString(Constant.user_country, "null") + divider;
        String postal = sharedPref.getString(Constant.user_postal_code, "null") + divider;
        String knownname = sharedPref.getString(Constant.user_known_name, "null");

        String location = address + city + state + country + postal + knownname;

        final String mName =  sharedPref.getString(Constant.app_pref_user_name, "check");
        final String mEmail = sharedPref.getString(Constant.app_pref_user_email, "check");
        final String mPhone = sharedPref.getString(Constant.app_pref_user_phone, "check");
        final String mGender = sharedPref.getString(Constant.app_pref_user_gender, "check");
        final String mDevice = Build.VERSION.SDK_INT + "+" + Build.DEVICE + "+" + Build.MODEL + "+" + Build.PRODUCT  ;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.SERVER_BASE_URL + "updatelocation" + "/" + mName + "/" + mEmail + "/" + mPhone + "/" + mGender + "/" + mDevice + "/" + location + "/" + BuildConfig.VERSION_CODE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", "Update Location");
                    }
                }) {
        };

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }



    private void updateOnline(){

        final SharedPreferences sharedPref = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);

        String location = "not_registered";

        if(sharedPref.getString(Constant.user_country, null) != null){
            String divider = "+ ";
            String address = sharedPref.getString(Constant.user_address, "null") + divider;
            String city = sharedPref.getString(Constant.user_city, "null") + divider;
            String state = sharedPref.getString(Constant.user_state, "null") + divider;
            String country = sharedPref.getString(Constant.user_country, "null") + divider;
            String postal = sharedPref.getString(Constant.user_postal_code, "null") + divider;
            String knownname = sharedPref.getString(Constant.user_known_name, "null");

            location = address + city + state + country + postal + knownname;
        }


        final String mName =  sharedPref.getString(Constant.app_pref_user_name, "check");
        final String mEmail = sharedPref.getString(Constant.app_pref_user_email, "check");
        final String mPhone = sharedPref.getString(Constant.app_pref_user_phone, "check");
        final String mGender = sharedPref.getString(Constant.app_pref_user_gender, "check");
        final String mDevice = Build.VERSION.SDK_INT + "+" + Build.DEVICE + "+" + Build.MODEL + "+" + Build.PRODUCT  ;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.SERVER_BASE_URL + "online" + "/" + mName + "/" + mEmail + "/" + mPhone + "/" + mGender + "/" + mDevice + "/" + location + "/" + BuildConfig.VERSION_CODE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("Response", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", "Update FCM");
                    }
                }) {
        };

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

    }


    private void updateSocialDistancingLink(final String title, final String message){
        final List<SocialDistancingListItems> list = new ArrayList<>();
        final SharedPreferences sharedPref = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.SERVER_BASE_URL + "socialdistancinglist",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            Log.e("Response", response);
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject.getString("error").equalsIgnoreCase("false")){
                                JSONArray a = new JSONArray(jsonObject.getString("message"));
                                for(int i=0; i<a.length(); i++){
                                    JSONObject o = a.getJSONObject(i);
                                    SocialDistancingListItems items = new SocialDistancingListItems();
                                    items.setUrl(o.getString("url"));
                                    items.setTitle(o.getString("title"));
                                    items.setName(o.getString("name"));
                                    items.setUid(o.getInt("uid"));
                                    items.setTimestamp(o.getString("timestamp"));
                                    list.add(items);
                                }
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                       new AsyncTask<Void, Void, Boolean>(){
                            @Override
                           protected Boolean doInBackground(Void... p){
                                SocialDistancingListDatabaseClient.getInstance(getApplicationContext()).getSocialDistancingListAppDatabase().SocialDistancingListDao().deleteAll();
                                SocialDistancingListDatabaseClient.getInstance(getApplicationContext()).getSocialDistancingListAppDatabase().SocialDistancingListDao().insertAll(list);
                                return true;
                            }
                           @Override
                           public void onPostExecute(Boolean b){
                               if(!b){
                                   updateSocialDistancingLink(title, message);
                               } else {
                                   Log.e("Done", "Doneeeeeeeee");
                               }
                           }
                       }.execute();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", "Update Social Distancing Link");
                        }
                }) {
        };

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if(sharedPref.getBoolean(Constant.NOTIFICATION_SOCIAL_DISTANCING_LINK, true)){
                    sendNotification(title, message, "", 1, Constant.CHANNEL_SOCIAL_LINK);
                }
            }
        }, 5000);
    }

    private void updateSocialDistancingNews(final String title, final String message){
        final List<SocialDistancingNewsItems> list = new ArrayList<>();
        final SharedPreferences sharedPref = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.SERVER_BASE_URL + "socialdistancingnews",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject.getString("error").equalsIgnoreCase("false")) {
                                JSONArray a = new JSONArray(jsonObject.getString("message"));
                                for (int i = 0; i < a.length(); i++) {
                                    JSONObject o = a.getJSONObject(i);
                                    SocialDistancingNewsItems items = new SocialDistancingNewsItems();
                                    items.setUrl(o.getString("url"));
                                    items.setName(o.getString("name"));
                                    items.setUid(o.getInt("uid"));
                                    items.setTimestamp(o.getString("timestamp"));
                                    list.add(items);
                                }
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        new AsyncTask<Void, Void, Boolean>(){
                            @Override
                            protected Boolean doInBackground(Void... p){
                                SocialDistancingNewsDatabaseClient.getInstance(getApplicationContext()).getSocialDistancingNewsAppDatabase().SocialDistancingNewsDao().deleteAll();
                                SocialDistancingNewsDatabaseClient.getInstance(getApplicationContext()).getSocialDistancingNewsAppDatabase().SocialDistancingNewsDao().insertAll(list);
                                return true;
                            }
                            @Override
                            public void onPostExecute(Boolean b){
                                if(!b){
                                    updateSocialDistancingNews(title, message);
                                }
                            }
                        }.execute();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", "Update Social Distancing News");
                    }
                }) {
        };

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if(sharedPref.getBoolean(Constant.NOTIFICATION_SOCIAL_DISTANCING_NEWS, true)){
                    sendNotification(title, message, "", 1, Constant.CHANNEL_SOCIAL_NEWS);
                }
            }
        }, 5000);
    }

    private void updateHelplineNos(final String title, final String message){
        final List<HelpItems> list = new ArrayList<>();
        final SharedPreferences sharedPref = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.SERVER_BASE_URL + "helplinenos",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            Log.e("Response", response);
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject.getString("error").equalsIgnoreCase("false")) {
                                JSONArray a = new JSONArray(jsonObject.getString("message"));
                                for (int i = 0; i < a.length(); i++) {
                                    JSONObject o = a.getJSONObject(i);
                                    HelpItems items = new HelpItems();
                                    items.setUid(o.getInt("uid"));
                                    items.setNumber(o.getString("number"));
                                    items.setName(o.getString("name"));
                                    list.add(items);
                                }
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        new AsyncTask<Void, Void, Boolean>(){
                            @Override
                            protected Boolean doInBackground(Void... p){
                                HelpDatabaseClient.getInstance(getApplicationContext()).getHelpAppDatabase().HelpDao().deleteAll();
                                HelpDatabaseClient.getInstance(getApplicationContext()).getHelpAppDatabase().HelpDao().insertAll(list);
                                return true;
                            }
                            @Override
                            public void onPostExecute(Boolean b){
                                if(!b){
                                    updateHelplineNos(title, message);
                                }
                            }
                        }.execute();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", "Update Helpline Nos");
                    }
                }) {
        };

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if(sharedPref.getBoolean(Constant.NOTIFICATION_HELPLINE_NO, true)){
                    sendNotification(title, message, "", 2, Constant.CHANNEL_HELPLINE_NOS);
                }
            }
        }, 5000);
    }

    private void updateInFocus(String title, String url){
        final SharedPreferences sharedPref = getSharedPreferences(
                Constant.home_pref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String mUrl = decodeURL(url);
        editor.putString(Constant.home_pref_news, title);
        editor.putString(Constant.home_pref_url, mUrl);
        editor.apply();
        if(sharedPref.getBoolean(Constant.NOTIFICATION_IN_FOCUS, true)){
            sendNotification(title, "In-Focus", "", 1, Constant.CHANNEL_IN_FOCUS);
        }
    }

    private String decodeURL(String message){
        String finalURL = "";
        try{
            JSONArray a = new JSONArray(message);
            for(int i = 0; i<a.length(); i++){
                if(finalURL.isEmpty()){
                    finalURL = a.getString(i) + "/";
                } else {
                    finalURL = finalURL + "/" + a.getString(i);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return finalURL;
    }

    private void updateApp(String title, String message, String url, String version){
        final SharedPreferences sharedPref = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String mUrl = decodeURL(url);
        editor.putBoolean(Constant.app_update_available, true);
        editor.putString(Constant.app_update_link, mUrl);
        editor.putString(Constant.app_update_version, version);
        editor.apply();
        if(sharedPref.getBoolean(Constant.NOTIFICATION_APP_UPDATE, true)){
            sendNotification(title, message, mUrl, 3, Constant.CHANNEL_NAME_ALL);
        }
    }


    private void sendNotification(String title, String messageBody, String url, int type, String channelName) {
        Intent intent = null;
        switch (type){
            case 1:
                intent = new Intent(this, MainActivity.class);
                break;
            case 2:
                intent = new Intent(this, Help.class);
                break;
            case 3:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                break;
            case 4:
                intent = new Intent(this, FullScreenMessageViewer.class);
                intent.putExtra("title", title);
                intent.putExtra("message", messageBody);
                intent.putExtra("url", url);
                break;
            default:
                intent = new Intent(this, MainActivity.class);
                break;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = Constant.CHANNEL_NAME_ALL;
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(messageBody))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        try{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        channelName,
                        NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
