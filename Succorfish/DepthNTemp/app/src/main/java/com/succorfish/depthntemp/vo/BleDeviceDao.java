package com.succorfish.depthntemp.vo;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.succorfish.depthntemp.db.TableBleDevice;

@Dao
public interface BleDeviceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(TableBleDevice bleDevice);

    @Query("SELECT * FROM tbl_ble_device WHERE ble_address=:address")
    TableBleDevice checkDeviceIsExistOrNot(String address);

    @Query("SELECT * FROM tbl_ble_device WHERE ble_address=:address")
    TableBleDevice getDeviceName(String address);
}
