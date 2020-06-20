package com.basusingh.coronavirus.database.tracker;

import android.content.Context;

import androidx.room.Room;

public class TrackerDatabaseClient {

    private Context mCtx;
    private static TrackerDatabaseClient mInstance;

    private TrackerAppDatabase TrackerAppDatabase;

    private TrackerDatabaseClient(Context mCtx) {
        this.mCtx = mCtx;
        TrackerAppDatabase = Room.databaseBuilder(mCtx, TrackerAppDatabase.class, "Tracker").build();
    }

    public static synchronized TrackerDatabaseClient getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new TrackerDatabaseClient(mCtx);
        }
        return mInstance;
    }

    public TrackerAppDatabase getTrackerAppDatabase() {
        return TrackerAppDatabase;
    }
}
