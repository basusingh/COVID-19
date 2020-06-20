package com.basusingh.coronavirus.utils;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basusingh.coronavirus.BuildConfig;
import com.basusingh.coronavirus.Constant;

import org.json.JSONArray;
import org.json.JSONObject;

public class JobSchedulerCheckAppVersion extends JobService {

    JobParameters parameters;
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        parameters = jobParameters;
        loadData();
        return true;
    }
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    private void loadData(){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.SERVER_BASE_URL + "checkappversion",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        Log.e("Version Check", response);
                        decodeData(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error", "Check App Version. Retrying.");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadData();
                            }
                        }, 60000);
                    }
                }) {
        };

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void decodeData(final String response){
        new DecodeTask().execute(response);
    }

    private class DecodeTask extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... urls) {
            try{
                JSONObject o = new JSONObject(urls[0]);
                JSONArray a = o.getJSONArray("message");
                JSONObject m = a.getJSONObject(0);
                int version = m.getInt("app_version");
                if(BuildConfig.VERSION_CODE < version){
                    String title = m.getString("title");
                    String message = m.getString("message");
                    final SharedPreferences sharedPref = getSharedPreferences(
                            Constant.app_pref, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(Constant.app_pref_app_blocked_from_usage, true);
                    editor.putString(Constant.app_pref_app_blocked_title, title);
                    editor.putString(Constant.app_pref_app_blocked_message, message);
                    editor.apply();
                }
                return true;
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean v){
            jobFinished(parameters, true);
        }
    }
}
