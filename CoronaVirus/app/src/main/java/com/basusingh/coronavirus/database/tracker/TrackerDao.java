package com.basusingh.coronavirus.database.tracker;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@androidx.room.Dao
public interface TrackerDao {


    @Query("SELECT * FROM TrackerItems")
    List<TrackerItems> getAll();

    @Insert
    void insertAll(List<TrackerItems> mList);

    @Insert
    void insert(TrackerItems mItem);

    @Query("DELETE FROM TrackerItems")
    void deleteAll();

    @Delete
    void delete(TrackerItems trackerItems);
}
