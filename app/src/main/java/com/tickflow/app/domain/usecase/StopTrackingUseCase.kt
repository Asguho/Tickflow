package com.tickflow.app.domain.usecase

import com.tickflow.app.core.model.WorkSession
import com.tickflow.app.domain.repository.WorkSessionRepository
import java.time.Instant
import javax.inject.Inject

class StopTrackingUseCase @Inject constructor(
    private val repository: WorkSessionRepository,
) {
    suspend operator fun invoke(at: Instant): WorkSession? =
        repository.stopActiveSession(at)
}
