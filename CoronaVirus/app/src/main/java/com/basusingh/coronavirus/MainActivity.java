package com.basusingh.coronavirus;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.basusingh.coronavirus.utils.ContextWrapper;
import com.basusingh.coronavirus.utils.DetectConnection;
import com.basusingh.coronavirus.utils.LiveDataForegroundService;
import com.basusingh.coronavirus.utils.LocationService;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle("");
            toolbar.setTitle("");
        }
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        sharedPref = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);

        checkUpdate();
        showPermission();

        if(sharedPref.getBoolean(Constant.app_pref_status_live_tracking, false) || sharedPref.getBoolean(Constant.app_pref_status_district_tracking, false)){
            try{
                if(DetectConnection.checkInternetConnection(getApplicationContext()) && isLocationEnabled(getApplicationContext()) && hasPermissions(this, PERMISSIONS) && !isLocationNotAvailable()){
                    startLiveTracking();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
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

    private boolean isLocationNotAvailable(){
        return sharedPref.getString(Constant.user_country, null) == null;
    }



    private void startLiveTracking(){
        Intent serviceIntent = new Intent(getApplicationContext(), LiveDataForegroundService.class);
        serviceIntent.putExtra("type", 0);
        ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);
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


    private void checkUpdate(){
        if(sharedPref.getBoolean(Constant.app_update_available, false) && !sharedPref.getString(Constant.app_update_version, String.valueOf(BuildConfig.VERSION_CODE)).equalsIgnoreCase(String.valueOf(BuildConfig.VERSION_CODE))){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Constant.app_update_available, false);
            editor.apply();
            showAppUpdateDialog(sharedPref.getString(Constant.app_update_link, "http://www.coronasars.org"));
        } else {
            checkNewUpdateInfo();
        }
    }

    private void checkNewUpdateInfo(){
        if(sharedPref.getBoolean(Constant.app_update_info, true)){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Constant.app_update_info, false);
            editor.apply();
            showNewAppUpdateInfo();
        }
    }

    private void showNewAppUpdateInfo(){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("What's new?")
                .setMessage(sharedPref.getString(Constant.app_update_whats_new, Constant.app_update_whats_new_feature))
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    private void showAppUpdateDialog(final String url){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("A new update is available")
                .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.help:
                startActivity(new Intent(getApplicationContext(), Help.class));
                return true;
            case R.id.setting:
                startActivityForResult(new Intent(getApplicationContext(), Settings.class), 1998);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1998)
        {
            if(data != null){
                if(data.getStringExtra("data").equalsIgnoreCase("changed")){
                    recreate();
                }
            }
        }
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

    private void showPermission(){
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        } else {
            openLocationService();
        }
    }

    private void openLocationService(){
        if(isLocationEnabled(getApplicationContext()) && !isLocationNotAvailable() && hasPermissions(getApplicationContext(), PERMISSIONS)){
            startService(new Intent(getApplicationContext(), LocationService.class));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openLocationService();
                }
            }
        }
    }


}
