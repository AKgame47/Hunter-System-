# GYMORA Technical Requirements Document

**Project:** Hunter System  
**App:** GYMORA  
**Status:** Phase 0 baseline

## 1. Canonical stack

- Kotlin and Jetpack Compose for Android UI.
- Material 3 primitives with a custom GYMORA theme.
- ViewModel plus StateFlow/state holders for screen state.
- Room for structured local data when persistence is introduced.
- DataStore for non-sensitive preferences.
- WorkManager for durable sync and model-download work.
- CameraX behind a replaceable camera boundary.
- A versioned HTTPS API under `/api/v1/`.
- Supabase or the FastAPI service may be the authoritative backend, but both must not remain competing sources of truth.

## 2. Compatibility targets

The current Gradle configuration targets Android API 36 with a minimum SDK of 26 and Java 17 bytecode. These values must be verified against the installed toolchain before release builds. The duplicate `android/` project must be retired or explicitly separated from the canonical `app/` project.

## 3. Domain boundaries

- `core-domain`: progression, quests, safety rules, and validation.
- `core-data`: repositories and synchronization contracts.
- `core-camera`: camera permission, frame lifecycle, and pose interfaces.
- `core-ai`: provider abstraction, schema validation, and safety gates.
- `feature-home`, `feature-quest`, `feature-workout`, `feature-progress`, and `feature-settings`.

The current code is a small vertical slice. New features should be extracted only when the boundary is real and tested.

## 4. Required contracts

### Progression

XP awards must be idempotent, bounded, and weighted by the source of verification. Rank calculations must be deterministic and covered by unit tests.

### Camera

`PoseDetector`, `ExerciseAnalyzer`, `RepDetector`, and `FormAnalyzer` are interfaces. Low confidence, occlusion, missing landmarks, and a user leaving the frame must not count as a verified repetition.

### AI

AI responses are untrusted data. Parse into a versioned schema, validate ranges and safety constraints, apply domain rules, and present a user-confirmable action. No unrestricted device commands.

### Sync

Use stable IDs, per-aggregate versions, idempotency keys, retry with backoff, and a conflict policy. Cursors must be deterministic when timestamps collide.

## 5. Performance targets

Targets are not measurements:

- Cold start: under 2.5 seconds on a mid-tier supported device.
- Main-thread frame work: under 16 ms for normal UI frames.
- Camera preview: target 30 FPS where hardware permits.
- Pose inference: sample frames rather than blocking every preview frame.
- No network or model work on the main thread.
- Model downloads must be cancellable and checksum-verified.

Actual measurements must be recorded during physical-device validation.

## 6. Security requirements

- Never commit secrets or complete API keys.
- Store credentials in Android-secure storage and redact logs.
- Use least-privilege database grants and user-scoped policies.
- Validate all API input and normalize authentication identifiers.
- Add rate limiting before public deployment.
- Keep production API documentation and stack traces disabled or sanitized.

## 7. Testing requirements

- Domain unit tests for XP, rank, quest completion, streaks, and safety rules.
- Repository and database tests for idempotency and conflict resolution.
- Camera state-machine tests using recorded landmarks, not claims of camera accuracy.
- API integration tests with two isolated users.
- Compose UI tests for onboarding, quest completion, error, offline, and permission states.
- Release build, accessibility, battery, thermal, and physical-device validation.
