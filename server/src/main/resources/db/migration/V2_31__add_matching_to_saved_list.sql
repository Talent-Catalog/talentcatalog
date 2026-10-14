-- Nullable: existing lists get their Matching lazily, when one is needed.
alter table saved_list add column if not exists matching_id bigint references matching (id);
create index if not exists saved_list_matching_id_idx on saved_list (matching_id);
