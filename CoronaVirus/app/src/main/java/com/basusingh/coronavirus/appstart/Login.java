package com.basusingh.coronavirus.appstart;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.basusingh.coronavirus.BuildConfig;
import com.basusingh.coronavirus.Constant;
import com.basusingh.coronavirus.MainActivity;
import com.basusingh.coronavirus.utils.DetectConnection;
import com.basusingh.coronavirus.utils.LocationService;
import com.basusingh.coronavirus.utils.NetworkSingleton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.basusingh.coronavirus.R;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    AppCompatEditText name, email, phone;
    FrameLayout btn_submit;
    RadioGroup radioGroup;
    String fcm_id;
    String mGender;
    ProgressDialog progressDialog;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPref = getSharedPreferences(
                Constant.app_pref, Context.MODE_PRIVATE);

        if(isLocationEnabled(getApplicationContext()) && hasPermissions(this, PERMISSIONS) && !isLocationNotAvailable()){
            try{
                startService(new Intent(getApplicationContext(), LocationService.class));
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);

        progressDialog = new ProgressDialog(Login.this);
        progressDialog.setMessage("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        btn_submit = findViewById(R.id.submit_btn);
        radioGroup = findViewById(R.id.radio_gender);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateData();
            }
        });

        FrameLayout skip_btn = findViewById(R.id.skip_btn);
        skip_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!DetectConnection.checkInternetConnection(getApplicationContext())){
                    showErrorAndRetry("Internet Connection Problem", 2);
                    return;
                }
                SkipAndUploadDataToServer();
            }
        });
    }

    private void validateData(){
        if(name.getText().toString().trim().isEmpty() || name.getText().toString().trim().length()<3){
            Toast.makeText(getApplicationContext(), "Please enter valid name", Toast.LENGTH_LONG).show();
            return;
        }
        if(phone.getText().toString().trim().isEmpty() || phone.getText().toString().trim().length()!=10){
            Toast.makeText(getApplicationContext(), "Please enter valid mobile number", Toast.LENGTH_LONG).show();
            return;
        }
        if(email.getText().toString().trim().isEmpty() || !isEmailValid(email.getText().toString().trim())){
            Toast.makeText(getApplicationContext(), "Please enter valid email", Toast.LENGTH_LONG).show();
            return;
        }

        if(radioGroup.getCheckedRadioButtonId() == -1){
            Toast.makeText(getApplicationContext(), "Please select gender", Toast.LENGTH_LONG).show();
            return;
        }
        uploadDataToServer();
    }

    private void uploadDataToServer(){

        if(!DetectConnection.checkInternetConnection(getApplicationContext())){
            showErrorAndRetry("No Internet Connection", 1);
            return;
        }

        if(!progressDialog.isShowing()){
            progressDialog.show();
        }

        final String mName = name.getText().toString().trim();
        final String mPhone = phone.getText().toString().trim();
        final String mEmail = email.getText().toString().trim();
        if(radioGroup.getCheckedRadioButtonId() == R.id.radio_male){
            mGender = "male";
        } else {
            mGender = "female";
        }
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Error", "getInstanceId failed", task.getException());
                            showErrorAndRetry("An error occurred", 1);
                            return;
                        }
                        try{
                            fcm_id = task.getResult().getToken();
                        } catch (Exception e){
                            e.printStackTrace();
                            fcm_id = "not_registered";
                        }
                        final String mDevice = Build.VERSION.SDK_INT + "+" + Build.DEVICE + "+" + Build.MODEL + "+" + Build.PRODUCT;

                        final String location = getUserLocation();

                        hideKeyboard(Login.this);
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.SERVER_BASE_URL + "login" + "/" + mName + "/" + mEmail + "/" + mPhone + "/" + mGender + "/" + fcm_id + "/" + mDevice + "/" + location + "/" + BuildConfig.VERSION_CODE,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        progressDialog.dismiss();
                                        try {
                                            JSONObject o = new JSONObject(response);
                                            if(o.getString("error").equalsIgnoreCase("true")){
                                                showErrorAndRetry("An error occurred", 1);
                                            } else {
                                                addDataOffline(mName, mEmail, mPhone, mGender);
                                                openDownloadData();
                                            }
                                        } catch (Exception e){
                                            e.printStackTrace();
                                            showErrorAndRetry("An error occurred", 1);
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("Error", "Login");
                                        showErrorAndRetry("An error occurred", 1);
                                    }
                                }) {
                        };

                        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
                    }
                });

    }


    private String getUserLocation(){
        if(sharedPref.getString(Constant.user_country, null) == null){
            return "not_registered_yet";
        }

        String divider = "+ ";
        String address = sharedPref.getString(Constant.user_address, "null") + divider;
        String city = sharedPref.getString(Constant.user_city, "null") + divider;
        String state = sharedPref.getString(Constant.user_state, "null") + divider;
        String country = sharedPref.getString(Constant.user_country, "null") + divider;
        String postal = sharedPref.getString(Constant.user_postal_code, "null") + divider;
        String knownname = sharedPref.getString(Constant.user_known_name, "null");

        return (address + city + state + country + postal + knownname);
    }

    private void SkipAndUploadDataToServer(){

        if(!progressDialog.isShowing()){
            progressDialog.show();
        }

        final String mName = "check";
        final String mPhone = "check";
        final String mEmail = "check";
        mGender = "check";
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Error", "getInstanceId failed", task.getException());
                            showErrorAndRetry("An error occurred", 2);
                            return;
                        }
                        try{
                            fcm_id = task.getResult().getToken();
                        } catch (Exception e){
                            e.printStackTrace();
                            fcm_id = "not_registered";
                        }
                        final String mDevice = Build.VERSION.SDK_INT + "+" + Build.DEVICE + "+" + Build.MODEL + "+" + Build.PRODUCT;

                        final String location = getUserLocation();

                        if(!DetectConnection.checkInternetConnection(getApplicationContext())){
                            showErrorAndRetry("No Internet Connection", 2);
                            return;
                        }
                        hideKeyboard(Login.this);
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.SERVER_BASE_URL + "login" + "/" + mName + "/" + mEmail + "/" + mPhone + "/" + mGender + "/" + fcm_id + "/" + mDevice + "/" + location + "/" + BuildConfig.VERSION_CODE,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        progressDialog.dismiss();
                                        try {
                                            JSONObject o = new JSONObject(response);
                                            if(o.getString("error").equalsIgnoreCase("true")){
                                                showErrorAndRetry("An error occurred", 2);
                                            } else {
                                                addDataOffline(mName, mEmail, mPhone, mGender);
                                                openDownloadData();
                                            }
                                        } catch (Exception e){
                                            e.printStackTrace();
                                            showErrorAndRetry("An error occurred", 2);
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("Error", "Login");
                                        showErrorAndRetry("An error occurred",2);
                                    }
                                }) {
                        };

                        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
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


    public static void hideKeyboard(Activity activity) {
        try{
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            //Find the currently focused view, so we can grab the correct window token from it.
            View view = activity.getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(activity);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private void addDataOffline(String name, String email, String phone, String gender){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constant.app_pref_user_name, name);
        editor.putString(Constant.app_pref_user_email, email);
        editor.putString(Constant.app_pref_user_phone, phone);
        editor.putString(Constant.app_pref_user_gender, gender);
        editor.apply();
    }

    private void showErrorAndRetry(final String message, final int value){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        new AlertDialog.Builder(Login.this)
                .setTitle(message)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(!DetectConnection.checkInternetConnection(getApplicationContext())){
                            showErrorAndRetry("No Internet Connection", value);
                            return;
                        }
                        if(value == 1){
                            uploadDataToServer();
                        } else {
                            SkipAndUploadDataToServer();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }


    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void openDownloadData(){
        if(progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(Constant.app_pref_not_sign_up, false);
        editor.apply();

        startActivity(new Intent(getApplicationContext(), DownloadData.class));
        finish();
    }

}
