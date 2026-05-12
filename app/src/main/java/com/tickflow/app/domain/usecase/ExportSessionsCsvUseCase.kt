package com.tickflow.app.domain.usecase

import com.tickflow.app.core.model.WorkSession
import com.tickflow.app.domain.repository.WorkSessionRepository
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ExportSessionsCsvUseCase @Inject constructor(
    private val repository: WorkSessionRepository,
) {
    suspend operator fun invoke(zoneId: ZoneId): String =
        buildCsv(repository.getAllSessions(), zoneId)

    fun buildCsv(sessions: List<WorkSession>, zoneId: ZoneId): String {
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(zoneId)
        return buildString {
            appendLine("id,start,end,source,confidence,corrected,edited_at,note")
            sessions.forEach { session ->
                appendCsvRow(
                    session.id.toString(),
                    formatter.format(session.start),
                    session.end?.let(formatter::format).orEmpty(),
                    session.source.name,
                    session.confidence.toString(),
                    session.corrected.toString(),
                    session.editedAt?.let(formatter::format).orEmpty(),
                    session.note.orEmpty(),
                )
            }
        }
    }

    private fun StringBuilder.appendCsvRow(vararg cells: String) {
        appendLine(cells.joinToString(",") { it.csvEscape() })
    }

    private fun String.csvEscape(): String {
        val escaped = replace("\"", "\"\"")
        return if (escaped.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"$escaped\""
        } else {
            escaped
        }
    }
}
