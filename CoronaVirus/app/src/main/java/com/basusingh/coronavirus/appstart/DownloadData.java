package com.basusingh.coronavirus.appstart;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basusingh.coronavirus.Constant;
import com.basusingh.coronavirus.MainActivity;
import com.basusingh.coronavirus.database.help.HelpDatabaseClient;
import com.basusingh.coronavirus.database.help.HelpItems;
import com.basusingh.coronavirus.database.socialdistancinglist.SocialDistancingListDatabaseClient;
import com.basusingh.coronavirus.database.socialdistancinglist.SocialDistancingListItems;
import com.basusingh.coronavirus.database.socialdistancingnews.SocialDistancingNewsDatabaseClient;
import com.basusingh.coronavirus.database.socialdistancingnews.SocialDistancingNewsItems;
import com.basusingh.coronavirus.utils.DetectConnection;
import com.basusingh.coronavirus.utils.NetworkSingleton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.basusingh.coronavirus.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DownloadData extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_data);
        beginDownload();
    }


    private void beginDownload(){
        if(!DetectConnection.checkInternetConnection(getApplicationContext())){
            showErrorAndRetry("No Internet Connection");
            return;
        }
        final List<SocialDistancingListItems> socialDistancingListList = new ArrayList<>();
        final List<SocialDistancingNewsItems> socialDistancingNewsList = new ArrayList<>();
        final List<HelpItems> HelpList = new ArrayList<>();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.SERVER_BASE_URL + "getalldata",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            String mLinks = jsonObject.getString("socialdistancinglist");
                            String mNews = jsonObject.getString("socialdistancingnews");
                            String mHelp = jsonObject.getString("helplinenos");

                            JSONArray a1 = new JSONArray(mLinks);
                            for(int i=0; i<a1.length(); i++){
                                JSONObject o = a1.getJSONObject(i);
                                SocialDistancingListItems items = new SocialDistancingListItems();
                                items.setUrl(o.getString("url"));
                                items.setTitle(o.getString("title"));
                                items.setName(o.getString("name"));
                                items.setUid(o.getInt("uid"));
                                items.setTimestamp(o.getString("timestamp"));
                                socialDistancingListList.add(items);
                            }

                            JSONArray a2 = new JSONArray(mNews);
                            for(int i=0; i<a2.length(); i++){
                                JSONObject o = a2.getJSONObject(i);
                                SocialDistancingNewsItems items = new SocialDistancingNewsItems();
                                items.setUrl(o.getString("url"));
                                items.setName(o.getString("name"));
                                items.setUid(o.getInt("uid"));
                                items.setTimestamp(o.getString("timestamp"));
                                socialDistancingNewsList.add(items);
                            }


                            JSONArray a3 = new JSONArray(mHelp);
                            for(int i=0; i<a3.length(); i++){
                                JSONObject o = a3.getJSONObject(i);
                                HelpItems items = new HelpItems();
                                items.setUid(o.getInt("uid"));
                                items.setNumber(o.getString("number"));
                                items.setName(o.getString("name"));
                                HelpList.add(items);
                            }

                            new AsyncTask<Void, Void, Boolean>(){
                                @Override
                                protected Boolean doInBackground(Void... p){
                                    HelpDatabaseClient.getInstance(getApplicationContext()).getHelpAppDatabase().HelpDao().deleteAll();
                                    HelpDatabaseClient.getInstance(getApplicationContext()).getHelpAppDatabase().HelpDao().insertAll(HelpList);

                                    SocialDistancingNewsDatabaseClient.getInstance(getApplicationContext()).getSocialDistancingNewsAppDatabase().SocialDistancingNewsDao().deleteAll();
                                    SocialDistancingNewsDatabaseClient.getInstance(getApplicationContext()).getSocialDistancingNewsAppDatabase().SocialDistancingNewsDao().insertAll(socialDistancingNewsList);

                                    SocialDistancingListDatabaseClient.getInstance(getApplicationContext()).getSocialDistancingListAppDatabase().SocialDistancingListDao().deleteAll();
                                    SocialDistancingListDatabaseClient.getInstance(getApplicationContext()).getSocialDistancingListAppDatabase().SocialDistancingListDao().insertAll(socialDistancingListList);
                                    return true;
                                }
                                @Override
                                public void onPostExecute(Boolean b){
                                    if(!b){
                                        showErrorAndRetry("An error occurred");
                                    } else {
                                        openMainActivity();
                                    }
                                }
                            }.execute();
                        } catch (Exception e){
                            e.printStackTrace();
                            showErrorAndRetry("An error occurred");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", "Fetch All Data Error");
                        showErrorAndRetry("Internet Connection Error.");
                    }
                }) {
        };

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }


    private void showErrorAndRetry(String message){
        new AlertDialog.Builder(DownloadData.this)
                .setTitle(message)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        beginDownload();
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).show();
    }

    private void openMainActivity(){
        final SharedPreferences sharedPref = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(Constant.app_pref_not_fetch_data, false);
        editor.apply();

        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }

}
