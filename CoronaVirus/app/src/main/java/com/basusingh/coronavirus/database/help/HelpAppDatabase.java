package com.basusingh.coronavirus.database.help;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {HelpItems.class}, version = 1)
public abstract class HelpAppDatabase extends RoomDatabase {
    public abstract HelpDao HelpDao();
}
