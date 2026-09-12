# GYMORA System Architecture

**Project:** Hunter System  
**App:** GYMORA  
**Status:** Phase 0 baseline

## 1. Target topology

```text
GYMORA Android app
        ↓ HTTPS
Apache reverse proxy / TLS / request limits
        ↓
Versioned backend API
        ↓
Service and validation layer
        ↓
PostgreSQL/Supabase + object storage
```

Camera frames and local AI inference should remain on-device by default. Cloud AI is opt-in and must be clearly disclosed.

## 2. Android layers

- Presentation: Compose screens, navigation, accessibility, state rendering.
- Domain: quests, progression, safety, verification weighting, and deterministic rules.
- Data: repositories, Room, DataStore, WorkManager, API clients, sync.
- Device capabilities: CameraX, pose detector, secure storage, notification and restriction adapters.

## 3. Backend responsibilities

- Authentication and account lifecycle.
- User-scoped profile and workout data.
- Server-authoritative competitive progression.
- Idempotent completion and synchronization.
- Leaderboard privacy and anti-cheat checks.
- AI request orchestration without exposing provider secrets.
- Model registry metadata and download authorization.
- Privacy export/deletion and audit events.

## 4. Data boundaries

Structured tables are preferred over one giant application-state JSON document. Core entities include users, hunter profiles, exercises, plans, sessions, sets, reps, pose analysis, quests, achievements, progression events, streaks, seasons, leaderboards, restrictions, leave requests, AI providers/models/conversations, devices, sync events, privacy requests, and audit events.

The repository currently contains only a subset of these entities. The schema should grow through reviewed migrations, not ad-hoc client writes.

## 5. Authentication and authorization

The client must use short-lived access tokens and rotated refresh tokens stored securely. Every read and write is user-scoped. Guest mode is local-first; linking a guest to a cloud account requires an explicit merge policy and must not silently discard progress.

## 6. AI architecture

```text
User/event → context builder → provider adapter → structured response
          → schema validator → safety validator → domain rules → user-confirmed action
```

AI never receives unrestricted device control. Model files are treated as untrusted input and require registry metadata, compatibility checks, checksum verification, and safe app-private storage.

## 7. Camera architecture

```text
CameraX → sampled frames → pose detector → landmarks → smoothing
        → exercise state machine → rep validation → confidence → workout state
```

Manual/guided mode is always available. No feature may claim medical-grade or perfect form verification.

## 8. Deployment

Apache terminates HTTPS and reverse-proxies the API. The application server owns business logic. Secrets are injected through the environment or a secret manager and never stored in public directories or source control. Production needs backups, migrations, health checks, rate limits, security headers, sanitized logs, and monitoring.

## 9. Current architecture risks

The repository currently contains both an Android project under `app/` and another under `android/`, plus both FastAPI and Supabase backend paths. One canonical source of truth must be selected before production expansion.
