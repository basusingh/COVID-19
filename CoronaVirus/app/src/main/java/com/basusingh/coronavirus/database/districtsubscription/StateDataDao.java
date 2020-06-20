package com.basusingh.coronavirus.database.districtsubscription;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@androidx.room.Dao
public interface StateDataDao {

    @Query("SELECT * FROM StateDataItems ORDER BY uid ASC")
    List<StateDataItems> getAll();

    @Insert
    void insertAll(List<StateDataItems> mList);

    @Insert
    void insert(StateDataItems mItem);

    @Query("DELETE FROM StateDataItems")
    void deleteAll();

    @Query("SELECT * FROM StateDataItems WHERE districtName = :dName AND stateName = :sName")
    StateDataItems checkIfExist(String dName, String sName);

    @Query("DELETE FROM StateDataItems WHERE districtName = :dName AND stateName = :sName")
    void deleteByDistrictAndState(String dName, String sName);

    @Query("SELECT COUNT(uid) FROM StateDataItems")
    int getCount();

    @Delete
    void delete(StateDataItems HelpItems);
}
