# GYMORA Risk Register

**Project:** Hunter System  
**App:** GYMORA  
**Status:** Phase 0 baseline

| Risk | Probability | Impact | Mitigation | Owner | Status |
|---|---:|---:|---|---|---|
| Camera false positives or missed reps | High | High | Confidence thresholds, manual fallback, recorded-landmark tests, physical-device calibration | CV/Android | Open |
| Android restriction APIs or Play policy disallow a requested mode | High | High | Verify APIs and policy before implementation; ship only compliant modes; preserve emergency bypass | Android/Release | Open |
| AI produces unsafe workout advice | Medium | Critical | Structured output, range validation, safety rules, user confirmation, conservative recovery behavior | AI/Product | Open |
| Local models overload RAM, storage, battery, or thermals | High | High | Compatibility metadata, free-space checks, cancellation, checksum/signature verification, device matrix | AI/Performance | Open |
| Guest-to-account merge loses progress | Medium | High | Stable IDs, explicit merge preview, conflict policy, two-user integration tests | Data/Backend | Open |
| Competitive scores are manipulated locally | High | High | Server-authoritative rewards, idempotent events, sanity checks, privacy controls | Backend | Open |
| Camera frames or AI conversations are retained unexpectedly | Medium | Critical | On-device default, data minimization, explicit consent, retention/deletion flows, redacted logs | Privacy/Security | Open |
| Duplicate Android trees drift | High | High | Canonicalize `app/`, update CI, retire or isolate `android/` | Architecture/DevOps | In progress |
| FastAPI and Supabase schemas diverge | High | High | Select one authoritative backend and formalize any migration/adapter | Architecture/Backend | Open |
| Release evidence is mistaken for production readiness | Medium | High | Track target versus measurement, require physical-device QA and signed release validation | QA/Release | Open |
