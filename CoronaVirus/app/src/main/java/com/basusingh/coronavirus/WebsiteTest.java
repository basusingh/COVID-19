package com.basusingh.coronavirus;


import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basusingh.coronavirus.adapter.Adapter_Tracker_Country;
import com.basusingh.coronavirus.database.tracker.TrackerDatabaseClient;
import com.basusingh.coronavirus.database.tracker.TrackerItems;
import com.basusingh.coronavirus.fragment.tracker;
import com.basusingh.coronavirus.utils.NetworkSingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WebsiteTest extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_website_test);

        loadData();
    }

    private void loadData(){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.DATA_GET_ALL_COUNTRY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        Log.e("Response", response);
                        decodeData(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", "Get all country");
                    }
                }) {
        };

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }


    private void decodeData(final String response){
        new LoadDataTask().execute(response);
    }

    private class LoadDataTask extends AsyncTask<String, Void, Boolean> {
        final List<TrackerItems> list = new ArrayList<>();
        boolean done = false;
        protected Boolean doInBackground(String... urls) {
            Log.e("Starting decode", "Now---------------");
            try{
                JSONObject o = new JSONObject(urls[0]);
                JSONArray a = o.getJSONArray("countryitems");
                JSONObject o1 = a.getJSONObject(0);
                for(int i = 1; i<o1.length(); i++) {
                    JSONObject o2 = o1.getJSONObject(String.valueOf(i));
                    TrackerItems items = new TrackerItems();
                    items.setOurid(o2.getString("ourid"));
                }
                return true;
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(Boolean result) {
           Log.e("Decoding", "Complete");
        }
    }


}
