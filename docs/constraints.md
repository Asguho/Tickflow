# Tickflow Constraints

Last updated: 2026-05-11

## 1. Technical Constraints

| Area | Constraint |
| --- | --- |
| Platform | Android native app. No cross-platform framework for MVP. |
| Language | Kotlin only for production app code. |
| UI | Jetpack Compose only. No new XML view screens. |
| Architecture | MVVM with unidirectional data flow. |
| Persistence | Room for structured work records; DataStore for settings/preferences. |
| Async | Kotlin Coroutines and Flow. |
| Dependency injection | Hilt. |
| Navigation | Navigation Compose. |
| Live tracking | Foreground service. |
| Reliable deferred work | WorkManager. |
| Build scripts | Gradle Kotlin DSL. |
| Core logic | Time, carry-over, prediction, and state transitions must be testable as pure Kotlin. |

## 2. Android Platform Constraints

- Wi-Fi SSID/BSSID access can require location-related permissions depending on Android version and API behavior.
- Background execution is restricted; active tracking must use a foreground service.
- WorkManager is not suitable for real-time tracking and must not be used as the active timer mechanism.
- Device reboot, process death, app update, and permission revocation must be treated as normal lifecycle events.
- Notification permission behavior varies by Android version; the app must degrade gracefully if notifications are denied.
- Exact alarms should not be used for live tracking unless a future requirement justifies them and the permission burden is accepted.

## 3. Performance Budgets

These are MVP engineering targets. If a target cannot be met, document the reason in `docs/status.md`.

| Area | Budget / Target |
| --- | --- |
| Foreground notification update cadence | No more than once per minute for normal worked-time display unless actively responding to a state change. |
| Wi-Fi detection | Event-driven through platform callbacks; no continuous polling loop. |
| Home screen data query | Query only relevant date ranges for current display, normally today plus carry-over history needed by the calculator. |
| Cold launch | Target under 1.5 seconds to first meaningful Compose frame on a modern device for release builds. |
| Home recomposition | Avoid per-second full-screen recomposition; derive ticking display at minute granularity where possible. |
| Database operations | Session insert/update/delete should be effectively instant for normal personal datasets; avoid unbounded full-history scans in UI paths. |
| Battery | Active tracking should not materially heat the device or appear as abnormal drain in normal daily use. |
| APK size | Keep dependencies purposeful; do not add heavy libraries for small utility functions. |

## 4. Scalability Requirements

Tickflow is a personal app, not a multi-user backend system. Scalability means handling years of local personal history cleanly.

Minimum local data assumptions:

- 10 years of workdays.
- Up to 20 sessions per day.
- At least 75,000 work session records without UI degradation.
- Multiple configured work Wi-Fi networks in the future, though MVP may start with one.

Data design requirements:

- Queries must filter by date/time range.
- Session timestamps must be indexed.
- Active session lookup must be efficient.
- Day balances should be recalculable from `WorkSession` source records.
- Migrations must preserve user time records.

## 5. Security and Privacy Constraints

| Requirement | Constraint |
| --- | --- |
| Local-first data | Work sessions, schedules, and Wi-Fi identifiers remain on device by default. |
| No account dependency | Core features cannot require sign-in. |
| No silent telemetry | Do not add analytics, crash reporting, or remote logging without an explicit product/privacy decision. |
| Sensitive identifiers | Treat SSID, BSSID, schedule, and work sessions as personal data. |
| Permission honesty | Permission rationale must state why network/location access is needed. |
| Data deletion | Public MVP must include a way to delete/reset local Tickflow data. |
| Export | If export is added, it must be user-initiated and local/share-sheet based unless cloud sync is later approved. |
| Logging | Do not log raw SSID/BSSID, exact schedules, or full session history in production logs. |

Recommended local handling:

- Store only the minimum Wi-Fi identifier data needed for matching.
- Prefer hashing or normalizing identifiers where matching can still work reliably.
- Avoid storing precise physical location unless a future geofence feature is explicitly approved.
- Keep debug logging behind build type checks.

## 6. Compliance and Policy Needs

Tickflow is not intended to be a payroll-compliance system. The app should avoid claims that it satisfies labor law, employer audit, or legal timesheet requirements.

Before public release:

- Review Android foreground service policy requirements for the selected service type.
- Review Android permissions and prominent disclosure requirements for any location-sensitive Wi-Fi access.
- Provide privacy policy text if distributed through a store.
- Ensure data deletion is available.

## 7. Coding Style and Conventions

### Kotlin

- Use idiomatic Kotlin with immutable data classes for state.
- Prefer `val` over `var`.
- Use sealed interfaces/classes for finite state and event hierarchies.
- Use `java.time.Instant`, `LocalDate`, `LocalTime`, `Duration`, and `ZoneId` for time logic.
- Do not represent durations as ambiguous raw integers outside clear type boundaries. If minutes are stored as `Int`, name fields explicitly, for example `targetMinutes`.
- Avoid nullable fields when a sealed state or explicit type communicates the model better.

### Coroutines and Flow

- Repositories may expose `Flow` for observable data.
- ViewModels collect flows and expose UI state through `StateFlow`.
- Do not launch unstructured global coroutines.
- Use injected dispatchers for testable asynchronous code.
- Use coroutine test utilities for deterministic tests.

### Compose

- Composables should be mostly stateless.
- Route-level Composables connect ViewModels to screen Composables.
- Screen Composables receive state and event lambdas.
- Avoid business logic in Composables.
- Provide previews for important UI states where practical.
- Ensure touch targets and text scaling are reasonable.

### Room

- Entities are data-layer types, not domain models.
- Use explicit mappers between entity and domain types.
- Export schemas if the project chooses to enforce migration discipline.
- Add indexes for timestamp/date fields used in queries.
- Avoid destructive migrations after user data exists.

### DataStore

- Use DataStore for settings and preferences, not session history.
- Keep settings schemas explicit.
- Validate settings before use in domain calculations.

### Hilt

- Keep DI modules organized by layer.
- Do not inject Android platform services deep into domain use cases.
- Domain services should depend on interfaces, not Android framework classes.

## 8. Forbidden Patterns

The following are not allowed unless this file is intentionally changed:

| Forbidden Pattern | Reason |
| --- | --- |
| Constant Wi-Fi polling loop | Battery drain and unnecessary platform work. |
| WorkManager as live timer | WorkManager is not designed for real-time tracking. |
| Business logic in Composables | Hard to test and maintain. |
| Business logic in Android service classes | Couples domain behavior to lifecycle/platform code. |
| Cloud sync by default | Violates local-first MVP. |
| Account requirement | Violates core product promise. |
| Raw SQL string construction from user input | Security and correctness risk. |
| Storing work sessions only in preferences | Not queryable or durable enough. |
| Overlapping active sessions | Corrupts totals. |
| Silent destructive data reset | User trust and data loss risk. |
| Production logs containing Wi-Fi identifiers | Privacy risk. |
| Adding analytics SDK by default | Privacy/product decision not approved. |
| XML screens for new UI | Conflicts with Compose-first architecture. |
| RxJava introduction | Coroutines/Flow is the approved async model. |

## 9. Library Constraints

Approved core libraries:

- AndroidX Core.
- AndroidX Lifecycle.
- Jetpack Compose.
- Material 3.
- Navigation Compose.
- Kotlin Coroutines.
- Room.
- DataStore.
- Hilt.
- WorkManager.
- Kotlin serialization only if structured local export or DataStore modeling needs it.
- Vico only if charts become complex enough to justify it.

Avoid for MVP:

- Cross-platform UI frameworks.
- Cloud database SDKs.
- Authentication SDKs.
- Analytics SDKs.
- Heavy charting libraries before actual stats requirements exist.
- Background scheduling libraries that duplicate WorkManager.

## 10. Product Scope Constraints

MVP must remain focused on:

- Setup.
- Automatic Wi-Fi tracking.
- Manual tracking.
- Predictive leave time.
- Carry-over.
- Local persistence.
- Foreground notification.
- Basic correction/editing.

Do not start these before MVP reliability is working:

- Wear OS.
- Glance widgets.
- Cloud sync.
- Team/admin features.
- Payroll exports.
- AI inference.
- Multi-platform clients.
- Public API.

## 11. Testing Constraints

Minimum required automated tests before MVP release:

- Domain time calculation tests.
- Carry-over tests.
- Predictive leave time tests.
- Tracking state reducer tests.
- Repository persistence tests for session CRUD and active session recovery.

Minimum required manual tests before MVP release:

- Fresh install setup.
- Permission granted and denied flows.
- Manual start/stop.
- Wi-Fi arrival/departure.
- Active notification actions.
- App process death during active tracking.
- Device reboot recovery path where possible.
- Day rollover.
- Session edit recalculation.
- Local data deletion.

## 12. Documentation Constraints

- Keep `docs/prompt.md` as the product and architecture source of truth.
- Keep `docs/plan.md` updated when milestones or scope change.
- Keep `docs/constraints.md` updated when stack, policy, or forbidden patterns change.
- Keep `docs/status.md` updated after meaningful implementation progress.
- Do not let README, comments, and docs diverge on core architecture decisions.
