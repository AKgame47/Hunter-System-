# GYMORA

**Project:** Hunter System  
**App:** GYMORA  
**Tagline:** Train · Discipline · Evolve

GYMORA is an original Android fitness RPG that turns safe, verified training into meaningful progression. The project name remains **Hunter System**; the user-facing app name is **GYMORA**.

## Current status

The repository contains an early functional vertical slice: Compose navigation, a daily quest, guided workout progression, XP/rank logic, a leaderboard preview, and a dark blue energy design system. Camera verification, cloud synchronization, AI, restrictions, and production release work are intentionally staged behind the roadmap.

See:

- [`docs/PRD.md`](docs/PRD.md)
- [`docs/TRD.md`](docs/TRD.md)
- [`docs/UI_UX_DESIGN.md`](docs/UI_UX_DESIGN.md)
- [`docs/SYSTEM_ARCHITECTURE.md`](docs/SYSTEM_ARCHITECTURE.md)
- [`docs/ROADMAP.md`](docs/ROADMAP.md)
- [`docs/RISK_REGISTER.md`](docs/RISK_REGISTER.md)
- [`docs/PLAY_STORE_READINESS.md`](docs/PLAY_STORE_READINESS.md)

## Android build

The canonical Android project is being consolidated under `app/`. Use the Gradle wrapper in the canonical project once the duplicate `android/` tree is retired. The CI workflow currently targets the legacy `android/` tree and must be updated as part of the consolidation work.

## Product safety

GYMORA is not a medical device or medical authority. Workout guidance must be conservative, support stopping, and never reward unsafe exercise volume merely to preserve a streak or increase engagement.
