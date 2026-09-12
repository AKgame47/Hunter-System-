# GYMORA Implementation Roadmap

## Phase 0 — Discovery and documentation

- Consolidate the Android project tree.
- Select the authoritative backend.
- Maintain PRD, TRD, UI/UX, architecture, API contract, and risk register.
- Verify toolchain and run a clean build.

## Phase 1 — Functional vertical slice

- Onboarding and local guest profile.
- Home and daily quest.
- Guided workout with manual completion.
- Deterministic XP, rank, streak, and summary.
- Basic local persistence.
- Unit and Compose tests.

## Phase 2 — Durable data and identity

- Room entities and migrations.
- DataStore preferences and secure credential storage.
- Google authentication and guest linking.
- Versioned API and conflict-aware sync.

## Phase 3 — Camera training

- CameraX permission and lifecycle.
- Replaceable pose detector.
- Reliable squat/push-up state machines.
- Confidence-aware reps and manual fallback.
- Physical-device calibration.

## Phase 4 — AI Coach

- Provider abstraction.
- Structured workout schema.
- Safety validation and user confirmation.
- Cloud/local/hybrid policy.
- Offline behavior.

## Phase 5 — Discipline and social features

- Leave and recovery.
- Inactivity rules without destructive deletion.
- Restrictions only where Android and Play policy allow.
- Server-validated seasons and leaderboards.
- Privacy controls and opt-out.

## Phase 6 — Local models and release

- Signed/verified registry.
- Download pause/resume/cancel/delete.
- Compatibility and storage checks.
- Performance, thermal, accessibility, security, and Play Store validation.
