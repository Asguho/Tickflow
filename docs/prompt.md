# Tickflow Project Memory: Single Source of Truth

Last updated: 2026-05-11

## 1. Product Vision

Tickflow is a privacy-first Android workday assistant that automatically tracks work time from the user's office Wi-Fi presence, predicts when the user can leave, and carries time deficits or credits forward between workdays.

The product should feel ambient, automatic, low-friction, and deeply Android-native. The core interaction is not "manage a timer"; it is "know when my workday is complete without thinking about it."

Primary product promise:

> Tickflow quietly understands when you are at work, keeps an accurate workday total, and tells you the human answer: when you can leave.

## 2. Core User Outcomes

| Outcome | Description | Priority |
| --- | --- | --- |
| Automatic work tracking | When the phone connects to a configured office Wi-Fi network, Tickflow starts tracking work time in the background. When the phone leaves that Wi-Fi context, Tickflow stops or pauses tracking according to the user's rules. | Must have |
| Predictive leave time | Tickflow shows the expected leave time based on today's target, work already completed, active session time, and carried balance. | Must have |
| Carry-over balance | If the user works more or less than the daily target, the difference is carried into future workdays and adjusts future leave predictions. | Must have |
| Manual control | The user can manually start, stop, edit, or mark work sessions for remote work, meetings, travel, or cases where automatic detection is ambiguous. | Must have |
| Return-to-office correction | If the user leaves the office and later returns, Tickflow can offer a one-tap way to mark the intervening gap as work when appropriate. | Should have |
| Persistent live status | While tracking is active, Tickflow uses a foreground service and persistent notification to show worked time and expected completion. | Must have |
| Offline-first privacy | No account is required. Work sessions and settings are stored locally by default. | Must have |
| Modern Android feel | Jetpack Compose, Material 3, smooth state updates, useful notifications, and low battery impact. | Must have |

## 3. Target Users

Primary users:

- Employees with flexible time policies who need to work a target number of hours per day or week.
- People whose office presence correlates strongly with a known Wi-Fi network.
- Users who want accurate personal time tracking without timesheets, enterprise workflows, or cloud accounts.

Secondary users:

- Hybrid workers who sometimes work from home and need manual tracking.
- Users who leave for lunch, meetings, errands, or travel and want easy correction.

## 4. Core Requirements

### 4.1 Presence Detection

Tickflow must support configuring one or more office Wi-Fi identifiers. The app should infer work presence from Android connectivity and Wi-Fi state instead of running a constant polling loop.

Required behavior:

- Detect when the device is connected to a configured office Wi-Fi.
- Detect when the device disconnects from the configured office Wi-Fi.
- Start a work session when an arrival event is accepted by the active rule set.
- End or pause a work session when a departure event is accepted by the active rule set.
- Recover gracefully after app process death, phone reboot, connectivity callback loss, or service restart.
- Avoid duplicate sessions from repeated connectivity events.
- Record the source of each session: `wifi`, `manual`, `inferred`, `edited`, or `system_recovered`.

Important implementation note:

- Android may require location-related permission to access SSID/BSSID information. The product must explain this permission honestly as required for identifying the configured work Wi-Fi, not for continuous location tracking.

### 4.2 Foreground Tracking

Tickflow is persistent, background-aware, and timing-sensitive. Active automatic tracking must run through a foreground service with a persistent notification.

Required behavior:

- Start the foreground service while an automatic or manual work session is active.
- Keep notification content concise and useful, for example: `Tickflow tracking workday - 6h 23m worked`.
- Include notification actions for at least stop/pause and open app.
- Keep the service resilient to process recreation.
- Do not use WorkManager for live second-by-second or minute-by-minute tracking.

### 4.3 WorkManager Reliability Jobs

WorkManager is for resilient background maintenance, not live tracking.

Required jobs:

- Midnight/day rollover.
- Recalculate day balances.
- Recover notification/service state after reboot or app update.
- Retry deferred local maintenance tasks.
- Future backup/export jobs if added.

### 4.4 Time Model and Carry-over

Tickflow must model time as sessions and derived daily balances.

Core data concepts:

| Concept | Purpose |
| --- | --- |
| `WorkSession` | Atomic interval of work time, with start/end instants, source, confidence, and correction metadata. |
| `DayBalance` | Derived or cached daily summary containing target minutes, actual minutes, carry-over minutes, and resulting credit/deficit. |
| `WorkSchedule` | User-defined target workdays and daily target duration. |
| `PresenceEvent` | Internal event representing arrival, departure, reconnect, disconnect, manual start, manual stop, or correction offer. |
| `TrackingState` | Current state exposed to UI: idle, tracking, paused, uncertain, or needs_review. |

Carry-over rules:

- If `actualMinutes > targetMinutes`, the difference becomes credit.
- If `actualMinutes < targetMinutes`, the difference becomes deficit.
- Carry-over affects the next eligible workday's effective target.
- Non-workdays should not automatically consume or generate carry-over unless the user manually tracks time and chooses to apply it.
- Edited sessions must trigger recalculation of affected `DayBalance` records.
- Time calculations must be timezone-aware and handle daylight saving transitions.

### 4.5 Predictive Leave Time

The home screen must answer the user's most important question: when can I leave?

Required calculation inputs:

- Today's target minutes.
- Carry-over balance entering today.
- Completed work minutes today.
- Active session elapsed time, if tracking.
- User schedule and workday eligibility.
- Optional excluded break rules, if added later.

Required UI outputs:

- Worked time today.
- Remaining time or overage.
- Expected leave time when tracking.
- Carry-over credit or deficit.
- Clear state when not tracking or when more input is needed.

### 4.6 Manual Tracking and Corrections

Required behavior:

- Manually start work from the app.
- Manually stop work from the app and notification.
- Create, edit, and delete work sessions.
- Mark a gap between office departure and return as work with one tap when Tickflow has enough confidence to suggest it.
- Preserve an audit trail field such as `corrected` or `editedAt` so manually changed records can be distinguished from raw automatic records.

### 4.7 Settings

Minimum settings:

- Office Wi-Fi configuration.
- Daily target duration.
- Workdays.
- Notification behavior.
- Carry-over behavior.
- Privacy/data controls.
- Manual export/delete local data, at least by a later milestone.

## 5. Architecture Principles

| Principle | Decision |
| --- | --- |
| Android-native first | Build as a native Android app using Kotlin and Jetpack Compose. |
| Offline-first | Local data is authoritative. No account or network dependency is required for core features. |
| Unidirectional data flow | UI observes immutable state from ViewModels and sends user intents/events downward. |
| Layered architecture | Separate UI, ViewModel, domain/use cases, repositories, data sources, service layer, and platform adapters. |
| Event-driven presence | React to Android network/connectivity events rather than polling. |
| Durable domain model | Store work sessions as source-of-truth records. Treat day balances as derived or recalculable. |
| Explicit uncertainty | Ambiguous detection should produce reviewable suggestions instead of silently corrupting time records. |
| Minimal permissions | Request only permissions required for the chosen feature set and explain them in product language. |
| Battery-aware by design | Active tracking uses foreground service; maintenance uses WorkManager; no tight loops. |
| Testable core | Time arithmetic, carry-over, detection rules, and state reducers must be unit-testable without Android framework dependencies. |

## 6. Approved Tech Stack

| Layer | Choice |
| --- | --- |
| Language | 100% Kotlin |
| UI | Jetpack Compose |
| Design system | Material 3 |
| Architecture | MVVM + UDF |
| Async/reactive | Kotlin Coroutines + Flow |
| Persistence | Room for structured records; DataStore for preferences/settings |
| Dependency injection | Hilt |
| Navigation | Navigation Compose |
| Background live tracking | Foreground Service |
| Resilient background work | WorkManager |
| Notifications | Android notification APIs with Material-style notification design |
| Charts/stats | Compose Canvas initially; Vico is acceptable if richer charts are needed |
| Widgets, later | Glance App Widgets |
| Wearables, later | Wear OS Compose |
| Build | Gradle Kotlin DSL |

## 7. UX Direction

Tickflow should feel:

- Ambient: tracking happens quietly and visibly only when useful.
- Predictive: the UI emphasizes leave time and remaining work, not raw logs.
- Humane: carry-over is presented as credit/deficit in plain language.
- Minimal: one primary screen should answer today's state at a glance.
- Trustworthy: automatic detection is visible, editable, and never opaque.
- Privacy-first: local-only behavior is clear and easy to understand.

Home screen hierarchy:

1. Primary visual: dynamic progress ring or equivalent progress representation for today's work.
2. Primary answer: `Leave at 16:42` or `Done for today`.
3. Secondary facts: worked today, remaining time, carry-over balance.
4. Contextual action: start/stop/edit/review suggestion.
5. Recent session list or compact timeline.

## 8. Non-goals

The following are explicitly out of scope unless this document is intentionally revised:

| Non-goal | Reason |
| --- | --- |
| Cloud account system | Core value is local, private, zero-account tracking. |
| Team management or employer admin portal | Tickflow is a personal assistant, not enterprise workforce software. |
| Payroll, invoicing, or legal timesheet compliance | These require audit/compliance guarantees beyond the product scope. |
| Continuous GPS tracking | Too invasive and battery-expensive for the core promise. Optional geofencing may be considered later only as a supporting signal. |
| Real-time server sync | Not needed for MVP and conflicts with offline-first simplicity. |
| Cross-platform app | Android-native quality is the priority. |
| Web dashboard | Not required for the primary use case. |
| Widgets in MVP | Useful later, but not required before the core tracking loop is reliable. |
| Wear OS support in MVP | Future extension only. |
| AI/cloud inference | Confidence should initially come from deterministic local signals. |

## 9. Hard Constraints

- The app must be Android-native and Kotlin-first.
- The app must work without a network account.
- Live tracking must not depend on WorkManager.
- The app must not run a constant Wi-Fi polling loop.
- Time records must be locally persisted.
- The user must be able to correct automatic tracking mistakes.
- The project must keep domain time calculations testable outside Android framework code.
- Any permission that can reveal location or network identity must be clearly justified in UI copy and documentation.
- The app must avoid sending work sessions, Wi-Fi identifiers, or schedules to third-party services by default.

## 10. Success Criteria

MVP is successful when all of the following are true:

| Area | Acceptance Criteria |
| --- | --- |
| Setup | User can configure an office Wi-Fi and work schedule. |
| Automatic tracking | Connecting to configured office Wi-Fi starts a session; leaving it ends or pauses the session. |
| Manual tracking | User can start/stop and edit sessions manually. |
| Persistence | Sessions survive app restart, process death, and device reboot recovery path. |
| Foreground service | Active tracking displays a persistent notification with current worked time and useful actions. |
| Carry-over | Daily credit/deficit is calculated and applied to the next eligible workday. |
| Prediction | Home screen displays expected leave time while tracking. |
| Correction | User can review and apply at least one common automatic-tracking correction flow. |
| Privacy | Core features work without account creation or cloud sync. |
| Tests | Domain time/carry-over logic and core state transitions have automated tests. |

## 11. Quality Standards

### Reliability

- No duplicate overlapping sessions from repeated connectivity callbacks.
- No lost active session after app restart.
- Day rollover must be deterministic and recalculable.
- Manual edits must recompute affected summaries.

### Performance and Battery

- No tight polling loops.
- Foreground service should update notification at a reasonable cadence, not every frame.
- Database queries for the home screen should be bounded to relevant date ranges.
- Compose UI should avoid unnecessary recomposition from high-frequency ticking.

### Security and Privacy

- Store data locally by default.
- Do not include analytics, crash reporting, or telemetry until there is an explicit privacy decision.
- Treat SSID/BSSID, schedule, and work sessions as sensitive personal data.
- Provide local data deletion before any public release.

### Maintainability

- Domain logic must not be embedded inside Composables or Android service classes.
- Keep platform APIs behind adapters/gateways.
- Prefer small use cases with explicit inputs/outputs for time calculations.
- Keep generated or derived state recalculable from source records.

### UX Quality

- The home screen must answer the current workday state without requiring navigation.
- Empty, permission-denied, and uncertain states must be designed intentionally.
- The app must explain automatic tracking behavior in plain language.
- Notifications must be useful but not noisy.

## 12. Canonical MVP Scope

MVP includes:

- Android project scaffold.
- Compose app shell.
- Local persistence with Room and DataStore.
- Hilt dependency graph.
- Office Wi-Fi setup.
- Work schedule setup.
- Automatic Wi-Fi-based tracking through foreground service.
- Manual start/stop/edit.
- Today home screen with progress and predictive leave time.
- Carry-over calculation.
- Midnight/day rollover job.
- Basic notification actions.
- Unit tests for domain logic.
- Instrumentation or manual verification checklist for service/presence behavior.

MVP excludes:

- Cloud sync.
- Accounts.
- Employer/team features.
- Wear OS.
- Widgets.
- Public analytics.
- Payroll exports beyond optional local export.
