-- Progression fields are server-owned. Mobile clients may edit only profile presentation fields.
revoke update on table public.profiles from authenticated;
grant update (hunter_name, display_name, avatar_path) on table public.profiles to authenticated;
