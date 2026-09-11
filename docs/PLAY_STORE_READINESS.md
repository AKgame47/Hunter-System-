# Hunter System — Play Store readiness

## Implemented and build-verifiable
- Native Kotlin/Compose app, adaptive icon, cleartext disabled.
- Supabase email/password auth and deep-link callback.
- Room profile cache and WorkManager profile outbox.
- CameraX preview/analyzer screen with optional camera permission and manual fallback.
- ML Kit processing remains on device.
- Pure Kotlin pose math/state machines and JVM tests.
- Local model catalog/download worker with pinned HTTPS source, size/free-space checks, SHA-256 verification, app-private no-backup storage, cancellation cleanup, and removal.

## Data Safety draft
- Account identifiers and profile data: collected for account/sync functionality.
- Workout/session data: collected for progression and cross-device sync.
- Camera frames: processed on device for pose guidance and not intentionally uploaded or retained.
- Downloaded AI model files: stored locally in app-private storage; prompts must remain local when a runtime is added.
- Users need account deletion/export flows and a public deletion URL before release.

## Physical-device validation still required
Test on low/mid/high-tier Android 26–36 devices: camera rotation/mirroring, front/back availability, permission denial, background/foreground lifecycle, memory pressure, thermal/battery behavior, pose accuracy for varied bodies/lighting/clothing, model download interruption/resume, insufficient storage, metered data, checksum failure, uninstall cleanup, auth callback, offline sync/retry, accessibility, and upgrade preservation.

## Blocking release items
- Local AI inference runtime is not yet bundled; download status must not be presented as inference-ready.
- Camera overlay alignment and every pose tracker require recorded-video and physical-device calibration.
- Server-backed workout session/set upload and `complete_workout` RPC reconciliation require end-to-end tests with two isolated test users.
- Final screenshots, feature graphic, privacy policy URL, support URL, account-deletion URL, signed AAB, Play Integrity decision, and completed Data Safety form.
