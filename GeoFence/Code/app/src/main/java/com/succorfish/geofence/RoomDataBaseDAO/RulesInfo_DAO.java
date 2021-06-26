package com.succorfish.geofence.RoomDataBaseDAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.succorfish.geofence.RoomDataBaseEntity.RulesInformation;

import java.util.List;

@Dao
public interface RulesInfo_DAO {
    @Query("Select * from Rule_info_Table")
    List<RulesInformation> getAll_RulesInfo();
    @Insert
    void insert_RulesInfo(RulesInformation rulesInformation);
    @Update
    void update__RulesInfo(RulesInformation rulesInformation);
    @Delete
    void delete__RulesInfo(RulesInformation rulesInformation);
}
