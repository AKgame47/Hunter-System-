-- Restrict the exercise catalog to authenticated reads only.
-- RLS already limits rows to active exercises, but table grants must also be least privilege.
revoke all on table public.exercises from anon, authenticated;
grant select on table public.exercises to authenticated;

-- Keep the privileged progression RPC callable only by signed-in users.
revoke all on function public.complete_workout(uuid, uuid) from public, anon;
grant execute on function public.complete_workout(uuid, uuid) to authenticated;

-- Cover foreign keys reported by the Supabase database advisor.
create index if not exists quests_completed_session_idx
  on public.quests(completed_session_id)
  where completed_session_id is not null;

create index if not exists workout_sets_exercise_idx
  on public.workout_sets(exercise_id);
