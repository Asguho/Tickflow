package com.tickflow.app.domain.usecase

import java.time.LocalDate

data class SessionDaySlice(
    val date: LocalDate,
    val minutes: Int,
)
