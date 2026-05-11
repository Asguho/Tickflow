# Tickflow Status

Last updated: 2026-05-11

## 1. Current Project Status

Tickflow is in the planning and architecture-memory stage.

The repository contains the initial product idea and context. Durable memory documents have now been established under `docs/` so future work can proceed consistently after long-running sessions or context resets.

No Android application scaffold has been created yet. There is currently no Gradle project, app module, Kotlin source code, Compose UI, persistence layer, service implementation, or automated test suite.

## 2. Completed

| Item | Status | Notes |
| --- | --- | --- |
| Initial product idea | Complete | Captured in `IDEA.md`. |
| Expanded architecture/product context | Complete | Captured in `context/context1.md`. |
| Secondary context file | Present but empty | `context/context2.md` currently has no content. |
| Durable source of truth | Complete | `docs/prompt.md` defines vision, requirements, stack, non-goals, and success criteria. |
| Execution plan | Complete | `docs/plan.md` defines phases, tasks, risks, and verification strategy. |
| Constraints document | Complete | `docs/constraints.md` defines technical, performance, privacy, style, and forbidden-pattern constraints. |
| Initial status document | Complete | This file records current state and next milestone. |

## 3. In Progress

| Item | Status | Notes |
| --- | --- | --- |
| Product architecture alignment | In progress | The durable docs establish the baseline. Future implementation should keep them updated. |

## 4. Not Started

| Area | Notes |
| --- | --- |
| Android project scaffold | No Gradle files or app module exist yet. |
| Compose UI | No UI code exists yet. |
| Domain model | Work session, day balance, carry-over, and prediction logic are not implemented yet. |
| Persistence | Room and DataStore are not configured yet. |
| Dependency injection | Hilt is not configured yet. |
| Foreground service | Tracking service does not exist yet. |
| Wi-Fi presence monitor | Connectivity/Wi-Fi integration does not exist yet. |
| WorkManager jobs | Rollover and recovery workers do not exist yet. |
| Notifications | Tracking notification does not exist yet. |
| Tests | No automated test setup exists yet. |
| Release assets | No icon, package metadata, signing, or release configuration exists yet. |

## 5. Blocking Issues

No implementation blockers are known yet.

Important unresolved decisions for Phase 1 and Phase 2:

| Decision | Why It Matters | Suggested Default |
| --- | --- | --- |
| Package/application ID | Needed before Android scaffold. | Use `com.tickflow.app` unless project ownership suggests another namespace. |
| Minimum SDK | Affects API availability and permission behavior. | Choose a modern but practical min SDK during scaffold; document it in `docs/constraints.md`. |
| Wi-Fi identifier strategy | SSID/BSSID access and storage affect permissions and privacy. | Start with local-only configured Wi-Fi matching and avoid cloud transmission. |
| DataStore type | Preferences vs Proto affects schema discipline. | Preferences is acceptable for MVP; Proto is better if settings become complex. |
| Single-module vs multi-module | Affects build complexity. | Start single-module with clear packages; split later if needed. |

## 6. Next Milestone

Next milestone: **Phase 1 - Android Foundation**.

Definition of done for the next milestone:

- Android Gradle project exists.
- `app` module builds with Gradle Kotlin DSL.
- Kotlin, Compose, Material 3, Navigation Compose, Hilt, Room, DataStore, WorkManager, and test dependencies are configured.
- App launches to a simple Compose shell.
- `./gradlew assembleDebug` passes.
- `./gradlew testDebugUnitTest` passes.
- Initial package structure follows `docs/plan.md`.

## 7. Immediate Next Tasks

| Order | Task | Output |
| --- | --- | --- |
| 1 | Scaffold Android project | Gradle root files, `app` module, manifest, application class, main activity. |
| 2 | Configure dependencies | Compose, Material 3, Navigation, Hilt, Room, DataStore, WorkManager, tests. |
| 3 | Add app shell | Minimal theme, `MainActivity`, nav host, placeholder home route. |
| 4 | Add test baseline | One passing unit test and test command documented. |
| 5 | Update status | Record scaffold decisions, commands run, and any blockers. |

## 8. Current Acceptance State

| MVP Acceptance Criterion | Current State |
| --- | --- |
| Configure office Wi-Fi and work schedule | Not started |
| Automatic Wi-Fi tracking | Not started |
| Manual start/stop/edit | Not started |
| Persist sessions across restart | Not started |
| Foreground tracking notification | Not started |
| Carry-over calculation | Not started |
| Predictive leave time | Not started |
| Correction flow | Not started |
| Local-first/no account behavior | Planned, not implemented |
| Domain tests | Not started |

## 9. Notes for Future Agents

- Treat `docs/prompt.md` as authoritative for product intent and scope.
- Treat `docs/constraints.md` as authoritative for engineering boundaries.
- Before adding code, check `docs/plan.md` for milestone order and package structure.
- After meaningful progress, update this file with completed work, commands run, blockers, and next tasks.
- Do not add cloud sync, accounts, analytics, widgets, Wear OS, or enterprise features before the MVP tracking loop is reliable.
