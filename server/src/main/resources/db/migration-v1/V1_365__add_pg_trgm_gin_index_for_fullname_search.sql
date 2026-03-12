CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS users_full_name_trgm_idx
    ON public.users USING GIN ((lower(trim(first_name) || ' ' || trim(last_name))) gin_trgm_ops);

CREATE INDEX IF NOT EXISTS users_full_name_rev_trgm_idx
    ON public.users USING GIN ((lower(trim(last_name) || ' ' || trim(first_name))) gin_trgm_ops);
