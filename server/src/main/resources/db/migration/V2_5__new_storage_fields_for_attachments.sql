alter table candidate_attachment add column if not exists active boolean default true not null;
alter table candidate_attachment add column if not exists bucket text;
alter table candidate_attachment add column if not exists content_length bigint;
alter table candidate_attachment add column if not exists public_id text;
alter table candidate_attachment add column if not exists sha256_hex text;
alter table candidate_attachment add column if not exists storage_key text;

create index if not exists candidate_attachment_public_id_idx on candidate_attachment(public_id);



