package com.basusingh.coronavirus.database.socialdistancingnews;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SocialDistancingNewsItems.class}, version = 1)
public abstract class SocialDistancingNewsAppDatabase extends RoomDatabase {
    public abstract SocialDistancingNewsDao SocialDistancingNewsDao();
}