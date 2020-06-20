package com.basusingh.coronavirus;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.basusingh.coronavirus.utils.ContextWrapper;
import com.basusingh.coronavirus.utils.DetectConnection;
import com.basusingh.coronavirus.utils.LiveDataForegroundService;
import com.basusingh.coronavirus.utils.LocationService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.processphoenix.ProcessPhoenix;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Locale;

public class Settings extends AppCompatActivity {

    Locale myLocale;
    SharedPreferences sharedPreferences;
    String currentLanguage;
    RadioGroup radioGroup;
    LinearLayout btn_apply;
    RadioButton english, hindi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        radioGroup = findViewById(R.id.radioGroup);
        english = findViewById(R.id.english);
        hindi = findViewById(R.id.hindi);
        btn_apply = findViewById(R.id.btn_apply);

        sharedPreferences = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);
        currentLanguage = sharedPreferences.getString(Constant.current_language, "en");
        switch (currentLanguage){
            case "en":
                english.setChecked(true);
                hindi.setChecked(false);
                break;
            case "hi":
                english.setChecked(false);
                hindi.setChecked(true);
                break;
        }

        btn_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(english.isChecked()){
                    setLocale("en");
                } else if(hindi.isChecked()){
                    setLocale("hi");
                }
            }
        });

        setUpSwitchClick();
        setUpTrackingFeature();
    }

    private void setUpTrackingFeature(){

        RelativeLayout live_district_tracking = findViewById(R.id.layout_live_district_tracking);
        live_district_tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), DistrictSubscribedList.class));
            }
        });

        if(sharedPreferences.getString(Constant.user_country, null) != null && sharedPreferences.getString(Constant.user_country, "null").equalsIgnoreCase("India")){
            live_district_tracking.setVisibility(View.VISIBLE);
        } else {
            live_district_tracking.setVisibility(View.GONE);
        }

        final SwitchCompat switch_live_track = findViewById(R.id.switch_live_track);
        switch_live_track.setChecked(sharedPreferences.getBoolean(Constant.app_pref_status_live_tracking, false));
        switch_live_track.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(!DetectConnection.checkInternetConnection(getApplicationContext())){
                        Toast.makeText(getApplicationContext(), "Internet connection problem. Please try later.", Toast.LENGTH_SHORT).show();
                        switch_live_track.setChecked(false);
                    } else if(!isLocationEnabled(getApplicationContext())){
                        switch_live_track.setChecked(false);
                        Toast.makeText(getApplicationContext(), "Please enable location and restart app.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }  else if(!hasPermissions(getApplicationContext(), PERMISSIONS)){
                        switch_live_track.setChecked(false);
                        Toast.makeText(getApplicationContext(), "Please give location permission and restart app.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    } else if(isLocationNotAvailable()){
                        switch_live_track.setChecked(false);
                        openLocationService();
                        Toast.makeText(getApplicationContext(), "Location not available. Please try later.", Toast.LENGTH_LONG).show();
                    } else {
                        final SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean(Constant.app_pref_status_live_tracking, isChecked);
                        editor.putBoolean(Constant.app_pref_can_show_live_tracking_feature, false);
                        editor.apply();
                        startLiveTracking();
                    }
                } else {
                    final SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(Constant.app_pref_status_live_tracking, isChecked);
                    editor.apply();
                    stopLiveTracking();
                }
            }
        });
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

    private boolean canShowNotification(){
        return NotificationManagerCompat.from(getApplicationContext()).areNotificationsEnabled();
    }

    private boolean isLocationNotAvailable(){
        return sharedPreferences.getString(Constant.user_country, null) == null;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    private void openLocationService(){
        if(isLocationEnabled(getApplicationContext())){
            startService(new Intent(getApplicationContext(), LocationService.class));
        }
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
    private void startLiveTracking(){
        Intent serviceIntent = new Intent(Settings.this, LiveDataForegroundService.class);
        serviceIntent.putExtra("type", 0);
        ContextCompat.startForegroundService(Settings.this, serviceIntent);
    }

    private void stopLiveTracking(){
        Intent myService = new Intent(Settings.this, LiveDataForegroundService.class);
        startService(myService);
    }

    private void setUpSwitchClick(){
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        final SwitchCompat app_update = findViewById(R.id.app_update);
        app_update.setChecked(sharedPreferences.getBoolean(Constant.NOTIFICATION_APP_UPDATE, true));
        app_update.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(canShowNotification()){
                        editor.putBoolean(Constant.NOTIFICATION_APP_UPDATE, isChecked);
                        editor.apply();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please enable notification", Toast.LENGTH_SHORT).show();
                        app_update.setChecked(false);
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                } else {
                    editor.putBoolean(Constant.NOTIFICATION_APP_UPDATE, isChecked);
                    editor.apply();
                }
            }
        });

        final SwitchCompat in_focus = findViewById(R.id.in_focus);
        in_focus.setChecked(sharedPreferences.getBoolean(Constant.NOTIFICATION_IN_FOCUS, true));
        in_focus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(canShowNotification()){
                        editor.putBoolean(Constant.NOTIFICATION_IN_FOCUS, isChecked);
                        editor.apply();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please enable notification", Toast.LENGTH_SHORT).show();
                        in_focus.setChecked(false);
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                } else {
                    editor.putBoolean(Constant.NOTIFICATION_IN_FOCUS, isChecked);
                    editor.apply();
                }
            }
        });

        final SwitchCompat social_list = findViewById(R.id.social_list);
        social_list.setChecked(sharedPreferences.getBoolean(Constant.NOTIFICATION_SOCIAL_DISTANCING_LINK, true));
        social_list.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(canShowNotification()){
                        editor.putBoolean(Constant.NOTIFICATION_SOCIAL_DISTANCING_LINK, isChecked);
                        editor.apply();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please enable notification", Toast.LENGTH_SHORT).show();
                        social_list.setChecked(false);
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                } else {
                    editor.putBoolean(Constant.NOTIFICATION_SOCIAL_DISTANCING_LINK, isChecked);
                    editor.apply();
                }
            }
        });

        final SwitchCompat social_news = findViewById(R.id.social_news);
        social_news.setChecked(sharedPreferences.getBoolean(Constant.NOTIFICATION_SOCIAL_DISTANCING_NEWS, true));
        social_news.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(canShowNotification()){
                        editor.putBoolean(Constant.NOTIFICATION_SOCIAL_DISTANCING_NEWS, isChecked);
                        editor.apply();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please enable notification", Toast.LENGTH_SHORT).show();
                        social_news.setChecked(false);
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                } else {
                    editor.putBoolean(Constant.NOTIFICATION_SOCIAL_DISTANCING_NEWS, isChecked);
                    editor.apply();
                }
            }
        });

        final SwitchCompat helpline = findViewById(R.id.helpline);
        helpline.setChecked(sharedPreferences.getBoolean(Constant.NOTIFICATION_HELPLINE_NO, true));
        helpline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(canShowNotification()){
                        editor.putBoolean(Constant.NOTIFICATION_HELPLINE_NO, isChecked);
                        editor.apply();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please enable notification", Toast.LENGTH_SHORT).show();
                        helpline.setChecked(false);
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                } else {
                    editor.putBoolean(Constant.NOTIFICATION_HELPLINE_NO, isChecked);
                    editor.apply();
                }
            }
        });
    }

    public void setLocale(String localeName) {
        if (!localeName.equals(currentLanguage)) {
            currentLanguage = localeName;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constant.current_language, localeName);
            editor.apply();
            Toast.makeText(Settings.this, "Language changed.", Toast.LENGTH_SHORT).show();
            myLocale = new Locale(localeName);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.setLocale(myLocale);
            res.updateConfiguration(conf, dm);
            createConfigurationContext(conf);
            try{
                if(DetectConnection.checkInternetConnection(getApplicationContext()) && isLocationEnabled(getApplicationContext()) && hasPermissions(getApplicationContext(), PERMISSIONS)){
                    startService(new Intent(getApplicationContext(), LocationService.class));
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Intent intent=new Intent();
                    intent.putExtra("data", "changed");
                    setResult(1998, intent);
                    finish();
                }
            });
        } else {
            Toast.makeText(Settings.this, "Language already selected!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.share:
                shareApp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareApp(){
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, "COVID'19 App");
        share.putExtra(Intent.EXTRA_TEXT, "COVID'19 Mobile App - Stay Connected to all the info, track live data and get best social distancing tips and tricks. Download Now:\n\n" + "https://bit.ly/covid19indiasars");
        startActivity(Intent.createChooser(share, "Share COVID'19 App"));
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
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
