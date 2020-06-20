package com.basusingh.coronavirus.database.districtsubscription;

import android.content.Context;

import androidx.room.Room;

public class StateDataDatabaseClient {

    private Context mCtx;
    private static StateDataDatabaseClient mInstance;

    private StateDataAppDatabase StateDataAppDatabase;

    private StateDataDatabaseClient(Context mCtx) {
        this.mCtx = mCtx;
        StateDataAppDatabase = Room.databaseBuilder(mCtx, StateDataAppDatabase.class, "StateData").build();
    }

    public static synchronized StateDataDatabaseClient getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new StateDataDatabaseClient(mCtx);
        }
        return mInstance;
    }

    public StateDataAppDatabase getStateDataAppDatabase() {
        return StateDataAppDatabase;
    }
}
