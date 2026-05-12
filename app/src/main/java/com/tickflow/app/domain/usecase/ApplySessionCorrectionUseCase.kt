package com.tickflow.app.domain.usecase

import com.tickflow.app.core.model.CorrectionSuggestion
import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.model.WorkSession
import com.tickflow.app.domain.repository.WorkSessionRepository
import java.time.Instant
import javax.inject.Inject

class ApplySessionCorrectionUseCase @Inject constructor(
    private val repository: WorkSessionRepository,
) {
    suspend operator fun invoke(
        suggestion: CorrectionSuggestion,
        editedAt: Instant,
    ): WorkSession {
        val session = WorkSession(
            start = suggestion.gapStart,
            end = suggestion.gapEnd,
            source = SessionSource.Inferred,
            confidence = 80,
            corrected = true,
            editedAt = editedAt,
            note = suggestion.reason,
        )
        val id = repository.insert(session)
        return session.copy(id = id)
    }
}
