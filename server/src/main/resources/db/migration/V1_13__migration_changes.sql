
alter table candidate_attachment add column admin_only boolean default false not null;
alter table candidate_attachment add column migrated boolean default false not null;
alter table candidate_attachment add column file_type text;

alter table candidate add column migration_education_major_id bigint references education_major;
