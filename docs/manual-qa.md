# Tickflow Manual QA Checklist

Last updated: 2026-05-12

Use this checklist on a physical Android device when possible. Wi-Fi SSID/BSSID access and foreground service behavior can differ by Android version and device vendor.

## Environment

| Item | Result | Notes |
| --- | --- | --- |
| Android version recorded | Pass | Android 16. |
| Device model recorded | Pass | Pixel 10 (`frankel`), ADB serial `5A291FDCR000A9`. |
| Fresh install from debug APK | Partial | Installed current debug APK with `adb install -r`; existing app data was preserved. |
| App data cleared before test | Not run | Preserved configured `TheLoge` setup and existing sessions. |

## Setup and Permissions

| Test | Expected Result | Result |
| --- | --- | --- |
| Open fresh install | Home shows setup prompt. | Not run |
| Start setup | Setup screen shows Wi-Fi permission rationale. | Not run |
| Deny location permission | App remains usable for manual tracking; no crash. | Not run |
| Grant location permission | Setup can save office Wi-Fi identifier. | Pass; location app-op was foreground/allowed and Wi-Fi SSID was visible. |
| Deny notification permission on Android 13+ | Manual tracking still persists; notification may be unavailable without crash. | Not run |
| Grant notification permission | Active tracking shows persistent notification. | Pass; foreground notification channel `active_tracking` posted while tracking. |

## Manual Tracking

| Test | Expected Result | Result |
| --- | --- | --- |
| Tap Start work | Active session is persisted and Home shows tracking state. | Pass; Home changed from `Start work` to `Stop tracking`. |
| Leave app and reopen | Active session is still visible. | Pass; force-stop/relaunch restored persisted idle/active state as expected for existing data. |
| Use notification Stop action | Session closes and notification is removed. | Pass; expanded notification exposed `Stop`, tap closed session and removed active notification. |
| Add session for another date | Session appears when navigating to that date. | Not run |
| Edit a session time | Totals and predicted leave time update. | Not run |
| Delete a session | Session disappears and totals update. | Not run |

## Wi-Fi Tracking

| Test | Expected Result | Result |
| --- | --- | --- |
| Configure current office Wi-Fi | Settings persist across app restart. | Pass; existing setup used `TheLoge` and survived reinstall/relaunch. |
| Connect to configured Wi-Fi | Wi-Fi session starts without duplicate overlap. | Pass; reconnect to `TheLoge` started one active Wi-Fi session. |
| Trigger repeated connectivity events | Only one active session exists. | Pass; session table showed one active row after reconnect/resample. |
| Disconnect from configured Wi-Fi | Active session closes. | Pass; Wi-Fi disabled moved device to `<unknown ssid>` and no active Tickflow notification remained for that closed state. |
| Leave and return after short gap | Home offers a correction review. | Pass; Home displayed `Review gap` for `20:46 - 21:00 (14m)`. |
| Accept correction | Gap becomes an inferred corrected work session. | Not run |
| Reject correction and restart app | Same rejected suggestion does not reappear. | Not run |

## Recovery

| Test | Expected Result | Result |
| --- | --- | --- |
| Kill app process while tracking | Relaunch restores active tracking state from Room. | Partial; direct `kill -9` was denied by Android shell, but relaunch/resample showed Room active session, foreground service, and UI agreed. |
| Reboot while tracking | Recovery worker restarts visible tracking state when allowed by Android. | Not run |
| Cross midnight while sessions exist | Rollover worker recalculates recent balances. | Not run |
| Disable permissions after setup | App does not crash and manual tracking remains available. | Not run |

## Privacy and Data

| Test | Expected Result | Result |
| --- | --- | --- |
| Use Delete local Tickflow data | Sessions, settings, and correction decisions reset locally. | Not run |
| Inspect logs during normal use | No raw SSID/BSSID or session history is logged by Tickflow code. | Not run |
