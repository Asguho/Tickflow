package com.tickflow.app.domain.usecase

import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.model.WorkSession
import com.tickflow.app.domain.repository.WorkSessionRepository
import java.time.Instant
import javax.inject.Inject

class StartTrackingUseCase @Inject constructor(
    private val repository: WorkSessionRepository,
) {
    suspend operator fun invoke(at: Instant, source: SessionSource): WorkSession =
        repository.startSession(at, source)
}
