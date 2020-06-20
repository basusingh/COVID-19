package com.basusingh.coronavirus.utils;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class AddToFirebaseAnalytics {

    //TODO

    private FirebaseAnalytics firebaseAnalytics;

    public AddToFirebaseAnalytics(Context mContext){
        firebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
    }
    public void addData(String id, String name, String value){
        String source = "";
        try{
            source = Build.MANUFACTURER + "/" + Build.BRAND + Build.DEVICE + "/" + Build.HARDWARE;
        } catch (Exception e){
            e.printStackTrace();
            source = "Error in permission!";
        }
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.ITEM_VARIANT, value);
        bundle.putString(FirebaseAnalytics.Param.SOURCE, source);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }
}
