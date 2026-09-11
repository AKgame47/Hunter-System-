create or replace function public.complete_workout(p_session_id uuid, p_idempotency_key uuid) returns public.profiles
language plpgsql security definer set search_path = '' as $$
declare
  v_user uuid := (select auth.uid());
  v_session public.workout_sessions;
  v_profile public.profiles;
  v_total integer;
  v_level_gain integer;
  v_inserted integer := 0;
begin
  if v_user is null then raise exception 'authentication required' using errcode='28000'; end if;

  select * into v_session
  from public.workout_sessions
  where id = p_session_id and user_id = v_user
  for update;

  if not found then raise exception 'workout not found' using errcode='P0002'; end if;
  if v_session.status = 'completed' then
    select * into v_profile from public.profiles where user_id = v_user;
    return v_profile;
  end if;
  if v_session.status not in ('active','paused') then raise exception 'workout is not completable'; end if;
  if not exists (
    select 1 from public.workout_sets
    where session_id = p_session_id and user_id = v_user and completed_at is not null
  ) then raise exception 'no completed sets'; end if;

  insert into public.progression_events(id,user_id,source_type,source_id,xp_delta,rules_version)
  values(p_idempotency_key,v_user,'workout',p_session_id,v_session.reward_xp,'v1')
  on conflict(user_id,source_type,source_id) do nothing;
  get diagnostics v_inserted = row_count;

  if v_inserted = 0 then
    select * into v_profile from public.profiles where user_id = v_user;
    return v_profile;
  end if;

  update public.workout_sessions
  set status='completed', completed_at=now(), updated_at=now()
  where id=p_session_id;

  select xp + v_session.reward_xp into v_total
  from public.profiles where user_id=v_user for update;
  v_level_gain := v_total / 500;

  update public.profiles
  set level=level+v_level_gain,
      xp=v_total%500,
      rank=case when level+v_level_gain>=50 then 'S' when level+v_level_gain>=35 then 'A' when level+v_level_gain>=25 then 'B' when level+v_level_gain>=15 then 'C' when level+v_level_gain>=5 then 'D' else 'E' end,
      completed_quests=completed_quests+1,
      version=version+1,
      updated_at=now()
  where user_id=v_user
  returning * into v_profile;

  return v_profile;
end;
$$;

revoke all on function public.complete_workout(uuid,uuid) from public, anon;
grant execute on function public.complete_workout(uuid,uuid) to authenticated;
