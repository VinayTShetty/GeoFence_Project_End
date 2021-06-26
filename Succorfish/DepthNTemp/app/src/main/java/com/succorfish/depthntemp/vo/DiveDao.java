package com.succorfish.depthntemp.vo;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.succorfish.depthntemp.db.TableDive;
import com.succorfish.depthntemp.db.TablePressureDepthCutOff;
import com.succorfish.depthntemp.db.TablePressureTemperature;

import java.util.List;

@Dao
public interface DiveDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(TableDive word);

    @Query("SELECT * FROM tbl_dive WHERE ble_address=:bleAddress AND utc_time = :utcTime")
    TableDive checkDiveRecordIsExistOrNot(String bleAddress, String utcTime);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(TableDive mTableDive);

    @Query("SELECT * FROM tbl_dive group by ble_address order by device_name")
    List<TableDive> getBleDeviceList();

    @Query("SELECT * FROM tbl_dive where ble_address=:bleAddress")
    List<TableDive> getDiveList(String bleAddress);

    @Query("SELECT * FROM tbl_dive where ble_address=:bleAddress and dive_id >=:diveID")
    TableDive getDiveListByDiveID(String bleAddress,int diveID);

    @Query("SELECT stationary_interval FROM tbl_dive where dive_id=:diveID")
    int getStationaryInterval(int diveID);

    @Query("DELETE FROM tbl_dive where dive_id=:diveID")
    void deleteDiveById(int diveID);

//    @Query("DELETE FROM tbl_dive where ble_address=:bleAddress")
//    void deleteDiveByBleAddress(String bleAddress);

//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    void insertAll(List<TableDive> repoList);
//
//    // Update all filed
//    @Update(onConflict = OnConflictStrategy.IGNORE)
//    int update(TableDive word);
//
//    @Query("UPDATE tbl_dive SET dive_no=:diveno WHERE dive_id = :id")
//    int update(int diveno, int id);
//
//    @Query("DELETE FROM tbl_dive")
//    void deleteAllDive();
//
//    @Query("SELECT * from tbl_dive")
//    List<TableDive> getAllDive();
//
//    @Query("SELECT * FROM tbl_dive WHERE dive_id=:id")
//    TableDive getDiveById(int id);


}
