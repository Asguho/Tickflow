# Tickflow Status

Last updated: 2026-05-12

## 1. Current Project Status

Tickflow now has a buildable single-module Android application scaffold and a first-pass MVP implementation across the planned architecture layers.

The app is no longer only planning documents. It contains Gradle Kotlin DSL build files, an Android `app` module, Kotlin/Compose UI, Hilt setup, Room persistence, DataStore settings, WorkManager workers, a foreground tracking service, an event-driven Wi-Fi presence adapter, domain time logic, and unit tests for the core time engine.

This is not yet release-Done. The remaining work is primarily deeper automated Android runtime coverage, reboot/long-running lifecycle validation, final accessibility/store polish, and production signing outside git.

## 2. Completed

| Item | Status | Notes |
| --- | --- | --- |
| Durable source of truth | Complete | `docs/prompt.md`, `docs/plan.md`, `docs/constraints.md`, and this status file are present. |
| Android project scaffold | Complete | Gradle Kotlin DSL root project and `app` module exist. |
| Build tooling | Complete | Gradle wrapper generated. Local verification used Android SDK API 36 and JDK 17. |
| Compose shell | Complete | `MainActivity`, Material 3 theme, Navigation Compose, home/setup/sessions/settings routes. |
| Dependency injection | Complete | Hilt application class and singleton bindings are configured. |
| Persistence foundation | Complete | Room database, work-session/day-balance entities, DAOs, mappers, repository implementation. |
| Repository persistence tests | Complete | In-memory Room tests cover CRUD, active recovery, overlap prevention, updates, and deletion. |
| Settings storage | Complete | DataStore preferences store schedule, Wi-Fi identifier, notification setting, carry-over setting, and rejected correction ids. |
| Settings repository tests | Complete | DataStore-backed tests cover defaults, updates, validation, rejected correction ids, and clear/reset. |
| Domain model and time engine | Complete | Work sessions, day balances, schedules, tracking state, presence events, day slicing, carry-over, leave prediction, reducer, correction suggestion logic. |
| Tracked-day workday behavior | Complete | Any day with a traced session counts as a workday for target, prediction, and carry-over calculations; unscheduled days without traced time do not consume carry-over. |
| Foreground tracking service | Implemented | Manual/Wi-Fi commands start and stop persisted sessions and update a persistent notification at minute cadence. |
| Wi-Fi presence adapter | Implemented | Connectivity callback adapter checks configured SSID/BSSID without a polling loop when permissions allow. |
| Presence coordinator | Implemented | App process observes Wi-Fi presence and maps arrival/departure to persisted tracking changes and service commands. |
| Presence processor tests | Complete | Unit tests cover repeated Wi-Fi arrival/departure handling with fake tracking and service controller dependencies. |
| Notification tests | Complete | Robolectric tests cover notification channel creation, worked-time text, ongoing state, stop action, and content intent. |
| WorkManager recovery and rollover | Implemented | Reboot recovery checks active sessions; rollover worker recalculates recent balances and is scheduled daily after midnight. |
| Core UI flows | Implemented | Setup, home status, manual start/stop, session list/editor, correction review, settings, and local data deletion. |
| Manual QA checklist | Complete | `docs/manual-qa.md` documents device-only setup, permission, tracking, Wi-Fi, recovery, and privacy checks. |
| Privacy notes | Complete | `docs/privacy.md` documents local data, permissions, export/deletion, and lack of telemetry. |
| Local CSV export | Complete | Settings can export sessions through Android's share sheet; CSV formatting is unit-tested. |
| Release notes | Complete | `docs/release.md` documents build outputs, signing expectations, and store-readiness checks. |
| Launcher icon resources | Complete | Adaptive, round, and monochrome launcher icon resources are configured. |
| Optional release signing | Complete | Release builds use signing environment variables when present and remain unsigned otherwise. |
| Domain tests | Complete | Unit tests cover duration, invalid sessions, midnight slicing, DST elapsed time, carry-over, prediction, reducer idempotency, and correction suggestion. |
| Traced-day tests | Complete | Domain tests cover tracked time on unscheduled days counting as workdays and unscheduled days without tracked time preserving carry-over. |
| Runtime edge-case tests | Complete | Repository tests cover zero-length active-session stop cleanup to avoid invalid sessions from immediate disconnects. |

## 3. Verification Completed

Commands run successfully on 2026-05-11:

```bash
JAVA_HOME=$PWD/.jdk/jdk17 ./gradlew testDebugUnitTest
JAVA_HOME=$PWD/.jdk/jdk17 ./gradlew assembleDebug
JAVA_HOME=$PWD/.jdk/jdk17 ./gradlew testDebugUnitTest assembleDebug
JAVA_HOME=$PWD/.jdk/jdk17 ./gradlew testDebugUnitTest assembleDebug assembleRelease
```

Notes:

- The host environment only had JDK 26, which failed AGP's Java image transform. A local JDK 17 was installed under ignored `.jdk/` for verification.
- A local Android SDK was installed under ignored `.android-sdk/`, and ignored `local.properties` points Gradle at that SDK.
- Release APK packaging passes after removing the default WorkManager startup initializer for the app's Hilt-backed `Configuration.Provider`.
- Local Robolectric tests are pinned to SDK 35 while the app still compiles/targets SDK 36.
- Build emits Gradle deprecation noise about future Gradle 10 compatibility, but build and tests pass.

## 4. In Progress

| Item | Status | Notes |
| --- | --- | --- |
| MVP runtime hardening | In progress | Manual Pixel 10 / Android 16 validation passed for manual tracking, notification stop, Wi-Fi disconnect/reconnect, and active-session persistence. Reboot and long-running lifecycle validation remain. |
| Reliability jobs | In progress | Reboot recovery and scheduled rollover are implemented; manual lifecycle validation is still pending. |
| Correction workflow | In progress | Home can review/accept/reject derived short gaps; rejected suggestions are persisted in settings. |
| Session editing | In progress | Sessions can be listed by date and added, edited, or deleted for arbitrary dates. |

## 5. Not Started or Not Complete

| Area | Notes |
| --- | --- |
| Emulator/device launch verification | Physical Pixel 10 device verification completed on 2026-05-12 with debug APK installed over existing app data. |
| Instrumentation tests | No Android instrumentation tests have been added yet. |
| Repository persistence tests | Complete for work-session repository and settings repository. |
| Foreground service lifecycle tests | Presence event processing has fake-based unit coverage; manual notification/service validation passed on device. Instrumentation coverage is still missing. |
| Wi-Fi automatic tracking manual test | Passed on Pixel 10 using office Wi-Fi `TheLoge`; disconnect closed Wi-Fi state and reconnect started a persisted Wi-Fi session with notification. |
| Correction review persistence | Rejected correction suggestions persist locally in DataStore. |
| Robust day rollover | Worker recalculates recent balances; manual lifecycle validation still pending. |
| Export | User-initiated CSV export is implemented through Android share sheet. |
| Release polish | Privacy notes, basic accessibility semantics, CSV export, launcher icon resources, release notes, optional env-based signing, and release APK packaging are implemented; store listing and manual QA remain. |

## 6. Current Acceptance State

| MVP Acceptance Criterion | Current State |
| --- | --- |
| Configure office Wi-Fi and target duration | Office Wi-Fi and target duration are configurable; explicit workday selection is intentionally not required because traced-time days drive workday eligibility. |
| Automatic Wi-Fi tracking | Implemented through connectivity callback and coordinator; manual Pixel validation passed on `TheLoge`. |
| Manual start/stop/edit | Start/stop and date-selectable session add/edit/delete are implemented. |
| Persist sessions across restart | Room persistence implemented; manual relaunch/recovery validation passed for active tracking state. |
| Foreground tracking notification | Implemented; manual device validation confirmed persistent notification and visible Stop action. |
| Carry-over calculation | Domain logic implemented and unit-tested; Home derives carry-in from the last 30 days of persisted sessions and counts traced-time days as workdays. |
| Predictive leave time | Implemented for current day and active tracking. |
| Correction flow | Short gaps are surfaced on Home with accept/reject actions; rejected suggestions persist locally. |
| Local-first/no account behavior | Implemented; no cloud/account/analytics added. |
| Domain tests | Implemented and passing. |

## 7. Next Milestone

Next milestone: **Phase 6 - Reliability and Recovery**, while finishing gaps from Phase 5.

Immediate next tasks:

| Order | Task | Output |
| --- | --- | --- |
| 1 | Add instrumentation coverage where feasible | Cover actual notification actions, active recovery, and foreground service lifecycle on Android runtime. |
| 2 | Finish remaining manual runtime checks | Fresh-install permission-denial paths, reboot recovery, cross-midnight rollover, export/share sheet, local-data deletion, and accessibility pass. |
| 3 | Add final store work | Store listing privacy policy, production signing credentials outside git, and final accessibility pass. |

## 8. Notes for Future Agents

- Treat `docs/prompt.md` as authoritative for product intent and scope.
- Treat `docs/constraints.md` as authoritative for engineering boundaries.
- The current implementation deliberately remains single-module per `docs/plan.md`.
- Do not add cloud sync, accounts, analytics, widgets, Wear OS, or enterprise features before MVP tracking reliability is proven.
- Use JDK 17 for Android builds unless the Gradle/AGP stack is intentionally upgraded.
