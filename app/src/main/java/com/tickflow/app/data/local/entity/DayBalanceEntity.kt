package com.tickflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tickflow.app.core.model.DayBalance
import java.time.LocalDate

@Entity(tableName = "day_balances")
data class DayBalanceEntity(
    @PrimaryKey
    val date: LocalDate,
    val targetMinutes: Int,
    val actualMinutes: Int,
    val carryInMinutes: Int,
    val isWorkday: Boolean,
    val manuallyAppliedOnNonWorkday: Boolean,
)

fun DayBalanceEntity.toDomain(): DayBalance =
    DayBalance(
        date = date,
        targetMinutes = targetMinutes,
        actualMinutes = actualMinutes,
        carryInMinutes = carryInMinutes,
        isWorkday = isWorkday,
        manuallyAppliedOnNonWorkday = manuallyAppliedOnNonWorkday,
    )

fun DayBalance.toEntity(): DayBalanceEntity =
    DayBalanceEntity(
        date = date,
        targetMinutes = targetMinutes,
        actualMinutes = actualMinutes,
        carryInMinutes = carryInMinutes,
        isWorkday = isWorkday,
        manuallyAppliedOnNonWorkday = manuallyAppliedOnNonWorkday,
    )
