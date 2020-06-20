package com.basusingh.coronavirus.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.basusingh.coronavirus.Constant;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.Locale;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class LocationService extends IntentService {

    FusedLocationProviderClient mFusedLocationClient;

    public LocationService() {
        super("LocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(isLocationEnabled(getApplicationContext()) && hasPermissions(this, PERMISSIONS)){
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            getLastLocation();
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

    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        try{
            mFusedLocationClient.getLastLocation().addOnCompleteListener(
                    new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            Location location = task.getResult();
                            if (location == null) {
                                requestNewLocationData();
                            } else {
                                setUserLocation(location.getLatitude(), location.getLongitude());
                            }
                        }
                    }
            );
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

       try{
           LocationRequest mLocationRequest = new LocationRequest();
           mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
           mLocationRequest.setInterval(0);
           mLocationRequest.setFastestInterval(0);
           mLocationRequest.setNumUpdates(1);

           mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
           mFusedLocationClient.requestLocationUpdates(
                   mLocationRequest, mLocationCallback,
                   Looper.myLooper()
           );
       } catch (Exception e){
           e.printStackTrace();
       }

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            setUserLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }
    };

    private void setUserLocation(double latitude, double longitude){
        Log.e("Setting user location", latitude + "-----" + longitude);
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try{
            addresses = geocoder.getFromLocation(latitude, longitude, 1);// Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (Exception e){
            e.printStackTrace();
            return;
        }
        final SharedPreferences sharedPref = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constant.user_address, addresses.get(0).getAddressLine(0));
        editor.putString(Constant.user_city, addresses.get(0).getLocality());
        editor.putString(Constant.user_state, addresses.get(0).getAdminArea());
        if(addresses.get(0).getCountryName().equalsIgnoreCase("भारत")){
            editor.putString(Constant.user_country, "India");
        } else {
            editor.putString(Constant.user_country, addresses.get(0).getCountryName());
        }
        editor.putString(Constant.user_country_code, addresses.get(0).getCountryCode());
        editor.putString(Constant.user_postal_code, addresses.get(0).getPostalCode());
        editor.putString(Constant.user_known_name, addresses.get(0).getFeatureName());
        editor.apply();
    }
}
