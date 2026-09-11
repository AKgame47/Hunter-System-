# Hunter System Supabase backend

This directory is the selected backend path for development and closed beta. The previous FastAPI/Apache implementation remains as a fallback until migration and authorization tests pass.

## Setup

1. Create a Supabase project in the nearest suitable region.
2. Install the Supabase CLI and run `supabase link --project-ref YOUR_PROJECT_REF`.
3. Review the migration, then run `supabase db push`.
4. Enable email confirmation and configure `huntersystem://auth-callback` as an allowed redirect.
5. Store AI-provider credentials only in Edge Function secrets.
6. Put only `SUPABASE_URL` and the publishable key into Android build-time configuration.

## Security rules

- Every exposed table has RLS enabled.
- `anon` receives no application-table access.
- Users can access only rows owned by `auth.uid()`.
- XP and rank mutation uses the validated `complete_workout` RPC.
- Never ship a secret or service-role key in Android.

## Validation still required

Run migrations against a disposable local Supabase instance, test two-user isolation, duplicate completion, token expiry, account deletion, and offline replay before production use.
