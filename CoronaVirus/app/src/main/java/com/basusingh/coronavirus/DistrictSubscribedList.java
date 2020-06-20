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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.basusingh.coronavirus.adapter.Adapter_District_Subscribed;
import com.basusingh.coronavirus.database.districtsubscription.StateDataDatabaseClient;
import com.basusingh.coronavirus.database.districtsubscription.StateDataItems;
import com.basusingh.coronavirus.utils.ContextWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DistrictSubscribedList extends AppCompatActivity {

    LinearLayout error_layout;
    FrameLayout subscribe_btn;
    ProgressDialog progressDialog;
    RecyclerView recyclerView;
    TextView error_text;
    List<StateDataItems> list;
    Adapter_District_Subscribed mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_district_subscribed_list);

        FrameLayout cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FrameLayout btn_add = findViewById(R.id.add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), IndiaStateList.class));
                finish();
            }
        });

        list = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setNestedScrollingEnabled(false);
        mAdapter = new Adapter_District_Subscribed(getApplicationContext(), list);

        progressDialog = new ProgressDialog(DistrictSubscribedList.this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        error_text = findViewById(R.id.error_text);

        error_layout = findViewById(R.id.error_layout);
        subscribe_btn = findViewById(R.id.subscribe_btn);
        subscribe_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), IndiaStateList.class));
                finish();
            }
        });

        error_layout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if(mAdapter.getItemCount() == 0){
                    showError(getResources().getString(R.string.district_subscribed_no_subscription_yet));
                } else {
                    error_layout.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
                super.onChanged();
            }
        });

        loadData();

    }

    private void loadData(){
        if(!progressDialog.isShowing()){
            progressDialog.show();
        }
        new AsyncTask<Void, Void, Boolean>(){
            @Override
            protected Boolean doInBackground(Void... p){
                list = StateDataDatabaseClient.getInstance(getApplicationContext()).getStateDataAppDatabase().stateDataDao().getAll();
                if(list.isEmpty()){
                    return false;
                } else {
                    mAdapter = new Adapter_District_Subscribed(getApplicationContext(), list);
                    return true;
                }
            }
            @Override
            protected void onPostExecute(Boolean result){
                if(progressDialog.isShowing()){
                    progressDialog.dismiss();
                }
                if(!result){
                    showError(getResources().getString(R.string.district_subscribed_no_subscription_yet));
                } else {
                    error_layout.setVisibility(View.GONE);
                    recyclerView.setAdapter(mAdapter);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }

    private void showError(String message){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        recyclerView.setVisibility(View.GONE);
        error_text.setText(message);
        error_layout.setVisibility(View.VISIBLE);
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
