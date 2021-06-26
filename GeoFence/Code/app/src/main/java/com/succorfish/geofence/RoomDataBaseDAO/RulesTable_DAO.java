package com.succorfish.geofence.RoomDataBaseDAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.succorfish.geofence.RoomDataBaseEntity.Rules;
import com.succorfish.geofence.RoomDataBaseEntity.RulesInformation;

import java.util.List;

@Dao
public interface RulesTable_DAO {
    @Query("Select * from Rules_Table")
    List<Rules> getAll_RulesTable();
    @Insert
    void insert_RulesTable(Rules rules);
    @Update
    void update__RulesTable(Rules rules);
    @Delete
    void delete__RulesTable(Rules rules);
    @Query("Delete from Rules_Table where geofence_ID=:geoFenceId")
    void deleteRecordFromGeoFenceId(String geoFenceId);
    @Query("select rule_value from rules_table where geofence_ID=:geoFenceIdValue and rule_ID=:firmwareRule_IdValue")
    String getRuleValueFromGeoFenceId_ruleId(String geoFenceIdValue,String firmwareRule_IdValue);
}
