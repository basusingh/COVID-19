package com.basusingh.coronavirus.utils;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.basusingh.coronavirus.Constant;
import com.basusingh.coronavirus.MainActivity;

public class NotificationCancelReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int notificationId = intent.getIntExtra("notificationId", 0);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        try{
            manager.cancel(notificationId);
            final SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.app_pref, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constant.app_pref_status_live_tracking, false);
            editor.apply();
            Intent stopIntent = new Intent(context, LiveDataForegroundService.class);
            context.startService(stopIntent);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

