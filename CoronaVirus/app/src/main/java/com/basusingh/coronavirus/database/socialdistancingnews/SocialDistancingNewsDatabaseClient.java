package com.basusingh.coronavirus.database.socialdistancingnews;

import android.content.Context;

import androidx.room.Room;

public class SocialDistancingNewsDatabaseClient {

    private Context mCtx;
    private static SocialDistancingNewsDatabaseClient mInstance;

    private SocialDistancingNewsAppDatabase appDatabase;

    private SocialDistancingNewsDatabaseClient(Context mCtx) {
        this.mCtx = mCtx;
        appDatabase = Room.databaseBuilder(mCtx, SocialDistancingNewsAppDatabase.class, "SocialDistancingNews").build();
    }

    public static synchronized SocialDistancingNewsDatabaseClient getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new SocialDistancingNewsDatabaseClient(mCtx);
        }
        return mInstance;
    }

    public SocialDistancingNewsAppDatabase getSocialDistancingNewsAppDatabase() {
        return appDatabase;
    }
}
