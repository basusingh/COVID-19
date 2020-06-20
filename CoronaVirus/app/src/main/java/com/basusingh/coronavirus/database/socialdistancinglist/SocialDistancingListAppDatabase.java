package com.basusingh.coronavirus.database.socialdistancinglist;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SocialDistancingListItems.class}, version = 1)
public abstract class SocialDistancingListAppDatabase extends RoomDatabase {
    public abstract SocialDistancingListDao SocialDistancingListDao();
}