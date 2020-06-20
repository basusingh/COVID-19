package com.basusingh.coronavirus;

import android.Manifest;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.basusingh.coronavirus.utils.ContextWrapper;
import com.basusingh.coronavirus.utils.LiveDataForegroundService;

import java.io.File;
import java.util.Locale;

public class AppBlockedFromUsage extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    DownloadManager downloadManager;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_blocked_from_usage);

        sharedPreferences = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);

        stopLiveTrackingService();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Downloading");
        progressDialog.setCanceledOnTouchOutside(false);

        TextView title = findViewById(R.id.title);
        title.setText(sharedPreferences.getString(Constant.app_pref_app_blocked_title, "New App Update Available"));

        TextView message = findViewById(R.id.message);
        message.setText(sharedPreferences.getString(Constant.app_pref_app_blocked_message, "Please download the new version of the app by clicking the download button below."));

        FrameLayout btn_download = findViewById(R.id.btn_download);
        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
    }

    private void startDownload(){
        Uri app_uri = Uri.parse("http://coronadisease.azurewebsites.net/apprelease/covid'19.apk");

    }


    private long DownloadData (Uri uri, View v) {

        long downloadReference;
        downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setTitle("Download Covid'19");

        request.setDescription("This will only take a moment");

        request.setDestinationInExternalFilesDir(AppBlockedFromUsage.this,
                Environment.DIRECTORY_DOWNLOADS,"covid'19.apk");

        downloadReference = downloadManager.enqueue(request);

        return downloadReference;
    }

    private void installUpdate(String path){
        Intent intent_install = new Intent(Intent.ACTION_VIEW);
        intent_install.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
        startActivity(intent_install);
        Toast.makeText(getApplicationContext(), "Installing app", Toast.LENGTH_LONG).show();
    }


    String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }



    private void stopLiveTrackingService(){
        if(isMyServiceRunning(LiveDataForegroundService.class)){
            final SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constant.app_pref_status_live_tracking, false);
            editor.putBoolean(Constant.app_pref_status_district_tracking, false);
            editor.apply();
            stopLiveTracking();
        }
    }

    private void stopLiveTracking(){
        Intent myService = new Intent(AppBlockedFromUsage.this, LiveDataForegroundService.class);
        stopService(myService);
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


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
