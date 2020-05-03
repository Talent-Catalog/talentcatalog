alter table candidate_attachment drop column url;
alter table candidate_attachment drop column file_name;
alter table candidate_attachment add column updated_by bigint references users;
alter table candidate_attachment add column updated_date timestamp with time zone;
alter table candidate_attachment add column location text not null;
