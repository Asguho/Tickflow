package com.tickflow.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.model.WorkSession
import java.time.Instant

@Entity(
    tableName = "work_sessions",
    indices = [
        Index(value = ["start"]),
        Index(value = ["end"]),
        Index(value = ["end", "start"]),
    ],
)
data class WorkSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val start: Instant,
    val end: Instant?,
    val source: SessionSource,
    val confidence: Int,
    val corrected: Boolean,
    val editedAt: Instant?,
    val note: String?,
)

fun WorkSessionEntity.toDomain(): WorkSession =
    WorkSession(
        id = id,
        start = start,
        end = end,
        source = source,
        confidence = confidence,
        corrected = corrected,
        editedAt = editedAt,
        note = note,
    )

fun WorkSession.toEntity(): WorkSessionEntity =
    WorkSessionEntity(
        id = id,
        start = start,
        end = end,
        source = source,
        confidence = confidence,
        corrected = corrected,
        editedAt = editedAt,
        note = note,
    )
