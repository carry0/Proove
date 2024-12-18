package com.proove.ble.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


import com.proove.ble.entity.EqInfoEntity;

import java.util.List;


@Dao
public interface EqInfoDao {
    @Query("SELECT * FROM eq_info")
    List<EqInfoEntity> getEqInfoList();

    @Query("SELECT * FROM eq_info WHERE device_tag = :deviceTag and custom_index = :index")
    EqInfoEntity getEqInfo(String deviceTag, int index);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addEqInfo(EqInfoEntity eqInfoEntity);

    @Delete
    void removeEqInfo(EqInfoEntity EqInfoEntity);
}
