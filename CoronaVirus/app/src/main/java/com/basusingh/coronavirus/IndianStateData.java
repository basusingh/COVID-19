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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.basusingh.coronavirus.adapter.Adapter_State;
import com.basusingh.coronavirus.adapter.Adapter_state_data;
import com.basusingh.coronavirus.database.districtsubscription.StateDataItems;
import com.basusingh.coronavirus.utils.ContextWrapper;
import com.basusingh.coronavirus.utils.listsort.StateDataListSort;
import com.basusingh.coronavirus.utils.listsort.StateDataSort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class IndianStateData extends AppCompatActivity {

    String state;
    List<StateDataItems> list, finalList;
    RecyclerView recyclerView;
    ProgressDialog progressDialog;
    Adapter_state_data mAdapter;

    int sortType = R.id.sort_name;
    int sortOrder = R.id.sort_ascending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indian_state_data);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("bundle");
        try{
            list = (List<StateDataItems>) args.getSerializable("data");
            state = intent.getStringExtra("state");
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        TextView title = findViewById(R.id.title);
        title.setText(state);

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

        finalList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setNestedScrollingEnabled(false);

        progressDialog = new ProgressDialog(IndianStateData.this);
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
        new AsyncTask<Void, Boolean, Boolean>(){
            @Override
            protected Boolean doInBackground(Void... a){
                for(int i = 0; i<list.size(); i++) {
                    StateDataItems stateDataItems = list.get(i);
                    if (stateDataItems.getStateName().equalsIgnoreCase(state)) {
                        finalList.add(stateDataItems);
                    }
                }
                Collections.sort(finalList, new StateDataSort("name", 0));
                return true;
            }

            @Override
            public void onPostExecute(Boolean result){
                finaliseView(result);
            }
        }.execute();
    }

    private void finaliseView(Boolean result){
        if(!result){
            showError("An error occurred! Please retry.");
        } else {
            if(!finalList.isEmpty()){
                mAdapter = new Adapter_state_data(IndianStateData.this, finalList);
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


    private void showSortingDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort");
        final View customLayout = getLayoutInflater().inflate(R.layout.custom_sort_state_data, null);
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
                        case R.id.sort_active:
                            showOrderDialog("active");
                            break;
                        case R.id.sort_confirmed:
                            showOrderDialog("confirmed");
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
        if(finalList.isEmpty()){
            showError("No data available! Please retry.");
            return;
        }
        Collections.sort(finalList, new StateDataSort(type, order));
        mAdapter = new Adapter_state_data(IndianStateData.this, finalList);
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
