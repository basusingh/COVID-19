package com.basusingh.coronavirus.database.tracker;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {TrackerItems.class}, version = 1)
public abstract class TrackerAppDatabase extends RoomDatabase {
    public abstract TrackerDao trackerDao();
}
