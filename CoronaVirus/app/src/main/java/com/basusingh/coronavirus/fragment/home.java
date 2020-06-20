package com.basusingh.coronavirus.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.basusingh.coronavirus.Constant;
import com.basusingh.coronavirus.DistrictSubscribedList;
import com.basusingh.coronavirus.R;
import com.basusingh.coronavirus.utils.ContextWrapper;
import com.basusingh.coronavirus.utils.DetectConnection;
import com.basusingh.coronavirus.utils.LiveDataForegroundService;
import com.basusingh.coronavirus.utils.LocationService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class home extends Fragment {

    public home() {
        // Required empty public constructor
    }

    private RelativeLayout in_focus_layout;
    private TextView in_focus_text;
    SharedPreferences appPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View v ,Bundle s){
        in_focus_layout = v.findViewById(R.id.in_focus_layout);
        in_focus_text = v.findViewById(R.id.in_focus_text);

        final SharedPreferences sharedPref = getActivity().getSharedPreferences(
                Constant.home_pref, Context.MODE_PRIVATE);
        in_focus_text.setText(sharedPref.getString(Constant.home_pref_news, "WHO Director-General's opening remarks at the media briefing on COVID-19 - 8 April 2020"));
        in_focus_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebView(sharedPref.getString(Constant.home_pref_url, "https://www.who.int/dg/speeches/detail/who-director-general-s-opening-remarks-at-the-media-briefing-on-covid-19--8-april-2020"));
            }
        });


        LinearLayout link1 = v.findViewById(R.id.link1);
        link1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebView("https://www.who.int/emergencies/diseases/novel-coronavirus-2019");
            }
        });
        LinearLayout link2 = v.findViewById(R.id.link2);
        link2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebView("https://www.cdc.gov/coronavirus/2019-ncov/index.html");
            }
        });
        LinearLayout link3 = v.findViewById(R.id.link3);
        link3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebView("https://google.com/covid19-map/?hl=en");
            }
        });
        LinearLayout link4 = v.findViewById(R.id.link4);
        link4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebView("https://twitter.com/hashtag/coronavirus?lang=en");
            }
        });
        LinearLayout link5 = v.findViewById(R.id.link5);
        link5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebView("https://www.mohfw.gov.in/");
            }
        });
        LinearLayout link6 = v.findViewById(R.id.link6);
        link6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebView("https://www.thelancet.com/coronavirus");
            }
        });
        LinearLayout link7 = v.findViewById(R.id.link7);
        link7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebView("https://www.whatsapp.com/coronavirus/");
            }
        });

        AdView mAdView = v.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        appPref = getActivity().getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);

        final RelativeLayout show_live_tracking = v.findViewById(R.id.show_live_tracking);
        show_live_tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(!DetectConnection.checkInternetConnection(getActivity())){
                        Toast.makeText(getContext(), "Internet connection problem. Please try later.", Toast.LENGTH_SHORT).show();
                    } if(!isLocationEnabled(getActivity())){
                        Toast.makeText(getContext(), "Please enable location and restart app.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    } else if(!hasPermissions(getActivity(), PERMISSIONS)){
                        Toast.makeText(getContext(), "Please give location permission and restart app.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    } else if(isLocationNotAvailable()){
                        openLocationService();
                        Toast.makeText(getActivity(), "Location not available. Please try later.", Toast.LENGTH_LONG).show();
                    } else {
                        SharedPreferences.Editor editor = appPref.edit();
                        editor.putBoolean(Constant.app_pref_status_live_tracking, true);
                        editor.putBoolean(Constant.app_pref_can_show_live_tracking_feature, false);
                        editor.apply();
                        show_live_tracking.setVisibility(View.GONE);
                        startLiveTracking();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getContext(), "An error occurred. Please try later.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView cancel_live_tracking = v.findViewById(R.id.cancel_live_tracking);
        cancel_live_tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = appPref.edit();
                editor.putBoolean(Constant.app_pref_can_show_live_tracking_feature, false);
                editor.apply();
                show_live_tracking.setVisibility(View.GONE);
            }
        });

        final RelativeLayout show_district_live_tracking = v.findViewById(R.id.show_district_live_tracking);
        show_district_live_tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), DistrictSubscribedList.class));
            }
        });
        ImageView cancel_district_live_tracking = v.findViewById(R.id.cancel_district_live_tracking);
        cancel_district_live_tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = appPref.edit();
                editor.putBoolean(Constant.app_pref_can_show_district_live_tracking, false);
                editor.apply();
                show_district_live_tracking.setVisibility(View.GONE);
            }
        });


        if(appPref.getBoolean(Constant.app_pref_can_show_live_tracking_feature, true)){
            show_live_tracking.setVisibility(View.VISIBLE);
        } else {
            show_live_tracking.setVisibility(View.GONE);
        }

        if(appPref.getBoolean(Constant.app_pref_can_show_district_live_tracking, true)){
            if(appPref.getString(Constant.user_country, null) != null && appPref.getString(Constant.user_country, "null").equalsIgnoreCase("India")){
                show_district_live_tracking.setVisibility(View.VISIBLE);
            } else {
                show_district_live_tracking.setVisibility(View.GONE);
            }
        } else {
            show_district_live_tracking.setVisibility(View.GONE);
        }


    }

    private void openLocationService(){
        if(isLocationEnabled(getContext())){
            getActivity().startService(new Intent(getContext(), LocationService.class));
        }
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

    private void openWebView(String s){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(s));
        startActivity(i);
    }

    private void startLiveTracking(){
        Intent serviceIntent = new Intent(getActivity(), LiveDataForegroundService.class);
        serviceIntent.putExtra("type", 0);
        ContextCompat.startForegroundService(getActivity(), serviceIntent);
    }

    public static Boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return  lm != null && lm.isLocationEnabled();
        } else {
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);
        }
    }

    private boolean isLocationNotAvailable(){
        return appPref.getString(Constant.user_country, null) == null;
    }
}
