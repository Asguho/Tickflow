# Tickflow Release Notes

Last updated: 2026-05-17

## Build Commands

Use JDK 17 for Android builds with the current Gradle/AGP stack.

```bash
JAVA_HOME=$PWD/.jdk/jdk17 ./gradlew testDebugUnitTest assembleDebug assembleRelease
```

## Current Build Outputs

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Unsigned release APK, when signing env vars are absent: `app/build/outputs/apk/release/Tickflow-<versionName>.apk` (unsigned variant produced by `assembleRelease` is named `app-release-unsigned.apk` only if the rename does not run; the configured rename produces `Tickflow-<versionName>.apk`)
- Signed release APK, when signing env vars are present: `app/build/outputs/apk/release/Tickflow-<versionName>.apk`

The release build stays unsigned by default. This is expected unless production signing environment variables are provided. Android will refuse to install an unsigned APK with the error "App not installed", so any APK published to GitHub releases must be signed.

## Signing

The Gradle release build can sign locally when all of these environment variables are set:

```bash
export TICKFLOW_RELEASE_KEYSTORE=/absolute/path/to/upload-keystore.jks
export TICKFLOW_RELEASE_KEYSTORE_PASSWORD=...
export TICKFLOW_RELEASE_KEY_ALIAS=...
export TICKFLOW_RELEASE_KEY_PASSWORD=...
```

Before public or closed testing distribution:

1. Create or obtain the production upload/signing key outside the repository.
2. Store signing credentials outside git.
3. Set the signing environment variables above.
4. Verify `assembleRelease` produces a signed artifact.

Do not commit keystores, passwords, upload certificates, or Play Console credentials.

### CI release signing (GitHub Actions)

The `Release` workflow (`.github/workflows/release.yml`) runs on manual dispatch and on every push to `main`. It builds the release APK, signs it, and attaches it to a GitHub release. Manual runs can provide an explicit tag; push-triggered runs use `v<appVersionName>-build.<github-run-number>`.

The workflow picks signing material from the following repository secrets:

- `RELEASE_KEYSTORE_BASE64` — the keystore file, base64 encoded (`base64 -w0 upload-keystore.jks`)
- `RELEASE_KEYSTORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

The workflow validates the keystore, alias, store password, and key password before Gradle runs. If the secrets are incomplete or cannot be validated, it falls back to generating an ephemeral keystore so the published APK is at least installable. The signature will differ between releases in that mode, which means users have to uninstall the previous build before installing a new one. Set valid secrets above for a stable signature across releases.

Local Robolectric tests are pinned to SDK 35 in `app/src/test/resources/robolectric.properties`; the production app still compiles and targets SDK 36.

## Store Readiness

Before store distribution:

- Complete `docs/manual-qa.md` on target Android versions.
- Convert `docs/privacy.md` into store/privacy-policy copy.
- Verify foreground service policy language for active work tracking.
- Verify Wi-Fi/location permission disclosure copy on setup.
- Confirm local data deletion works on a device.
- Review launcher icon appearance on light, dark, themed, round, and adaptive icon surfaces.
