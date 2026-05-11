# Tickflow Manual QA Checklist

Last updated: 2026-05-11

Use this checklist on a physical Android device when possible. Wi-Fi SSID/BSSID access and foreground service behavior can differ by Android version and device vendor.

## Environment

| Item | Result | Notes |
| --- | --- | --- |
| Android version recorded | Not run |  |
| Device model recorded | Not run |  |
| Fresh install from debug APK | Not run |  |
| App data cleared before test | Not run |  |

## Setup and Permissions

| Test | Expected Result | Result |
| --- | --- | --- |
| Open fresh install | Home shows setup prompt. | Not run |
| Start setup | Setup screen shows Wi-Fi permission rationale. | Not run |
| Deny location permission | App remains usable for manual tracking; no crash. | Not run |
| Grant location permission | Setup can save office Wi-Fi identifier. | Not run |
| Deny notification permission on Android 13+ | Manual tracking still persists; notification may be unavailable without crash. | Not run |
| Grant notification permission | Active tracking shows persistent notification. | Not run |

## Manual Tracking

| Test | Expected Result | Result |
| --- | --- | --- |
| Tap Start work | Active session is persisted and Home shows tracking state. | Not run |
| Leave app and reopen | Active session is still visible. | Not run |
| Use notification Stop action | Session closes and notification is removed. | Not run |
| Add session for another date | Session appears when navigating to that date. | Not run |
| Edit a session time | Totals and predicted leave time update. | Not run |
| Delete a session | Session disappears and totals update. | Not run |

## Wi-Fi Tracking

| Test | Expected Result | Result |
| --- | --- | --- |
| Configure current office Wi-Fi | Settings persist across app restart. | Not run |
| Connect to configured Wi-Fi | Wi-Fi session starts without duplicate overlap. | Not run |
| Trigger repeated connectivity events | Only one active session exists. | Not run |
| Disconnect from configured Wi-Fi | Active session closes. | Not run |
| Leave and return after short gap | Home offers a correction review. | Not run |
| Accept correction | Gap becomes an inferred corrected work session. | Not run |
| Reject correction and restart app | Same rejected suggestion does not reappear. | Not run |

## Recovery

| Test | Expected Result | Result |
| --- | --- | --- |
| Kill app process while tracking | Relaunch restores active tracking state from Room. | Not run |
| Reboot while tracking | Recovery worker restarts visible tracking state when allowed by Android. | Not run |
| Cross midnight while sessions exist | Rollover worker recalculates recent balances. | Not run |
| Disable permissions after setup | App does not crash and manual tracking remains available. | Not run |

## Privacy and Data

| Test | Expected Result | Result |
| --- | --- | --- |
| Use Delete local Tickflow data | Sessions, settings, and correction decisions reset locally. | Not run |
| Inspect logs during normal use | No raw SSID/BSSID or session history is logged by Tickflow code. | Not run |

