# Tickflow Execution Plan

Last updated: 2026-05-11

## 1. Current Starting Point

The repository currently contains project idea/context files and durable planning documents. The Android application has not yet been scaffolded.

Current known files:

- `IDEA.md`: concise product idea.
- `context/context1.md`: expanded product and architecture context.
- `context/context2.md`: currently empty.
- `docs/prompt.md`: single source of truth.
- `docs/plan.md`: execution plan.
- `docs/constraints.md`: engineering constraints.
- `docs/status.md`: current project status.

## 2. Milestone Overview

| Phase | Name | Primary Goal | Definition of Done |
| --- | --- | --- | --- |
| 0 | Product and Architecture Memory | Establish durable alignment documents. | `docs/prompt.md`, `docs/plan.md`, `docs/constraints.md`, and `docs/status.md` exist and are internally consistent. |
| 1 | Android Foundation | Create a buildable modern Android project. | App builds locally, launches a Compose shell, uses Gradle Kotlin DSL, Hilt, Compose, Room, DataStore, and test setup. |
| 2 | Domain Model and Time Engine | Implement testable work session and carry-over logic. | Domain unit tests cover session duration, day balance, carry-over, predictive leave time, timezone boundaries, and edits. |
| 3 | Persistence and Settings | Store sessions, schedules, and office Wi-Fi settings locally. | Room and DataStore repositories are implemented with tests or verifiable fakes. |
| 4 | Tracking Runtime | Implement foreground service and Wi-Fi presence handling. | Configured Wi-Fi arrival/departure starts/stops sessions and notification reflects active state. |
| 5 | Core UI | Build the user-facing home, setup, session edit, and settings flows. | User can configure, track, view, edit, and understand current workday state. |
| 6 | Reliability and Recovery | Add rollover jobs, reboot recovery, duplicate prevention, and correction flows. | Tracking survives realistic lifecycle interruptions and ambiguous gaps are reviewable. |
| 7 | Polish, QA, and Release Readiness | Hardening, performance, privacy, and release packaging. | MVP acceptance criteria pass on target devices/emulators with documented verification. |

## 3. Suggested File and Folder Structure

Use a standard Android app layout with clear package boundaries.

```text
Tickflow/
  settings.gradle.kts
  build.gradle.kts
  gradle/
  app/
    build.gradle.kts
    src/
      main/
        AndroidManifest.xml
        java/com/tickflow/app/
          TickflowApplication.kt
          MainActivity.kt
          core/
            time/
              ClockProvider.kt
              DateTimeExtensions.kt
            model/
              WorkSession.kt
              DayBalance.kt
              WorkSchedule.kt
              TrackingState.kt
              PresenceEvent.kt
            result/
              AppResult.kt
            permissions/
              PermissionExplainer.kt
          data/
            local/
              TickflowDatabase.kt
              entity/
                WorkSessionEntity.kt
                DayBalanceEntity.kt
              dao/
                WorkSessionDao.kt
                DayBalanceDao.kt
            datastore/
              SettingsDataStore.kt
              SettingsModels.kt
            repository/
              WorkSessionRepositoryImpl.kt
              SettingsRepositoryImpl.kt
          domain/
            repository/
              WorkSessionRepository.kt
              SettingsRepository.kt
            usecase/
              CalculateDayBalanceUseCase.kt
              CalculateCarryOverUseCase.kt
              PredictLeaveTimeUseCase.kt
              StartTrackingUseCase.kt
              StopTrackingUseCase.kt
              ApplySessionCorrectionUseCase.kt
            service/
              PresenceDecisionEngine.kt
              TrackingStateReducer.kt
          platform/
            connectivity/
              WifiPresenceMonitor.kt
              AndroidConnectivityObserver.kt
            notification/
              TrackingNotificationFactory.kt
            service/
              TrackingForegroundService.kt
            worker/
              MidnightRolloverWorker.kt
              RebootRecoveryWorker.kt
            receiver/
              BootCompletedReceiver.kt
          ui/
            navigation/
              TickflowNavHost.kt
              Routes.kt
            theme/
              Color.kt
              Theme.kt
              Type.kt
            home/
              HomeRoute.kt
              HomeViewModel.kt
              HomeScreen.kt
              WorkProgressRing.kt
            setup/
              SetupRoute.kt
              SetupViewModel.kt
              SetupScreen.kt
            sessions/
              SessionListRoute.kt
              SessionEditorRoute.kt
              SessionViewModel.kt
            settings/
              SettingsRoute.kt
              SettingsViewModel.kt
      test/
        java/com/tickflow/app/
          domain/
          data/
      androidTest/
        java/com/tickflow/app/
          ui/
          service/
  docs/
    prompt.md
    plan.md
    constraints.md
    status.md
```

If the project later adopts multiple Gradle modules, split by dependency direction:

- `:core:model`
- `:core:domain`
- `:data:local`
- `:platform:android`
- `:app`

Do not introduce multi-module complexity before the single-module implementation becomes hard to navigate.

## 4. Phase 0: Product and Architecture Memory

### Tasks

| Task | Details | Status |
| --- | --- | --- |
| Capture vision | Convert the idea/context files into a stable product source of truth. | Done |
| Define execution plan | Establish milestones, task ordering, folder structure, and risks. | Done |
| Define constraints | Document technical, security, performance, and style boundaries. | Done |
| Initialize status | Record current repository state and next milestone. | Done |

### Verification

- All four durable docs exist in `docs/`.
- The docs agree on stack, MVP scope, non-goals, and current status.
- Future agents can understand the project without reading the original chat.

## 5. Phase 1: Android Foundation

### Goals

Create a buildable Android project that can support the full Tickflow architecture.

### Tasks

| Task | Details | Depends On |
| --- | --- | --- |
| Generate Android project | Create Gradle Kotlin DSL project with `app` module. | Phase 0 |
| Configure Kotlin and Compose | Enable Compose compiler, Material 3, Navigation Compose. | Project scaffold |
| Configure Hilt | Add Hilt plugin, application class, base DI module. | Project scaffold |
| Configure Room | Add Room dependencies, compiler/KSP, placeholder database. | Project scaffold |
| Configure DataStore | Add preferences/proto decision and dependency. | Project scaffold |
| Configure WorkManager | Add WorkManager dependency and Hilt worker integration if needed. | Hilt |
| Configure tests | Add JUnit, coroutine test, Turbine if used, AndroidX test, Compose UI test. | Project scaffold |
| Add app shell | `MainActivity`, theme, nav host, placeholder home screen. | Compose |
| Add CI-ready commands | Document build/test commands in README or docs. | Build works |

### Definition of Done

- `./gradlew assembleDebug` succeeds.
- `./gradlew testDebugUnitTest` succeeds.
- The app launches to a Compose home shell.
- Hilt application setup works.
- No core domain logic is placed in UI or Android service classes.

### Verification Steps

- Run unit tests.
- Build debug APK.
- Launch on emulator.
- Confirm no runtime crash on first launch.

## 6. Phase 2: Domain Model and Time Engine

### Goals

Build the pure Kotlin heart of Tickflow before binding it to Android platform APIs.

### Tasks

| Task | Details | Depends On |
| --- | --- | --- |
| Define domain models | `WorkSession`, `DayBalance`, `WorkSchedule`, `TrackingState`, `PresenceEvent`. | Phase 1 |
| Define source types | `wifi`, `manual`, `inferred`, `edited`, `system_recovered`. | Domain models |
| Implement duration calculation | Calculate session duration safely for closed and active sessions. | Domain models |
| Implement day slicing | Split sessions across local date boundaries when needed. | Duration calculation |
| Implement day balance | Calculate target, actual, credit/deficit, and carry-over. | Day slicing |
| Implement predictive leave time | Return expected leave instant or completion state. | Day balance |
| Implement state reducer | Convert events and current state into new `TrackingState`. | Domain models |
| Implement correction logic | Represent reviewable gaps and apply accepted corrections. | State reducer |
| Add deterministic clock | Use injectable clock provider for tests. | Phase 1 |

### Required Unit Tests

| Test Area | Cases |
| --- | --- |
| Session duration | Closed session, active session, zero-length rejection, invalid end-before-start. |
| Day boundary | Session crosses midnight, daylight saving transition, timezone change behavior. |
| Carry-over | Credit day, deficit day, exact target day, non-workday handling. |
| Predictive leave | Active tracking, already complete, not tracking, deficit carry-in, credit carry-in. |
| State reducer | Arrival while idle, repeated arrival, departure while tracking, manual stop, recovery event. |
| Correction | Gap suggestion created, accepted correction becomes work session, rejected correction ignored. |

### Definition of Done

- Domain tests pass without Android instrumentation.
- Time calculations use `java.time` types and explicit `ZoneId`.
- Use cases have clear inputs and outputs.
- No database, service, or Compose dependencies leak into domain logic.

## 7. Phase 3: Persistence and Settings

### Goals

Persist source records locally and expose reactive streams to the rest of the app.

### Tasks

| Task | Details | Depends On |
| --- | --- | --- |
| Implement Room entities | Work sessions and optionally cached day balances. | Phase 2 models |
| Implement DAOs | Insert/update/delete sessions, query by date range, query active session. | Entities |
| Implement migrations policy | Start with schema version 1 and document migration expectations. | Database |
| Implement repositories | Map entities to domain models and expose Flow streams. | DAOs |
| Implement settings storage | Work schedule, target duration, Wi-Fi identifiers, notification preferences. | DataStore |
| Decide identifier storage | Prefer local-only storage; consider hashing BSSID/SSID if feasible without breaking matching. | Settings |
| Add repository tests | Use in-memory Room and DataStore test setup. | Repositories |

### Definition of Done

- Sessions can be created, updated, deleted, and queried.
- Active session can be restored from storage.
- Settings persist across process restart.
- Repository APIs return domain models, not Room entities.

### Verification Steps

- Run unit/local persistence tests.
- Manually create and edit session through a debug path or temporary test harness if UI is not ready.
- Confirm database schema is checked in if Room schema export is enabled.

## 8. Phase 4: Tracking Runtime

### Goals

Connect Android platform events to domain tracking behavior.

### Tasks

| Task | Details | Depends On |
| --- | --- | --- |
| Implement Wi-Fi presence monitor | Wrap `ConnectivityManager`, network callbacks, and Wi-Fi info access. | Phase 3 settings |
| Implement permission flow support | Surface whether required Wi-Fi/network permissions are available. | UI setup or platform adapter |
| Implement foreground service | Start, stop, observe tracking state, update notification. | Domain and repositories |
| Implement notification | Show worked time, expected leave time, and stop/open actions. | Foreground service |
| Implement service commands | Start tracking, stop tracking, refresh state, recover active session. | Foreground service |
| Handle duplicate events | Debounce or idempotently process repeated connectivity callbacks. | Presence engine |
| Handle process death | Restore active session and notification from persisted state. | Persistence |
| Add reboot receiver | Schedule recovery worker or service check after boot when appropriate. | WorkManager |

### Definition of Done

- Connecting to configured Wi-Fi starts a persisted session.
- Disconnecting ends or pauses the session according to current MVP rule.
- Active tracking displays a foreground notification.
- Notification actions work.
- Repeated callbacks do not create overlapping duplicate sessions.

### Verification Steps

- Manual emulator/device test with Wi-Fi changes where possible.
- Use adb or dependency-injected fake monitor for repeatable state tests.
- Kill and relaunch app during active tracking; verify state recovery.
- Reboot test path if available.

## 9. Phase 5: Core UI

### Goals

Build the primary user workflows in Compose.

### Screens

| Screen | Purpose |
| --- | --- |
| Home | Current workday progress, expected leave time, tracking state, primary action. |
| Setup | Initial office Wi-Fi and work schedule configuration. |
| Session List | Review recent sessions and day totals. |
| Session Editor | Create, edit, delete, or correct work sessions. |
| Settings | Work schedule, target hours, Wi-Fi configuration, notifications, privacy controls. |

### Tasks

| Task | Details | Depends On |
| --- | --- | --- |
| Define UI state models | Stable immutable state per screen. | Domain/repositories |
| Implement HomeViewModel | Combine settings, sessions, active tracking, prediction. | Phase 3/4 |
| Implement progress ring | Compose visual for today worked/remaining. | Home UI |
| Implement setup flow | Configure target hours, workdays, office Wi-Fi. | Settings repository |
| Implement manual controls | Start, stop, edit session. | Tracking use cases |
| Implement correction review | Show suggested gap and one-tap accept/reject. | Correction logic |
| Implement settings | Update schedule, notifications, Wi-Fi identifiers, data controls. | DataStore |
| Add empty/error states | Permission denied, no Wi-Fi configured, not a workday, uncertain detection. | UI state |

### Definition of Done

- A new user can complete setup.
- Home screen displays accurate current state.
- User can manually start and stop tracking.
- User can edit sessions and see recalculated totals.
- Permission and uncertainty states are understandable.

### Verification Steps

- Compose previews for core states.
- Compose UI tests for critical flows if feasible.
- Manual walkthrough from fresh install to first tracked day.
- Verify text fits on common small and large screen sizes.

## 10. Phase 6: Reliability and Recovery

### Goals

Make the MVP dependable in real Android conditions.

### Tasks

| Task | Details | Depends On |
| --- | --- | --- |
| Midnight rollover worker | Recalculate previous day and prepare current day. | Persistence/domain |
| Recovery worker | Reconcile active session and notification after reboot/app update. | WorkManager/service |
| Session overlap guard | Enforce no overlapping active sessions. | Repositories/domain |
| Gap detection | Detect left-and-returned intervals that may be work. | Presence event history |
| Correction offers | Persist or derive reviewable suggestions. | Gap detection |
| Battery review | Check service update cadence and unnecessary wakeups. | Tracking runtime |
| Permission degradation | Define behavior when permissions are denied/revoked. | Runtime/UI |

### Definition of Done

- Rollover works across date boundaries.
- Active session recovery is reliable.
- Duplicate/overlap prevention is enforced.
- Common ambiguous gaps can be reviewed and corrected.
- Permission loss does not crash the app or corrupt data.

### Verification Steps

- Unit tests for overlap and rollover.
- Manual timezone/day-boundary tests with injected clock where possible.
- Reboot/process-death test checklist.
- Battery sanity check using Android Studio profiler or system battery stats.

## 11. Phase 7: Polish, QA, and Release Readiness

### Goals

Prepare the app for a credible MVP release or dogfood build.

### Tasks

| Task | Details | Depends On |
| --- | --- | --- |
| Privacy copy | Explain local storage, Wi-Fi detection, permissions, and data deletion. | Core flows |
| Data deletion | Add local delete/reset control. | Persistence |
| Export option | Optional local export to CSV/JSON if time permits. | Persistence |
| Design polish | Material 3 theme, typography, haptics, animation restraint. | UI |
| Accessibility pass | Content descriptions, dynamic type, contrast, touch targets. | UI |
| Performance pass | Cold start, query bounds, recomposition checks, notification cadence. | Full app |
| Test pass | Unit, instrumentation where available, manual lifecycle checklist. | Full app |
| Release config | App icon, package name, versioning, signing notes, ProGuard/R8. | Build |

### Definition of Done

- MVP acceptance criteria in `docs/prompt.md` pass.
- No known critical data-loss, tracking, or crash bugs remain.
- Privacy-sensitive behavior is documented and visible to users.
- Release build can be produced.

## 12. Dependencies and Ordering

Critical path:

1. Scaffold project.
2. Implement domain time engine.
3. Implement persistence.
4. Implement tracking runtime.
5. Implement UI over real data.
6. Harden lifecycle and recovery.
7. Polish and release.

Do not build advanced UI before the domain and persistence contracts exist. Do not wire Wi-Fi service behavior directly to UI state without repository/domain boundaries. Do not add widgets, Wear OS, cloud sync, or analytics before MVP tracking reliability is proven.

## 13. Risk Register

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Android Wi-Fi identifier restrictions | Automatic detection may need permissions or may behave differently by OS version. | Create a platform adapter early; test on target Android versions; provide honest permission copy; support manual tracking fallback. |
| Background execution limits | Tracking may stop if service is not correctly implemented. | Use foreground service for active tracking; persist active session; add recovery paths. |
| Battery drain | Poor implementation could harm trust. | Event-driven presence, no polling loops, bounded notification cadence, profiler review. |
| Time calculation bugs | Carry-over and leave predictions become untrustworthy. | Keep pure domain logic heavily unit tested with injected clock and timezone cases. |
| Duplicate/overlapping sessions | Totals become inflated. | Enforce idempotent event handling and repository-level overlap checks. |
| Permission denial | Automatic tracking cannot work. | Design degraded state and manual tracking path. |
| User trust | Silent automatic mistakes may feel creepy or unreliable. | Show source/confidence, allow edits, and use review prompts for uncertainty. |
| Scope creep | MVP may stall. | Keep widgets, Wear OS, cloud, enterprise features, and analytics out of MVP. |

## 14. Verification Strategy

| Layer | Verification |
| --- | --- |
| Domain | Fast unit tests for time arithmetic, carry-over, prediction, state transitions. |
| Data | DAO/repository tests with in-memory Room and test DataStore. |
| Platform | Adapter tests with fakes where possible; manual device checks for actual Wi-Fi behavior. |
| Service | Instrumentation/manual tests for foreground notification, actions, process death, reboot recovery. |
| UI | Compose previews, UI tests for primary flows, manual responsive/accessibility checks. |
| End-to-end | Fresh install setup, connect/disconnect, manual session, edit, rollover, correction, data deletion. |

## 15. MVP Manual Acceptance Checklist

- Fresh install opens setup.
- User can set daily target duration and workdays.
- User can configure current Wi-Fi as office Wi-Fi.
- Home shows not-tracking state after setup.
- Manual start creates active session and notification.
- Manual stop closes session and updates total.
- Wi-Fi arrival starts session when configured and permission is granted.
- Wi-Fi departure closes or pauses session.
- Active notification shows useful worked-time status.
- Home predicts leave time during active tracking.
- Editing a session recalculates today and carry-over.
- Day rollover produces correct next-day carry-over.
- App restart during active tracking restores state.
- Permission denial shows degraded manual mode, not a crash.
- User can delete local data.
