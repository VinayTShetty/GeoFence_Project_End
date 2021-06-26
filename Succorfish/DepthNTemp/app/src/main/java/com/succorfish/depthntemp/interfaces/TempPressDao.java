package com.succorfish.depthntemp.interfaces;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.succorfish.depthntemp.db.TableDive;
import com.succorfish.depthntemp.db.TablePressureDepthCutOff;
import com.succorfish.depthntemp.db.TablePressureTemperature;

import java.util.List;

/*Table temperature and pressure db operation*/
@Dao
public interface TempPressDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(TablePressureTemperature mPressureTemperature);

    @Query("SELECT * from tbl_pre_temp where pre_temp_dive_id=:diveID")
    List<TablePressureTemperature> getAllPressTempDataBySingleDive(int diveID);

    @Query("SELECT * from tbl_pre_temp where pre_temp_dive_id=:diveOneID or pre_temp_dive_id=:diveTwoID")
    List<TablePressureTemperature> getAllPressTempDataByMultiDive(int diveOneID, int diveTwoID);

    @Query("SELECT * from tbl_pre_temp where utc_time >=:startTimeFrom and utc_time<:startTimeTo")
    List<TablePressureTemperature> getAllPressTempDataBySingleDate(long startTimeFrom, long startTimeTo);

    @Query("DELETE FROM tbl_pre_temp where pre_temp_dive_id=:diveID")
    void deleteTempPressDiveByDiveId(int diveID);


//    @Query("SELECT * FROM tbl_pre_temp WHERE pre_temp_id=:preTempId")
//    TablePressureTemperature checkDepthMeterIsExistOrNot(int preTempId);

    //    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    void insertAll(List<TablePressureTemperature> repoList);
//
    @Update(onConflict = OnConflictStrategy.IGNORE)
    int update(TablePressureTemperature word);
//
//    @Query("DELETE FROM tbl_pre_temp")
//    void deleteAll();
//
//    @Query("SELECT * from tbl_pre_temp")
//    List<TablePressureTemperature> getAllPressTempData();
//
//    @Query("SELECT * FROM tbl_pre_temp WHERE pre_temp_id=:id")
//    TablePressureTemperature getPressTempById(int id);

}
