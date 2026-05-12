---
name: android-adb-ui-inspector
description: Use when inspecting a live Android app UI on a connected device/emulator with ADB, including screenshots, UIAutomator accessibility hierarchy dumps, taps, swipes, text input, launches, and concise UI findings.
---

# Android ADB UI Inspector

Use ADB plus UIAutomator instead of screenshots alone.

## Workflow

1. Confirm device and foreground app:
   ```bash
   adb devices -l
   adb shell dumpsys window | rg -i 'mCurrentFocus|mFocusedApp|topResumedActivity'
   ```

2. Capture screen and hierarchy:
   ```bash
   adb exec-out screencap -p > /tmp/android_screen.png
   adb shell uiautomator dump /sdcard/window_dump.xml
   adb pull /sdcard/window_dump.xml /tmp/window_dump.xml
   ```

3. Inspect both:
   - Open `/tmp/android_screen.png` visually.
   - Read `/tmp/window_dump.xml` for text, labels, clickable nodes, and `bounds`.

4. Interact only when useful:
   ```bash
   adb shell input tap X Y
   adb shell input swipe X1 Y1 X2 Y2
   adb shell input text "hello"
   adb shell monkey -p PACKAGE -c android.intent.category.LAUNCHER 1
   ```

## Report

Keep findings concise:
- Current screen and key visible text.
- Main clickable controls and coordinates if relevant.
- Obvious visual/accessibility issues, especially overlap, clipping, missing labels, tiny targets, or unsafe system-bar insets.
- Mention any command that failed or any missing device authorization.
