package com.basusingh.coronavirus.adapter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.basusingh.coronavirus.Constant;
import com.basusingh.coronavirus.R;
import com.basusingh.coronavirus.database.districtsubscription.StateDataDatabaseClient;
import com.basusingh.coronavirus.database.districtsubscription.StateDataItems;
import com.basusingh.coronavirus.utils.DetectConnection;
import com.basusingh.coronavirus.utils.LiveDataForegroundService;
import com.basusingh.coronavirus.utils.LocationService;

import java.util.List;

public class Adapter_District_Subscribed extends RecyclerView.Adapter<Adapter_District_Subscribed.ViewHolder> {

    private Context context;
    private List<StateDataItems> list;
    SharedPreferences sharedPreferences;

    public Adapter_District_Subscribed(Context mContext, List<StateDataItems> mItems){
        this.context = mContext;
        this.list = mItems;
        sharedPreferences = context.getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView state, district;
        FrameLayout delete;
        public ViewHolder(View v){
            super(v);
            state = v.findViewById(R.id.state);
            district = v.findViewById(R.id.district);
            delete = v.findViewById(R.id.delete);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position){
        View v = LayoutInflater.from(context).inflate(R.layout.item_subscribed_district, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position){
        final StateDataItems i = list.get(position);
        viewHolder.state.setText(i.getStateName());
        viewHolder.district.setText(i.getDistrictName());
        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFromDatabase(i, position);
            }
        });
    }

    private void deleteFromDatabase(final StateDataItems i, final int position){
        new AsyncTask<Void, Void, Boolean>(){
            @Override
            protected Boolean doInBackground(Void... v){
                StateDataDatabaseClient.getInstance(context).getStateDataAppDatabase().stateDataDao().delete(i);
                if(StateDataDatabaseClient.getInstance(context).getStateDataAppDatabase().stateDataDao().getCount() == 0){
                    final SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.app_pref, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(Constant.app_pref_status_district_tracking, false);
                    editor.apply();
                    return true;
                } else {
                    return false;
                }
            }
            @Override
            public void onPostExecute(Boolean a){
                list.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, list.size());
                Toast.makeText(context, "Removed", Toast.LENGTH_SHORT).show();
                if(a){
                    if(DetectConnection.checkInternetConnection(context) && isLocationEnabled(context) && !isLocationNotAvailable() && hasPermissions(context, PERMISSIONS)){
                        Intent serviceIntent = new Intent(context, LiveDataForegroundService.class);
                        serviceIntent.putExtra("type", 0);
                        ContextCompat.startForegroundService(context, serviceIntent);
                    }
                }
            }
        }.execute();
    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    private boolean isLocationNotAvailable(){
        return sharedPreferences.getString(Constant.user_country, null) == null;
    }


    String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

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
}

