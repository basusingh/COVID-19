package com.basusingh.coronavirus.database.help;

import android.content.Context;

import androidx.room.Room;

public class HelpDatabaseClient {

    private Context mCtx;
    private static HelpDatabaseClient mInstance;

    private HelpAppDatabase HelpAppDatabase;

    private HelpDatabaseClient(Context mCtx) {
        this.mCtx = mCtx;
        HelpAppDatabase = Room.databaseBuilder(mCtx, HelpAppDatabase.class, "Help").build();
    }

    public static synchronized HelpDatabaseClient getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new HelpDatabaseClient(mCtx);
        }
        return mInstance;
    }

    public HelpAppDatabase getHelpAppDatabase() {
        return HelpAppDatabase;
    }
}
