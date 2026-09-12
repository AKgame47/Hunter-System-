# GYMORA UI/UX Design

**Project:** Hunter System  
**App:** GYMORA  
**Primary reference:** user-provided GYMORA/Hunter System concept board and icon

## 1. Design direction

GYMORA is dark, cinematic, athletic, and futuristic. The visual language uses deep navy surfaces, electric blue energy, restrained violet accents, thin luminous borders, and strong information hierarchy. Glow is reserved for focus, progression, verified success, and important actions.

The supplied concept is a direction, not a pixel-copy target. All production art, characters, icons, and copy must be original.

## 2. Brand hierarchy

- **GYMORA** — user-facing app name.
- **Hunter System** — project/system identity used in technical and in-world language.
- **Train · Discipline · Evolve** — product tagline.
- **Level up in real life** — product philosophy.

## 3. Navigation

Primary bottom navigation:

- Home
- Quest
- Train
- Rank
- Menu

Menu contains Profile, Achievements, Statistics, AI Coach, App Restrictions, Leave/Recovery, Settings, Developer Settings, and About.

## 4. Screen map

`Splash → Onboarding → Authentication/Guest → Hunter setup → Home`

`Home → Today's quest → Workout → Exercise guide → Camera/manual training → Completion → Reward`

`Menu → Profile / Achievements / Statistics / AI Coach / Restrictions / Leave / Settings / Developer / About`

## 5. Reusable components

- `GymoraTopBar`
- `SystemCard`
- `EnergyCard`
- `QuestCard`
- `ExerciseCard`
- `XPBar`
- `RankBadge`
- `StreakIndicator`
- `EnergyButton`
- `WarningPanel`
- `AchievementCard`
- `LeaderboardRow`
- `AIMessage`
- `ExerciseGuide`
- `CameraHUD`
- `RestrictionCard`
- `ModelCard`
- `SettingsRow`
- `OfflineBanner`
- `LoadingState`, `EmptyState`, and `ErrorState`

## 6. Accessibility

Every meaningful icon has a semantic label. Status never depends on color alone. Touch targets are at least 48dp, text supports system scaling, animations respect reduced-motion preferences, and focus order follows the visual reading order.

## 7. Copy and safety

Use direct, motivating language without coercion. Workout screens must keep a visible stop/safety affordance. Camera uncertainty must be expressed in normal user language such as “Move into frame” or “Form uncertain,” not hidden diagnostics.

## 8. Screen state requirements

Every network, camera, model, and AI screen needs loading, empty, offline, error, permission-denied, retry, and success states. A manual workout path must remain available when camera or AI features are unavailable.
