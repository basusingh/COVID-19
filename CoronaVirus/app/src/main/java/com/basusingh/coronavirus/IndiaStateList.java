package com.basusingh.coronavirus;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basusingh.coronavirus.adapter.Adapter_State;
import com.basusingh.coronavirus.database.districtsubscription.StateDataItems;
import com.basusingh.coronavirus.utils.ContextWrapper;
import com.basusingh.coronavirus.utils.NetworkSingleton;
import com.basusingh.coronavirus.utils.RecyclerTouchListener;
import com.basusingh.coronavirus.utils.StateDataList;
import com.basusingh.coronavirus.utils.listsort.StateDataListSort;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class IndiaStateList extends AppCompatActivity {

    RecyclerView recyclerView;
    List<StateDataList> stateList = new ArrayList<>();
    List<StateDataItems> list = new ArrayList<>();
    Adapter_State mAdapter;
    ProgressDialog progressDialog;

    int sortType = R.id.sort_name;
    int sortOrder = R.id.sort_ascending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_india_state_list);
        FrameLayout cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FrameLayout view_subscribed_list = findViewById(R.id.view_subscribed);
        view_subscribed_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), DistrictSubscribedList.class));
            }
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(),
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                final StateDataList item = stateList.get(position);
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(getApplicationContext(), IndianStateData.class);
                        i.putExtra("state", item.getState());
                        Bundle args = new Bundle();
                        args.putSerializable("data", (Serializable)list);
                        i.putExtra("bundle", args);
                        startActivity(i);
                    }
                });
            }

            @Override
            public void onLongClick(View view, int position) {
                //TODO
            }
        }));

        progressDialog = new ProgressDialog(IndiaStateList.this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadData();

        FrameLayout sort_data = findViewById(R.id.sort);
        sort_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortingDialog();
            }
        });
    }


    private void loadData(){
        if(!progressDialog.isShowing()){
            progressDialog.show();
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.DATA_GET_INDIA_STATE,
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
            Log.e("Starting decode", "NOW--------");
            try{
                JSONObject o = new JSONObject(urls[0]);
                Iterator<String> keys = o.keys();
                for(int i = 0; i<o.length(); i++){
                    String str_Name = keys.next();
                    JSONObject o2 = o.getJSONObject(str_Name);
                    JSONObject o3 = o2.getJSONObject("districtData");
                    Iterator<String> keys1 = o3.keys();
                    int totalCase = 0, totalDeaths = 0, totalRecovered = 0;
                    for(int j = 0; j<o3.length(); j++){
                        String dtc_Name = keys1.next();
                        JSONObject o4 = o3.getJSONObject(dtc_Name);
                        StateDataItems stateDataItems = new StateDataItems();
                        stateDataItems.setStateName(str_Name);
                        stateDataItems.setDistrictName(dtc_Name);
                        stateDataItems.setStateCode(o2.getString("statecode"));
                        stateDataItems.setActive(o4.getString("active"));
                        stateDataItems.setConfirmed(o4.getString("confirmed"));
                        totalCase = totalCase + o4.getInt("confirmed");
                        stateDataItems.setDeceased(o4.getString("deceased"));
                        totalDeaths = totalDeaths + o4.getInt("deceased");
                        stateDataItems.setRecovered(o4.getString("recovered"));
                        totalRecovered = totalRecovered + o4.getInt("recovered");
                        list.add(stateDataItems);
                    }
                    StateDataList stateDataItems = new StateDataList();
                    stateDataItems.setState(str_Name);
                    stateDataItems.setTotalCase(String.valueOf(totalCase));
                    stateDataItems.setTotalDeaths(String.valueOf(totalDeaths));
                    stateDataItems.setTotalRecovered(String.valueOf(totalRecovered));
                    stateList.add(stateDataItems);
                }
                Collections.sort(stateList, new StateDataListSort("name", 0));
                mAdapter = new Adapter_State(getApplicationContext(), stateList);
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
                if(!stateList.isEmpty()){
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
                    showError("No data available! Please retry.");
                }
            }
        }
    }


    private void showSortingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort");
        final View customLayout = getLayoutInflater().inflate(R.layout.custom_sort_state_list, null);
        builder.setView(customLayout);
        final RadioGroup radioGroup1 = customLayout.findViewById(R.id.radioGroup1);
        final RadioButton sortTypeButton = customLayout.findViewById(sortType);
        sortTypeButton.setChecked(true);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(radioGroup1.getCheckedRadioButtonId() == -1){
                    Toast.makeText(getApplicationContext(), "Please select at least one criteria", Toast.LENGTH_LONG).show();
                    showSortingDialog();
                } else {
                    sortType = radioGroup1.getCheckedRadioButtonId();
                    switch (radioGroup1.getCheckedRadioButtonId()){
                        case R.id.sort_name:
                            showOrderDialog("name");
                            break;
                        case R.id.sort_case:
                            showOrderDialog("cases");
                            break;
                        case R.id.sort_deaths:
                            showOrderDialog("deaths");
                            break;
                        case R.id.sort_recovered:
                            showOrderDialog("recovered");
                            break;
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showOrderDialog(final String type){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose order");
        final View customLayout = getLayoutInflater().inflate(R.layout.custom_sort_order, null);
        builder.setView(customLayout);
        RadioButton btnOrder = customLayout.findViewById(sortOrder);
        btnOrder.setChecked(true);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final RadioGroup radioGroup2 = customLayout.findViewById(R.id.radioGroup2);
                if(radioGroup2.getCheckedRadioButtonId() == -1){
                    Toast.makeText(getApplicationContext(), "Please select order", Toast.LENGTH_LONG).show();
                    showOrderDialog(type);
                } else {
                    sortOrder = radioGroup2.getCheckedRadioButtonId();
                    switch (radioGroup2.getCheckedRadioButtonId()){
                        case R.id.sort_ascending:
                            sortData(type, 0);
                            break;
                        case R.id.sort_descending:
                            sortData(type, 1);
                            break;
                    }
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sortData(String type, int order){
        if(stateList.isEmpty()){
            showError("No data available! Please retry.");
            return;
        }
        Collections.sort(stateList, new StateDataListSort(type, order));
        mAdapter = new Adapter_State(getApplicationContext(), stateList);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                recyclerView.setAdapter(mAdapter);
            }
        });
    }

    private void showError(String message){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        finish();
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
