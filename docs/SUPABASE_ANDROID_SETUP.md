# Supabase configuration for GYMORA

The Supabase project is already connected to the repository's database schema. The Android client reads configuration at build time and does not commit credentials.

## Local build

Create `gradle.properties` in the repository root, using `gradle.properties.example` as a template:

```properties
supabase.url=https://fmrszokykwvgknnaeqmk.supabase.co
supabase.publishableKey=YOUR_PUBLISHABLE_KEY
```

The same values can be supplied through environment variables:

```bash
export SUPABASE_URL="https://fmrszokykwvgknnaeqmk.supabase.co"
export SUPABASE_PUBLISHABLE_KEY="YOUR_PUBLISHABLE_KEY"
```

The publishable/anon key is intended for client applications. Row-level security remains the authorization boundary. Never use a Supabase secret/service-role key in the Android app.

## Current client behavior

- Supabase Auth supports email/password sign-in and sign-up.
- The profile row is loaded from `public.profiles` after sign-in.
- Quest completion syncs the current profile when an authenticated session exists.
- Guest mode remains local-first when no account is configured.
- The app never uploads camera frames through this profile-sync path.

## Required project settings

- Keep the `public.profiles` insert trigger enabled for new Auth users.
- Keep RLS enabled on `public.profiles`.
- Keep profile updates scoped to `auth.uid() = user_id`.
- Configure email confirmation and redirect URLs according to the release environment.
- Test sign-in, sign-up, profile loading, profile update, sign-out, and offline recovery on a physical device.
