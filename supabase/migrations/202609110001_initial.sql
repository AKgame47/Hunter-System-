create extension if not exists pgcrypto;
create schema if not exists private;

create table public.profiles (
  user_id uuid primary key references auth.users(id) on delete cascade,
  hunter_name varchar(40) not null,
  display_name varchar(80) not null,
  avatar_path text,
  level integer not null default 1 check (level >= 1),
  rank varchar(1) not null default 'E' check (rank in ('E','D','C','B','A','S')),
  xp integer not null default 0 check (xp between 0 and 499),
  streak integer not null default 0 check (streak >= 0),
  completed_quests integer not null default 0 check (completed_quests >= 0),
  version bigint not null default 1,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.exercises (
  id uuid primary key default gen_random_uuid(),
  slug varchar(80) not null unique,
  name varchar(120) not null,
  category varchar(40) not null,
  difficulty varchar(20) not null check (difficulty in ('beginner','intermediate','advanced')),
  equipment text[] not null default '{}',
  instructions jsonb not null,
  warnings jsonb not null default '[]',
  animation_key varchar(80) not null,
  camera_capability varchar(20) not null check (camera_capability in ('full','partial','manual')),
  tracking_configuration jsonb,
  active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table public.workout_sessions (
  id uuid primary key,
  user_id uuid not null references auth.users(id) on delete cascade,
  status varchar(20) not null check (status in ('planned','active','paused','completed','cancelled')),
  source varchar(20) not null check (source in ('quest','plan','manual','ai')),
  started_at timestamptz not null,
  completed_at timestamptz,
  duration_seconds integer check (duration_seconds >= 0),
  reward_xp integer not null default 0 check (reward_xp between 0 and 1000),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
create index workout_sessions_user_started_idx on public.workout_sessions(user_id, started_at desc);

create table public.workout_sets (
  id uuid primary key,
  user_id uuid not null references auth.users(id) on delete cascade,
  session_id uuid not null references public.workout_sessions(id) on delete cascade,
  exercise_id uuid not null references public.exercises(id),
  set_number integer not null check (set_number between 1 and 20),
  target_reps integer check (target_reps between 1 and 500),
  completed_reps integer not null default 0 check (completed_reps between 0 and 500),
  target_seconds integer check (target_seconds between 1 and 7200),
  completed_seconds integer not null default 0 check (completed_seconds between 0 and 7200),
  verification varchar(20) not null default 'manual' check (verification in ('manual','camera','uncertain')),
  confidence numeric(5,4) check (confidence between 0 and 1),
  completed_at timestamptz,
  unique(session_id, exercise_id, set_number)
);
create index workout_sets_user_idx on public.workout_sets(user_id);
create index workout_sets_session_idx on public.workout_sets(session_id);

create table public.quests (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  local_date date not null,
  time_zone varchar(64) not null,
  state varchar(20) not null default 'available' check (state in ('available','active','completed','expired','recovery')),
  reward_xp integer not null check (reward_xp between 0 and 1000),
  completed_session_id uuid references public.workout_sessions(id),
  created_at timestamptz not null default now(),
  unique(user_id, local_date)
);
create index quests_user_date_idx on public.quests(user_id, local_date desc);

create table public.progression_events (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  source_type varchar(30) not null,
  source_id uuid not null,
  xp_delta integer not null check (xp_delta between -1000 and 1000),
  rules_version varchar(40) not null,
  occurred_at timestamptz not null default now(),
  unique(user_id, source_type, source_id)
);
create index progression_events_user_time_idx on public.progression_events(user_id, occurred_at desc);

create table public.sync_events (
  id uuid primary key,
  user_id uuid not null references auth.users(id) on delete cascade,
  device_id uuid not null,
  aggregate_type varchar(40) not null,
  aggregate_id uuid not null,
  sequence bigint not null check (sequence >= 1),
  payload jsonb not null,
  occurred_at timestamptz not null,
  received_at timestamptz not null default now(),
  unique(user_id, aggregate_type, aggregate_id, sequence)
);
create index sync_events_user_cursor_idx on public.sync_events(user_id, received_at, id);

alter table public.profiles enable row level security;
alter table public.exercises enable row level security;
alter table public.workout_sessions enable row level security;
alter table public.workout_sets enable row level security;
alter table public.quests enable row level security;
alter table public.progression_events enable row level security;
alter table public.sync_events enable row level security;

revoke all on all tables in schema public from anon;
revoke all on public.profiles, public.workout_sessions, public.workout_sets, public.quests, public.progression_events, public.sync_events from authenticated;
grant select, update on public.profiles to authenticated;
grant select on public.exercises to authenticated;
grant select, insert, update on public.workout_sessions to authenticated;
grant select, insert, update on public.workout_sets to authenticated;
grant select on public.quests to authenticated;
grant select on public.progression_events to authenticated;
grant select, insert on public.sync_events to authenticated;

create policy profiles_select_own on public.profiles for select to authenticated using ((select auth.uid()) = user_id);
create policy profiles_update_own on public.profiles for update to authenticated using ((select auth.uid()) = user_id) with check ((select auth.uid()) = user_id);
create policy exercises_read_authenticated on public.exercises for select to authenticated using (active = true);
create policy sessions_select_own on public.workout_sessions for select to authenticated using ((select auth.uid()) = user_id);
create policy sessions_insert_own on public.workout_sessions for insert to authenticated with check ((select auth.uid()) = user_id and status in ('planned','active'));
create policy sessions_update_own on public.workout_sessions for update to authenticated using ((select auth.uid()) = user_id) with check ((select auth.uid()) = user_id);
create policy sets_select_own on public.workout_sets for select to authenticated using ((select auth.uid()) = user_id);
create policy sets_insert_own on public.workout_sets for insert to authenticated with check ((select auth.uid()) = user_id and exists (select 1 from public.workout_sessions s where s.id = session_id and s.user_id = (select auth.uid())));
create policy sets_update_own on public.workout_sets for update to authenticated using ((select auth.uid()) = user_id) with check ((select auth.uid()) = user_id);
create policy quests_select_own on public.quests for select to authenticated using ((select auth.uid()) = user_id);
create policy progression_select_own on public.progression_events for select to authenticated using ((select auth.uid()) = user_id);
create policy sync_select_own on public.sync_events for select to authenticated using ((select auth.uid()) = user_id);
create policy sync_insert_own on public.sync_events for insert to authenticated with check ((select auth.uid()) = user_id);

create or replace function private.handle_new_user() returns trigger language plpgsql security definer set search_path = '' as $$
begin
  insert into public.profiles(user_id, hunter_name, display_name)
  values (new.id, coalesce(nullif(new.raw_user_meta_data->>'hunter_name',''), 'Hunter'), coalesce(nullif(new.raw_user_meta_data->>'display_name',''), 'Hunter'));
  return new;
end;
$$;
create trigger on_auth_user_created after insert on auth.users for each row execute function private.handle_new_user();

create or replace function public.complete_workout(p_session_id uuid, p_idempotency_key uuid) returns public.profiles
language plpgsql security definer set search_path = '' as $$
declare
  v_user uuid := (select auth.uid());
  v_session public.workout_sessions;
  v_profile public.profiles;
  v_total integer;
  v_level_gain integer;
begin
  if v_user is null then raise exception 'authentication required' using errcode='28000'; end if;
  select * into v_session from public.workout_sessions where id=p_session_id and user_id=v_user for update;
  if not found then raise exception 'workout not found' using errcode='P0002'; end if;
  if v_session.status='completed' then select * into v_profile from public.profiles where user_id=v_user; return v_profile; end if;
  if not exists(select 1 from public.workout_sets where session_id=p_session_id and user_id=v_user and completed_at is not null) then raise exception 'no completed sets'; end if;
  insert into public.progression_events(id,user_id,source_type,source_id,xp_delta,rules_version) values(p_idempotency_key,v_user,'workout',p_session_id,v_session.reward_xp,'v1') on conflict(user_id,source_type,source_id) do nothing;
  update public.workout_sessions set status='completed',completed_at=coalesce(completed_at,now()),updated_at=now() where id=p_session_id;
  select xp + case when exists(select 1 from public.progression_events where id=p_idempotency_key and user_id=v_user) then v_session.reward_xp else 0 end into v_total from public.profiles where user_id=v_user for update;
  v_level_gain := v_total / 500;
  update public.profiles set level=level+v_level_gain,xp=v_total%500,rank=case when level+v_level_gain>=50 then 'S' when level+v_level_gain>=35 then 'A' when level+v_level_gain>=25 then 'B' when level+v_level_gain>=15 then 'C' when level+v_level_gain>=5 then 'D' else 'E' end,completed_quests=completed_quests+1,version=version+1,updated_at=now() where user_id=v_user returning * into v_profile;
  return v_profile;
end;
$$;
revoke all on function public.complete_workout(uuid,uuid) from public, anon;
grant execute on function public.complete_workout(uuid,uuid) to authenticated;

insert into public.exercises(slug,name,category,difficulty,equipment,instructions,warnings,animation_key,camera_capability,tracking_configuration) values
('bodyweight-squat','Bodyweight Squat','strength','beginner','{}','["Stand with feet around shoulder width","Brace your trunk","Sit down and back","Drive through the whole foot"]','["Stop for sharp knee, hip, or back pain"]','squat','full','{"primary_angles":["left_knee","right_knee"],"down_threshold":95,"up_threshold":160}'),
('push-up','Push-up','strength','beginner','{}','["Place hands slightly wider than shoulders","Keep a straight line from head to heels","Lower under control","Press to full extension"]','["Use an elevated surface if floor push-ups are not controlled"]','push_up','full','{"primary_angles":["left_elbow","right_elbow"],"down_threshold":90,"up_threshold":155}'),
('plank','Forearm Plank','core','beginner','{}','["Place elbows beneath shoulders","Brace the trunk","Keep hips level","Breathe normally"]','["Stop if you cannot maintain a neutral back"]','plank','partial','{"duration_seconds":30}'),
('reverse-lunge','Reverse Lunge','strength','beginner','{}','["Stand tall","Step one foot back","Lower both knees under control","Return through the front foot"]','["Use support if balance is uncertain"]','lunge','full','{"primary_angles":["left_knee","right_knee"],"down_threshold":100,"up_threshold":160}')
on conflict(slug) do nothing;
