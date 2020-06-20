package com.basusingh.coronavirus;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.basusingh.coronavirus.appstart.AppInfoPage;
import com.basusingh.coronavirus.appstart.DownloadData;
import com.basusingh.coronavirus.appstart.Login;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final SharedPreferences sharedPref = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);
        if(sharedPref.getBoolean(Constant.app_pref_is_first_time, true)){
            startActivity(new Intent(getApplicationContext(), AppInfoPage.class));
            finish();
        } else if(sharedPref.getBoolean(Constant.app_pref_not_sign_up, true)){
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        } else if(sharedPref.getBoolean(Constant.app_pref_not_fetch_data, true)){
            startActivity(new Intent(getApplicationContext(), DownloadData.class));
            finish();
        } else if(sharedPref.getBoolean(Constant.app_pref_app_blocked_from_usage, false)){
            startActivity(new Intent(SplashScreen.this, AppBlockedFromUsage.class));
            finish();
        } else {
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
            finish();
        }
    }

}
