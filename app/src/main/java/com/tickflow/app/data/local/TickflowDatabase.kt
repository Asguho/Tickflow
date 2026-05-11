package com.tickflow.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tickflow.app.data.local.dao.DayBalanceDao
import com.tickflow.app.data.local.dao.WorkSessionDao
import com.tickflow.app.data.local.entity.Converters
import com.tickflow.app.data.local.entity.DayBalanceEntity
import com.tickflow.app.data.local.entity.WorkSessionEntity

@Database(
    entities = [WorkSessionEntity::class, DayBalanceEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class TickflowDatabase : RoomDatabase() {
    abstract fun workSessionDao(): WorkSessionDao
    abstract fun dayBalanceDao(): DayBalanceDao
}
