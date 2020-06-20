package com.basusingh.coronavirus.database.socialdistancinglist;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@androidx.room.Dao
public interface SocialDistancingListDao {

    @Query("SELECT * FROM SocialDistancingListItems WHERE title = :title ORDER BY uid DESC")
    List<SocialDistancingListItems> getAll(String title);

    @Insert
    void insertAll(List<SocialDistancingListItems> mList);

    @Insert
    void insert(SocialDistancingListItems mItem);

    @Query("DELETE FROM SocialDistancingListItems")
    void deleteAll();

    @Delete
    void delete(SocialDistancingListItems socialDistancingListItems);

}
