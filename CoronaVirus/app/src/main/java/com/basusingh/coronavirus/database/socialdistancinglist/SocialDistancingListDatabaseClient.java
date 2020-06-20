package com.basusingh.coronavirus.database.socialdistancinglist;

import android.content.Context;

import androidx.room.Room;

public class SocialDistancingListDatabaseClient {

    private Context mCtx;
    private static SocialDistancingListDatabaseClient mInstance;

    private SocialDistancingListAppDatabase socialDistancingListAppDatabase;

    private SocialDistancingListDatabaseClient(Context mCtx) {
        this.mCtx = mCtx;
        socialDistancingListAppDatabase = Room.databaseBuilder(mCtx, SocialDistancingListAppDatabase.class, "SocialDistancingList").build();
    }

    public static synchronized SocialDistancingListDatabaseClient getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new SocialDistancingListDatabaseClient(mCtx);
        }
        return mInstance;
    }

    public SocialDistancingListAppDatabase getSocialDistancingListAppDatabase() {
        return socialDistancingListAppDatabase;
    }
}
