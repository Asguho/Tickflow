# Tickflow

Privacy-first Android workday assistant built with Kotlin, Jetpack Compose, Room, DataStore, Hilt, WorkManager, and a foreground tracking service.

## Build

This project targets Java 17 bytecode. In this workspace, verification was run with a local JDK 17 because the host JDK 26 is not compatible with the selected Android Gradle Plugin toolchain path.

```bash
JAVA_HOME=$PWD/.jdk/jdk17 ./gradlew testDebugUnitTest
JAVA_HOME=$PWD/.jdk/jdk17 ./gradlew assembleDebug
JAVA_HOME=$PWD/.jdk/jdk17 ./gradlew assembleRelease
```

On a normal development machine, use an installed JDK 17 and Android SDK with API 36.

Release signing is optional and environment-variable driven; see `docs/release.md`.
