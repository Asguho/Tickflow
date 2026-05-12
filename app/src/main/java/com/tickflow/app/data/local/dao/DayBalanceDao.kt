package com.tickflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tickflow.app.data.local.entity.DayBalanceEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DayBalanceDao {
    @Query("SELECT * FROM day_balances WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    fun observeBetween(start: LocalDate, end: LocalDate): Flow<List<DayBalanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(balance: DayBalanceEntity)

    @Query("DELETE FROM day_balances")
    suspend fun deleteAll()
}
