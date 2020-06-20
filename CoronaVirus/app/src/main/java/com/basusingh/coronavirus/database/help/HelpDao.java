package com.basusingh.coronavirus.database.help;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@androidx.room.Dao
public interface HelpDao {

    @Query("SELECT * FROM HelpItems ORDER BY uid ASC")
    List<HelpItems> getAll();

    @Insert
    void insertAll(List<HelpItems> mList);

    @Insert
    void insert(HelpItems mItem);

    @Query("DELETE FROM HelpItems")
    void deleteAll();

    @Delete
    void delete(HelpItems HelpItems);
}
