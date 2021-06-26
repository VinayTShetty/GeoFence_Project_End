package com.succorfish.depthntemp.interfaces;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;


import com.succorfish.depthntemp.db.TablePressureDepthCutOff;

import java.util.List;

/*Table depth abd cut off db operation*/
@Dao
public interface DepthCutOffDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(TablePressureDepthCutOff cutOff);

    @Query("SELECT * FROM tbl_pre_depth_cut_off WHERE pre_depth_meter=:depthMeter")
    TablePressureDepthCutOff checkDepthMeterIsExistOrNot(double depthMeter);

    @Query("SELECT * from tbl_pre_depth_cut_off")
    List<TablePressureDepthCutOff> getAllDepthCutOffList();

    @Query("SELECT * from tbl_pre_depth_cut_off order by abs(pre_depth_milibar-:milibar)")
    TablePressureDepthCutOff getRecordBasedOnMilibar(int milibar);


//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    void insertAll(List<TablePressureDepthCutOff> cutOffList);
//
//    // Update all filed
//    @Update(onConflict = OnConflictStrategy.IGNORE)
//    int update(TablePressureDepthCutOff cutOff);
//
//    @Query("DELETE FROM tbl_pre_depth_cut_off")
//    void deleteAllDive();
//
//    @Query("SELECT * FROM tbl_pre_depth_cut_off WHERE pre_cutoff_id=:id")
//    TablePressureDepthCutOff getDepthById(int id);
}
