package com.proove.ble.dao;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.proove.ble.entity.DeviceInfoEntity;

import java.util.List;


@Dao
public interface DeviceInfoDao {
    @Query("SELECT * FROM device_info")
    List<DeviceInfoEntity> getDeviceInfoList();

    @Query("SELECT * FROM device_info WHERE mac = :mac")
    DeviceInfoEntity getDeviceInfo(String mac);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addDevice(DeviceInfoEntity deviceInfoEntity);

    @Delete
    void removeDevice(DeviceInfoEntity deviceInfoEntity);
}
