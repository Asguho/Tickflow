package com.tickflow.app.ui.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tickflow.app.core.model.SessionSource
import com.tickflow.app.core.model.WorkSession
import com.tickflow.app.core.time.ClockProvider
import com.tickflow.app.domain.repository.WorkSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val repository: WorkSessionRepository,
    private val clockProvider: ClockProvider,
) : ViewModel() {
    private val zoneId = clockProvider.zoneId()
    private val today = LocalDate.now(zoneId)
    private val selectedDate = MutableStateFlow(today)
    private val editorState = MutableStateFlow(SessionEditorUiState())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<SessionListUiState> = combine(
        selectedDate.flatMapLatest { date ->
            repository.observeSessionsBetween(
                date.atStartOfDay(zoneId).toInstant(),
                date.plusDays(1).atStartOfDay(zoneId).toInstant(),
            )
        }
            .map { sessions -> sessions.sortedByDescending { it.start } },
        selectedDate,
        editorState,
    ) { sessions, date, editor ->
        SessionListUiState(date = date, sessions = sessions, editor = editor)
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionListUiState())

    fun previousDay() {
        selectedDate.update { it.minusDays(1) }
    }

    fun nextDay() {
        selectedDate.update { it.plusDays(1) }
    }

    fun openCreateEditor() {
        val date = selectedDate.value
        editorState.value = SessionEditorUiState(
            visible = true,
            title = "Add session",
            dateText = DateFormatter.format(date),
            startText = "09:00",
            endText = "17:00",
        )
    }

    fun openEditEditor(session: WorkSession) {
        editorState.value = SessionEditorUiState(
            visible = true,
            sessionId = session.id,
            title = "Edit session",
            dateText = DateFormatter.format(session.start.atZone(zoneId).toLocalDate()),
            startText = TimeFormatter.format(session.start.atZone(zoneId).toLocalTime()),
            endText = session.end?.atZone(zoneId)?.toLocalTime()?.let(TimeFormatter::format).orEmpty(),
        )
    }

    fun dismissEditor() {
        editorState.value = SessionEditorUiState()
    }

    fun updateEditorStart(value: String) {
        editorState.update { it.copy(startText = value, error = null) }
    }

    fun updateEditorDate(value: String) {
        editorState.update { it.copy(dateText = value, error = null) }
    }

    fun updateEditorEnd(value: String) {
        editorState.update { it.copy(endText = value, error = null) }
    }

    fun saveEditor() {
        val current = editorState.value
        viewModelScope.launch {
            val date = parseDate(current.dateText)
            val startTime = parseTime(current.startText)
            val endTime = parseTime(current.endText)
            if (date == null || startTime == null || endTime == null) {
                editorState.update { it.copy(error = "Use yyyy-MM-dd and HH:mm values.") }
                return@launch
            }
            if (!endTime.isAfter(startTime)) {
                editorState.update { it.copy(error = "End must be after start.") }
                return@launch
            }

            val start = date.atTime(startTime).atZone(zoneId).toInstant()
            val end = date.atTime(endTime).atZone(zoneId).toInstant()
            val saved = runCatching {
                if (current.sessionId == null) {
                    repository.insert(
                        WorkSession(
                            start = start,
                            end = end,
                            source = SessionSource.Manual,
                            corrected = true,
                            editedAt = clockProvider.now(),
                        ),
                    )
                } else {
                    repository.update(
                        WorkSession(
                            id = current.sessionId,
                            start = start,
                            end = end,
                            source = SessionSource.Edited,
                            corrected = true,
                            editedAt = clockProvider.now(),
                        ),
                    )
                }
            }
            if (saved.isSuccess) {
                selectedDate.value = date
                dismissEditor()
            } else {
                editorState.update {
                    it.copy(error = saved.exceptionOrNull()?.message ?: "Session could not be saved.")
                }
            }
        }
    }

    fun delete(session: WorkSession) {
        viewModelScope.launch { repository.delete(session) }
    }

    private fun parseTime(value: String): LocalTime? {
        return try {
            LocalTime.parse(value, TimeFormatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private fun parseDate(value: String): LocalDate? {
        return try {
            LocalDate.parse(value, DateFormatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    private companion object {
        val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val DateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }
}

data class SessionListUiState(
    val date: LocalDate = LocalDate.now(),
    val sessions: List<WorkSession> = emptyList(),
    val editor: SessionEditorUiState = SessionEditorUiState(),
)

data class SessionEditorUiState(
    val visible: Boolean = false,
    val sessionId: Long? = null,
    val title: String = "Session",
    val dateText: String = "",
    val startText: String = "",
    val endText: String = "",
    val error: String? = null,
)
