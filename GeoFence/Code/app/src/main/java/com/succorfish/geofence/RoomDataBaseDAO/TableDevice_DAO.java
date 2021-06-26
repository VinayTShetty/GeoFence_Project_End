package com.succorfish.geofence.RoomDataBaseDAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.succorfish.geofence.RoomDataBaseEntity.DeviceTable;
import com.succorfish.geofence.RoomDataBaseEntity.Rules;

import java.util.List;

@Dao
public interface TableDevice_DAO {
    @Query("Select * from tbl_Device")
    List<DeviceTable> getAll_tableDevices();
    @Insert
    void insert_tableDevices(DeviceTable deviceTable);
    @Update
    void update__tableDevices(DeviceTable deviceTable);
    @Delete
    void delete__tableDevices(DeviceTable deviceTable);
    @Query("Select * from tbl_Device where imei=:iridiumId")
    public boolean isImeiNumberExists(String iridiumId);
    @Query("Select device_token from tbl_Device where imei=:imeiNUmber")
    public String getDeviceToken(String imeiNUmber);

    @Query("select * from tbl_Device where BLE_Address=:bleAddress")
    public boolean isRecordAvaliableForBleAddress(String bleAddress);
    @Query("select name from tbl_Device where BLE_Address=:bleAddressValue")
    public String getDeviceNameSavedFromBleAddress(String bleAddressValue);
    @Query("select count(*) from tbl_Device")
    int getNumberOfRecordsCount();


}
