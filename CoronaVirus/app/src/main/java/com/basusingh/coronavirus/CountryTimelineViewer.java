package com.basusingh.coronavirus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basusingh.coronavirus.adapter.Adapter_Country_Timeline;
import com.basusingh.coronavirus.utils.ContextWrapper;
import com.basusingh.coronavirus.utils.CoronaTimelineItems;
import com.basusingh.coronavirus.database.tracker.TrackerItems;
import com.basusingh.coronavirus.utils.NetworkSingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class CountryTimelineViewer extends AppCompatActivity {

    TrackerItems items;
    TextView title, total_cases, total_recovered, total_deaths, total_new_cases_today, total_new_deaths_today, total_active_cases;
    ProgressDialog progressDialog;
    Adapter_Country_Timeline mAdapter;
    RecyclerView recyclerView;
    List<CoronaTimelineItems> list;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_timeline_viewer);

        FrameLayout cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            items = (TrackerItems) getIntent().getSerializableExtra("data");
        } else {
            finish();
            return;
        }

        sharedPref = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);

        list = new ArrayList<>();
        progressDialog = new ProgressDialog(CountryTimelineViewer.this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setNestedScrollingEnabled(false);

        title = findViewById(R.id.title);
        total_cases = findViewById(R.id.total_cases);
        total_recovered = findViewById(R.id.total_recovered);
        total_deaths = findViewById(R.id.total_deaths);
        total_new_cases_today = findViewById(R.id.total_new_cases_today);
        total_new_deaths_today = findViewById(R.id.total_new_deaths_today);
        total_active_cases = findViewById(R.id.total_active_cases);

        title.setText(items.getTitle());
        total_cases.setText(items.getTotal_cases());
        total_recovered.setText(items.getTotal_recovered());
        total_deaths.setText(items.getTotal_deaths());
        total_new_cases_today.setText(items.getTotal_new_case_today());
        total_new_deaths_today.setText(items.getTotal_new_deaths_today());
        total_active_cases.setText(items.getTotal_active_cases());

        LinearLayout btn_state_data = findViewById(R.id.btn_state_data);
        btn_state_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), IndiaStateList.class));
            }
        });
        if(items.getTitle().equalsIgnoreCase("India") && isLocationAvailable() && items.getTitle().equalsIgnoreCase(sharedPref.getString(Constant.user_country, "null"))){
            btn_state_data.setVisibility(View.VISIBLE);
        } else {
            btn_state_data.setVisibility(View.GONE);
        }

        LinearLayout btn_graph = findViewById(R.id.btn_graph);
        btn_graph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), CountryChartViewer.class);
                i.putExtra("data", items);
                startActivity(i);
            }
        });

        TextView flag = findViewById(R.id.flag);
        flag.setText(localeToEmoji(items.getCode()));

        FrameLayout share = findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, getShareText(items));
                startActivity(Intent.createChooser(share, "COVID'19 - Live Data of " + items.getTitle()));
            }
        });

        loadData();
    }

    private void loadData(){
        if(!progressDialog.isShowing()){
            progressDialog.show();
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.DATA_GET_COUNTRY_DATA_ALTERNATE + items.getTitle(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        decodeData(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error", "Get all country");
                        showError("Internet connection problem. Please retry.");
                    }
                }) {
        };

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }

    private void decodeData(final String response){
        new LoadDataTask().execute(response);
    }

    private class LoadDataTask extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... urls) {
            try{
                JSONArray a = new JSONArray(urls[0]);
                int lastCase = 0;
                int lastDeath = 0;
                for(int i = 1; i<a.length(); i++){
                    JSONObject o2 = a.getJSONObject(i);
                    CoronaTimelineItems items = new CoronaTimelineItems();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd");
                    Date date = simpleDateFormat.parse(o2.getString("Date").substring(0, 10));
                    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("dd-mm-yyyy");
                    items.setDate(simpleDateFormat1.format(date));
                    items.setTotal_cases(o2.getString("Confirmed"));
                    items.setTotal_deaths(o2.getString("Deaths"));
                    items.setTotal_recoveries(o2.getString("Recovered"));
                    items.setNew_daily_cases(String.valueOf(o2.getInt("Confirmed") - lastCase));
                    lastCase = o2.getInt("Confirmed");
                    items.setNew_daily_deaths(String.valueOf(o2.getInt("Deaths") - lastDeath));
                    lastDeath = o2.getInt("Deaths");
                    list.add(items);
                }
                Collections.reverse(list);
                mAdapter = new Adapter_Country_Timeline(getApplicationContext(), list);
                return true;
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(Boolean result) {
            if(!result){
                showError("An error occurred! Please retry.");
            } else {
                if(!list.isEmpty()){
                    recyclerView.setAdapter(mAdapter);
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            if(progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                        }
                    });
                } else {
                    showError("An error occurred! Please retry.");
                }
            }
        }
    }

    private void showError(String message){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        finish();
    }



    private String getShareText(TrackerItems i){
        String singleBreak = "\n";
        String multipleBreak = "\n\n";
        String a = "COVID'19 - Live Data of " + i.getTitle() + multipleBreak;
        String b = "Total cases: " + i.getTotal_cases() + singleBreak;
        String c = "Total deaths: " +  i.getTotal_deaths() + singleBreak;
        String d = "Total recovered: " + i.getTotal_recovered() + singleBreak;
        String e = "Total new cases today: " + i.getTotal_new_case_today() + singleBreak;
        String f = "Total new deaths today: " + i.getTotal_new_deaths_today() + multipleBreak;
        String g = "Download COVID'19 App for live tracking of Coronavirus:" + singleBreak + "https://bit.ly/covid19indiasars";

        return a+b+c+d+e+f+g;
    }

    private boolean isLocationAvailable(){
        return sharedPref.getString(Constant.user_country, null) != null;
    }
    private String localeToEmoji(String countryCode) {
        int firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6;
        int secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6;
        return new String(Character.toChars(firstLetter)) + new String(Character.toChars(secondLetter));
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        Locale newLocale;
        SharedPreferences sharedPreferences = newBase.getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);
        newLocale = new Locale(sharedPreferences.getString(Constant.current_language, "en"));
        Context context = ContextWrapper.wrap(newBase, newLocale);
        super.attachBaseContext(context);
    }

}
