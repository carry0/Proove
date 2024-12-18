package com.proove.smart.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.proove.ble.entity.DeviceInfoEntity;
import com.proove.ble.entity.EqInfoEntity;
import com.proove.ble.dao.DeviceInfoDao;
import com.proove.ble.dao.EqInfoDao;


@Database(
        entities = {DeviceInfoEntity.class, EqInfoEntity.class},
        version = 1
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract DeviceInfoDao deviceInfoDao();
    public abstract EqInfoDao eqInfoDao();
}
