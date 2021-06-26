package com.succorfish.geofence.RoomDataBaseDAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.succorfish.geofence.RoomDataBaseEntity.ChatInfo;
import com.succorfish.geofence.RoomDataBaseEntity.GeofenceAlert;

import java.util.List;

@Dao
public interface ChatInfo_DAO {
    @Query("Select * from NewChat  where identifier=:bleAddressValue ORDER BY timeStamp ASC")
    List<ChatInfo> getAll_ChatsFromBleAddress(String bleAddressValue);
    @Insert
    void insert_ChatInfo(ChatInfo chatInfo);
    @Update
    void update_ChatInfo(ChatInfo chatInfo);
    @Delete
    void delete_ChatInfo (ChatInfo chatInfo);
    @Query("update newchat set status=:messageStatus where identifier=:bleAddress and sequence=:sequenceNumber")
    void updateMessageStatusInDb(String bleAddress,String sequenceNumber,String messageStatus);
}
