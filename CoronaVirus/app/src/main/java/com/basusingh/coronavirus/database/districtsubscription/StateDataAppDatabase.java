package com.basusingh.coronavirus.database.districtsubscription;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {StateDataItems.class}, version = 1)
public abstract class StateDataAppDatabase extends RoomDatabase {
    public abstract StateDataDao stateDataDao();
}
