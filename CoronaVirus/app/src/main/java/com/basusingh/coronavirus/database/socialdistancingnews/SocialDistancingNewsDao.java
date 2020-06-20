package com.basusingh.coronavirus.database.socialdistancingnews;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@androidx.room.Dao
public interface SocialDistancingNewsDao {

    @Query("SELECT * FROM SocialDistancingNewsItems ORDER BY uid DESC")
    List<SocialDistancingNewsItems> getAll();

    @Insert
    void insertAll(List<SocialDistancingNewsItems> mList);

    @Insert
    void insert(SocialDistancingNewsItems mItem);

    @Query("DELETE FROM SocialDistancingNewsItems")
    void deleteAll();

    @Delete
    void delete(SocialDistancingNewsItems items);

}
