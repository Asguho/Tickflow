# Tickflow Privacy Notes

Last updated: 2026-05-11

Tickflow is designed as a local-first personal workday assistant.

## Data Stored On Device

Tickflow stores the following data locally on the Android device:

- Work sessions, including start and end times, source, correction state, and optional notes.
- Work schedule settings, including target duration and selected workdays.
- Configured office Wi-Fi identifiers, such as SSID and optional BSSID.
- Notification and carry-over preferences.
- Rejected correction suggestion identifiers.

## Network and Accounts

Tickflow does not require an account for core features.

Tickflow does not send work sessions, Wi-Fi identifiers, schedules, or correction data to a server by default.

## Permissions

Tickflow may request location-related permission because Android can require it to read Wi-Fi SSID/BSSID information. Tickflow uses that permission to identify the configured work Wi-Fi. The MVP does not use GPS tracking.

Tickflow may request notification permission so active work tracking can remain visible through a foreground notification and offer quick actions such as stopping tracking.

## Export and Deletion

Session export is user-initiated and uses Android's share sheet with CSV text.

The Settings screen includes a local data deletion action that clears Tickflow sessions and settings from the app's local storage.

## Telemetry

The MVP does not include analytics, crash reporting, remote logging, cloud sync, or advertising SDKs.
