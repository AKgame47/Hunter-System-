create or replace function public.get_leaderboard(p_limit integer default 50)
returns table(leaderboard_position bigint, display_name text, level integer, rank text, total_xp bigint, completed_quests integer)
language sql security definer set search_path = '' stable
as $$
  select row_number() over (order by (p.level::bigint * 500 + p.xp) desc, p.completed_quests desc, p.user_id),
    left(coalesce(nullif(p.display_name, ''), 'Hunter'), 80)::text,
    p.level, p.rank::text, (p.level::bigint * 500 + p.xp)::bigint, p.completed_quests
  from public.profiles p
  order by (p.level::bigint * 500 + p.xp) desc, p.completed_quests desc, p.user_id
  limit least(greatest(coalesce(p_limit, 50), 1), 100);
$$;
revoke all on function public.get_leaderboard(integer) from public, anon;
grant execute on function public.get_leaderboard(integer) to authenticated;
