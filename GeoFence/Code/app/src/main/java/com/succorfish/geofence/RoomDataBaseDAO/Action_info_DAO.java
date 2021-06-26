package com.succorfish.geofence.RoomDataBaseDAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.succorfish.geofence.RoomDataBaseEntity.Action_info;

import java.util.List;

@Dao
public interface Action_info_DAO {
    @Query("Select * from Action_info_Table")
    List<Action_info> getAActionInfo();
    @Insert
    void insert_ActionInfo(Action_info action_info);
    @Update
    void update_ActionInfo(Action_info action_info);
    @Delete
    void delete_ActionInfo(Action_info action_info);

}
