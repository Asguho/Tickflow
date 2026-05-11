package com.tickflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tickflow.app.data.local.entity.WorkSessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface WorkSessionDao {
    @Query(
        """
        SELECT * FROM work_sessions
        WHERE start < :endExclusive AND (end IS NULL OR end > :startInclusive)
        ORDER BY start ASC
        """,
    )
    fun observeBetween(startInclusive: Instant, endExclusive: Instant): Flow<List<WorkSessionEntity>>

    @Query("SELECT * FROM work_sessions WHERE end IS NULL ORDER BY start DESC LIMIT 1")
    fun observeActive(): Flow<WorkSessionEntity?>

    @Query("SELECT * FROM work_sessions WHERE end IS NULL ORDER BY start DESC LIMIT 1")
    suspend fun getActive(): WorkSessionEntity?

    @Query("SELECT * FROM work_sessions ORDER BY start ASC")
    suspend fun getAll(): List<WorkSessionEntity>

    @Query(
        """
        SELECT COUNT(*) FROM work_sessions
        WHERE id != :excludeId
        AND start < :endExclusive
        AND (end IS NULL OR end > :startInclusive)
        """,
    )
    suspend fun countOverlaps(
        startInclusive: Instant,
        endExclusive: Instant,
        excludeId: Long = 0,
    ): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: WorkSessionEntity): Long

    @Update
    suspend fun update(entity: WorkSessionEntity)

    @Delete
    suspend fun delete(entity: WorkSessionEntity)

    @Query("DELETE FROM work_sessions")
    suspend fun deleteAll()
}
