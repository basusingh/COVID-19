package com.basusingh.coronavirus.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.basusingh.coronavirus.Constant;
import com.basusingh.coronavirus.R;
import com.basusingh.coronavirus.database.districtsubscription.StateDataDao;
import com.basusingh.coronavirus.database.districtsubscription.StateDataDatabaseClient;
import com.basusingh.coronavirus.database.districtsubscription.StateDataItems;
import com.basusingh.coronavirus.utils.LiveDataForegroundService;

import java.util.ArrayList;
import java.util.List;

public class Adapter_state_data extends RecyclerView.Adapter<Adapter_state_data.ViewHolder> {

    private Context context;
    private List<StateDataItems> list;
    SharedPreferences sharedPref;

    public Adapter_state_data(Context mContext, List<StateDataItems> mItems){
        this.context = mContext;
        this.list = mItems;
        sharedPref = mContext.getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView title, active, confirmed, deceased, recovered;
        LinearLayout share_data, subscribe;
        public ViewHolder(View v){
            super(v);
            title = v.findViewById(R.id.title);
            active = v.findViewById(R.id.active);
            confirmed = v.findViewById(R.id.confirmed);
            deceased = v.findViewById(R.id.deceased);
            recovered = v.findViewById(R.id.recovered);
            share_data = v.findViewById(R.id.btn_share_data);
            subscribe = v.findViewById(R.id.btn_subscribe);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position){
        View v = LayoutInflater.from(context).inflate(R.layout.item_state_data, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int position){
        final StateDataItems i = list.get(position);
        viewHolder.title.setText(i.getDistrictName());
        viewHolder.active.setText(i.getActive());
        viewHolder.confirmed.setText(i.getConfirmed());
        viewHolder.deceased.setText(i.getDeceased());
        viewHolder.recovered.setText(i.getRecovered());
        viewHolder.share_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent share = new Intent(android.content.Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, getShareText(i));
                share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(Intent.createChooser(share, "COVID'19 - Live Data of " + i.getDistrictName() + ", " + i.getStateName()));
            }
        });

        viewHolder.subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLocationNotEnabled()){
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Please enable location and restart app.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                            intent.setData(uri);
                            context.startActivity(intent);
                        }
                    });
                } else {
                    addData(i);
                }
            }
        });
    }

    private void addData(final StateDataItems i){
        new AsyncTask<Void, Integer, Integer>(){
            @Override
            protected Integer doInBackground(Void... v){
                int result = 0;
                if(!sharedPref.getBoolean(Constant.app_pref_status_district_tracking, false)){
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(Constant.app_pref_status_district_tracking, true);
                    editor.apply();
                }
                if(StateDataDatabaseClient.getInstance(context).getStateDataAppDatabase().stateDataDao().checkIfExist(i.getDistrictName(), i.getStateName()) == null){
                    if(StateDataDatabaseClient.getInstance(context).getStateDataAppDatabase().stateDataDao().getCount() >= 5){
                        result = 2;
                    } else {
                        Log.e("Added", "Added----------");
                        StateDataDatabaseClient.getInstance(context).getStateDataAppDatabase().stateDataDao().insert(i);
                    }
                } else {
                    Log.e("Exist", "Exist----------");
                    result = 1;
                }
                return result;
            }
            @Override
            public void onPostExecute(Integer val){
                if(val == 1){
                    Toast.makeText(context, "Already subscribed", Toast.LENGTH_SHORT).show();
                    startLiveTracking();
                } else if(val == 0){
                    Toast.makeText(context, "Subscribed", Toast.LENGTH_SHORT).show();
                    startLiveTracking();
                } else if(val == 2){
                    Toast.makeText(context, "Maximum number of district already subscribed. Please unsubscribe from one.", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }


    private void startLiveTracking(){
        Intent serviceIntent = new Intent(context, LiveDataForegroundService.class);
        serviceIntent.putExtra("type", 1);
        ContextCompat.startForegroundService(context, serviceIntent);
    }

    private boolean isLocationNotEnabled(){
        return sharedPref.getString(Constant.user_country, null) == null;
    }

    private String getShareText(StateDataItems i){
        String singleBreak = "\n";
        String multipleBreak = "\n\n";
        String a = "COVID'19 - Live Data of " + i.getDistrictName() + ", " + i.getStateName() + ":" + multipleBreak;
        String b = "Total active: " + i.getActive() + singleBreak;
        String c = "Total confirmed: " +  i.getConfirmed() + singleBreak;
        String d = "Total deaths: " + i.getDeceased() + singleBreak;
        String e = "Total recovered: " + i.getRecovered() + multipleBreak;
        String f = "Download COVID'19 App for live tracking of Coronavirus:" + singleBreak + "https://bit.ly/covid19indiasars";

        return a+b+c+d+e+f;
    }



    @Override
    public int getItemCount(){
        return list.size();
    }
}


