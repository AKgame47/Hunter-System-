# GYMORA Product Requirements Document

**Project:** Hunter System  
**App:** GYMORA  
**Status:** Living document — Phase 0 baseline

## 1. Vision

GYMORA helps people build consistent, safe training habits by combining a game-like progression system with practical workouts, transparent verification, recovery-aware streaks, and an original futuristic visual identity.

## 2. Problem

Many fitness apps either feel clinical and repetitive or optimize for engagement without enough safety context. GYMORA should make a session feel purposeful while keeping real fitness, recovery, privacy, and user control ahead of XP and rank.

## 3. Target users

- Beginners who need simple guided workouts.
- Consistent exercisers who want progression and accountability.
- Gamers who respond to clear quests, ranks, and achievements.
- Privacy-conscious users who want local/manual modes when cloud services are unavailable.

## 4. Product principles

1. Safety overrides engagement.
2. Verified activity is more valuable than unverified claims.
3. RPG metrics remain visually distinct from real fitness metrics.
4. Every cloud feature has an honest offline state.
5. AI output is untrusted until validated.
6. Camera guidance must communicate uncertainty.
7. The product uses original branding and assets.

## 5. Core loop

`Plan → Quest → Train → Verify → Reward → Recover → Next quest`

## 6. MVP scope

### Identity

- First launch and short onboarding.
- Guest/local profile.
- Optional account linking after the local flow is stable.

### Core training

- Hunter profile with level, rank, XP, streak, and quest count.
- Daily quest with a small exercise set.
- Guided workout with manual completion and explicit safety copy.
- XP award with verification weighting.
- Quest completion summary.

### Progress

- Rank progression.
- Basic statistics and achievements.
- Leaderboard preview with privacy-safe server validation planned.

### Platform foundation

- Offline-first local state.
- Versioned API boundary.
- Camera interface with manual fallback.
- CI, tests, release checklist, and documented risks.

## 7. Post-MVP scope

- Google authentication and guest-to-account linking.
- Room-backed workout history and conflict-aware sync.
- CameraX plus a replaceable pose detector.
- AI Coach with structured output and safety validation.
- Local model runtime and verified model downloads.
- Restrictions, leave/recovery, inactivity, seasons, social features, and production leaderboards.
- Full privacy export/deletion flows and Play Store release readiness.

## 8. Safety requirements

- Ask about experience, goals, equipment, time, and limitations before adaptive plans.
- Never diagnose or claim medical clearance.
- Stop or offer a conservative alternative when pain, dizziness, or unusual symptoms are reported.
- Do not require camera use; retain a guided/manual path.
- Never delete an account or valuable data because of inactivity.
- Do not make exercise volume the sole competitive advantage.

## 9. Definition of done

A feature is complete only when it is implemented, builds, tested, exercised in the running app, reviewed for accessibility and privacy, documented, and assigned an honest production status.
