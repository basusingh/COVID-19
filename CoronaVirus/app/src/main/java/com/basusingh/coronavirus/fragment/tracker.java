package com.basusingh.coronavirus.fragment;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import com.basusingh.coronavirus.Constant;
import com.basusingh.coronavirus.CountryTimelineViewer;
import com.basusingh.coronavirus.R;
import com.basusingh.coronavirus.adapter.Adapter_Tracker_Country;
import com.basusingh.coronavirus.database.tracker.TrackerDatabaseClient;
import com.basusingh.coronavirus.database.tracker.TrackerItems;
import com.basusingh.coronavirus.utils.DetectConnection;
import com.basusingh.coronavirus.utils.LiveDataForegroundService;
import com.basusingh.coronavirus.utils.NetworkSingleton;
import com.basusingh.coronavirus.utils.RecyclerTouchListener;
import com.basusingh.coronavirus.utils.StateDataList;
import com.basusingh.coronavirus.utils.listsort.CountryItemSort;
import com.reddit.indicatorfastscroll.FastScrollItemIndicator;
import com.reddit.indicatorfastscroll.FastScrollerThumbView;
import com.reddit.indicatorfastscroll.FastScrollerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import kotlin.jvm.functions.Function1;

public class tracker extends Fragment {

    public tracker() {
        // Required empty public constructor
    }

    SharedPreferences sharedPref;
    private Adapter_Tracker_Country mAdapter;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    int mTotal_case = 0, mTotal_death = 0, mTotal_recovered = 0;
    private TextView total_cases, total_deaths, total_recovered;
    private LinearLayout mainLayout;
    CardView btn_reloadLayout;
    boolean isLocationPresent = false;
    List<TrackerItems> list;
    boolean done = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tracker, container, false);
    }

    @Override
    public void onViewCreated(View v ,Bundle s){
        list = new ArrayList<>();
        progressBar = v.findViewById(R.id.progressBar);
        recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(),
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                final TrackerItems i = list.get(position);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        final Intent intent = new Intent(getContext(), CountryTimelineViewer.class);
                        intent.putExtra("data", i);
                        intent.putExtra("totalCase", mTotal_case);
                        intent.putExtra("totalDeath", mTotal_death);
                        intent.putExtra("totalRecovered", mTotal_recovered);
                        startActivity(intent);
                    }
                });
            }
            @Override
            public void onLongClick(View view, int position) {
                //TODO
            }
        }));

        FastScrollerView fastScrollerView = v.findViewById(R.id.fastscroller);
        fastScrollerView.setupWithRecyclerView(
                recyclerView,
                new Function1<Integer, FastScrollItemIndicator>() {
                    @Override
                    public FastScrollItemIndicator invoke(Integer position) {
                        if(position == 0 && isLocationPresent && done){
                            return new FastScrollItemIndicator.Text("A");
                        } else {
                            TrackerItems item = list.get(position);
                            return new FastScrollItemIndicator.Text(
                                    item.getTitle().substring(0, 1).toUpperCase()
                            );
                        }
                    }
                }
        );

        fastScrollerView.getItemIndicatorSelectedCallbacks().add(
                new FastScrollerView.ItemIndicatorSelectedCallback() {
                    @Override
                    public void onItemIndicatorSelected(@NotNull FastScrollItemIndicator indicator, int indicatorCenterY, int itemPosition) {
                        recyclerView.scrollToPosition(itemPosition);
                    }
                }
        );

        FastScrollerThumbView fastScrollerThumbView = v.findViewById(R.id.fastscroller_thumb);
        fastScrollerThumbView.setupWithFastScroller(fastScrollerView);

        total_cases = v.findViewById(R.id.total_cases);
        total_deaths = v.findViewById(R.id.total_deaths);
        total_recovered = v.findViewById(R.id.total_recovered);

        mainLayout = v.findViewById(R.id.mainLayout);
        btn_reloadLayout = v.findViewById(R.id.btn_reloadLayout);

        sharedPref = getActivity().getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);
        if(sharedPref.getString(Constant.user_country, null) != null){
            isLocationPresent = true;
        }

        FrameLayout btn_reload = v.findViewById(R.id.btn_reload);
        btn_reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProcess();
            }
        });
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                startProcess();
            }
        });
    }

    private void startProcess(){
        mainLayout.setVisibility(View.GONE);
        btn_reloadLayout.setVisibility(View.GONE);
        if(progressBar.getVisibility() == View.GONE){
            progressBar.setVisibility(View.VISIBLE);
        }
        if(!DetectConnection.checkInternetConnection(getActivity())){
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    new LoadOfflineData().execute();
                }
            });
        } else {
            loadAlternateData();
        }
    }


    private void loadAlternateData(){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.DATA_GET_ALL_COUNTRY_ALTERNATE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        decodeDataAlternate(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                new LoadOfflineData().execute();
                            }
                        });
                    }
                }) {
        };

        NetworkSingleton.getInstance(getActivity()).addToRequestQueue(stringRequest);
    }


    private void decodeDataAlternate(final String response){
        new LoadDataTaskAlternate().execute(response);
    }

    private class LoadDataTaskAlternate extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... urls) {
            try{
                JSONObject o = new JSONObject(urls[0]);
                JSONArray a = o.getJSONArray("Countries");
                for(int i = 1; i<a.length(); i++){
                    JSONObject o2 = a.getJSONObject(i);
                    TrackerItems items = new TrackerItems();
                    items.setOurid(String.valueOf(i));
                    items.setTitle(o2.getString("Country"));
                    items.setCode(o2.getString("CountryCode"));
                    items.setTotal_cases(o2.getString("TotalConfirmed"));
                    items.setTotal_recovered(o2.getString("TotalRecovered"));
                    items.setTotal_deaths(o2.getString("TotalDeaths"));
                    items.setTotal_new_case_today(o2.getString("NewConfirmed"));
                    items.setTotal_new_deaths_today(o2.getString("NewDeaths"));
                    items.setTotal_active_cases(String.valueOf(o2.getInt("TotalConfirmed") - o2.getInt("TotalRecovered")));
                    if(isLocationPresent && !done){
                        if(o2.getString("Country").equalsIgnoreCase(sharedPref.getString(Constant.user_country, "null")) || o2.getString("CountryCode").equalsIgnoreCase(sharedPref.getString(Constant.user_country_code, "null"))){
                            list.add(0, items);
                            done = true;
                        } else {
                            list.add(items);
                        }
                    } else {
                        list.add(items);
                    }

                    mTotal_case = mTotal_case + Integer.parseInt(items.getTotal_cases());
                    mTotal_death = mTotal_death + Integer.parseInt(items.getTotal_deaths());
                    mTotal_recovered = mTotal_recovered + Integer.parseInt(items.getTotal_recovered());
                }
                if(isLocationPresent && done){
                    TrackerItems countryItems = list.get(0);
                    list.remove(0);
                    Collections.sort(list, new CountryItemSort());
                    list.add(0, countryItems);
                } else {
                    Collections.sort(list, new CountryItemSort());
                }
                try{
                    TrackerDatabaseClient.getInstance(getActivity()).getTrackerAppDatabase().trackerDao().deleteAll();
                } catch (Exception e){
                    e.printStackTrace();
                }
                try{
                    TrackerDatabaseClient.getInstance(getActivity()).getTrackerAppDatabase().trackerDao().insertAll(list);
                } catch (Exception e){
                    e.printStackTrace();
                }
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(Constant.TOTAL_CASES, mTotal_case);
                editor.putInt(Constant.TOTAL_DEATHS, mTotal_death);
                editor.putInt(Constant.TOTAL_RECOVERED, mTotal_recovered);
                editor.apply();
                mAdapter = new Adapter_Tracker_Country(getActivity(), list);
                return true;
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(Boolean result) {
            if(!result){
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        new LoadOfflineData().execute();
                    }
                });
            } else {
                if(!list.isEmpty()){
                    if(progressBar.getVisibility() == View.VISIBLE){
                        progressBar.setVisibility(View.GONE);
                    }
                    btn_reloadLayout.setVisibility(View.GONE);
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.setAdapter(mAdapter);
                            mainLayout.setVisibility(View.VISIBLE);
                            ValueAnimator animator1 = ValueAnimator.ofInt(0, mTotal_case);
                            animator1.setDuration(1000);
                            animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    total_cases.setText(animation.getAnimatedValue().toString());
                                }
                            });
                            animator1.start();

                            ValueAnimator animator2 = ValueAnimator.ofInt(0, mTotal_death);
                            animator2.setDuration(1000);
                            animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    total_deaths.setText(animation.getAnimatedValue().toString());
                                }
                            });
                            animator2.start();

                            ValueAnimator animator3 = ValueAnimator.ofInt(0, mTotal_recovered);
                            animator3.setDuration(1000);
                            animator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    total_recovered.setText(animation.getAnimatedValue().toString());
                                }
                            });
                            animator3.start();
                            startLiveTrackingService();
                        }
                    });
                } else {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            new LoadOfflineData().execute();
                        }
                    });
                }
            }
        }
    }


    private void startLiveTrackingService(){
        if(sharedPref.getBoolean(Constant.app_pref_status_live_tracking, false) || sharedPref.getBoolean(Constant.app_pref_status_district_tracking, false)){
            try{
                if(DetectConnection.checkInternetConnection(getContext()) && isLocationEnabled(getContext()) && hasPermissions(getActivity(), PERMISSIONS) && !isLocationNotAvailable()){
                    startLiveTracking();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
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

    private void startLiveTracking(){
        Intent serviceIntent = new Intent(getContext(), LiveDataForegroundService.class);
        serviceIntent.putExtra("type", 0);
        ContextCompat.startForegroundService(requireContext(), serviceIntent);
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

    private class LoadOfflineData extends AsyncTask<Void, Void, Boolean> {
        protected Boolean doInBackground(Void... urls) {
            try{
                list = TrackerDatabaseClient.getInstance(getActivity()).getTrackerAppDatabase().trackerDao().getAll();
                if(list.isEmpty()){
                    return false;
                } else {
                    final SharedPreferences sharedPref = getActivity().getSharedPreferences(
                            Constant.app_pref, Context.MODE_PRIVATE);
                    mTotal_case = sharedPref.getInt(Constant.TOTAL_CASES, 0);
                    mTotal_death = sharedPref.getInt(Constant.TOTAL_DEATHS, 0);
                    mTotal_recovered = sharedPref.getInt(Constant.TOTAL_RECOVERED, 0);
                    mAdapter = new Adapter_Tracker_Country(getActivity(), list);
                    return true;
                }
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        protected void onPostExecute(Boolean result) {
            if(!result){
                showFinalError("No data available. Please check your internet connection and try again later");
            } else {
                if(progressBar.getVisibility() == View.VISIBLE){
                    progressBar.setVisibility(View.GONE);
                }
                //showToast("Showing offline data", Toast.LENGTH_LONG);
                btn_reloadLayout.setVisibility(View.GONE);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setAdapter(mAdapter);
                        mainLayout.setVisibility(View.VISIBLE);
                        ValueAnimator animator1 = ValueAnimator.ofInt(0, mTotal_case);
                        animator1.setDuration(1000);
                        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            public void onAnimationUpdate(ValueAnimator animation) {
                                total_cases.setText(animation.getAnimatedValue().toString());
                            }
                        });
                        animator1.start();

                        ValueAnimator animator2 = ValueAnimator.ofInt(0, mTotal_death);
                        animator2.setDuration(1000);
                        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            public void onAnimationUpdate(ValueAnimator animation) {
                                total_deaths.setText(animation.getAnimatedValue().toString());
                            }
                        });
                        animator2.start();

                        ValueAnimator animator3 = ValueAnimator.ofInt(0, mTotal_recovered);
                        animator3.setDuration(1000);
                        animator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            public void onAnimationUpdate(ValueAnimator animation) {
                                total_recovered.setText(animation.getAnimatedValue().toString());
                            }
                        });
                        animator3.start();
                    }
                });
            }
        }
    }


    private void showFinalError(String message){
        if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.GONE);
        }
        mainLayout.setVisibility(View.GONE);
        btn_reloadLayout.setVisibility(View.VISIBLE);
        showToast(message, Toast.LENGTH_SHORT);
    }

    private void showToast(String message, int length){
        try{
            Toast.makeText(getActivity(), message, length).show();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
