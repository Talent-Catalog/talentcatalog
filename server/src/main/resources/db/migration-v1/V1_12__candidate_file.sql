
alter table candidate_file rename to candidate_attachment;
alter table candidate_attachment drop column url;
alter table candidate_attachment drop column file_name;
alter table candidate_attachment add column updated_by bigint references users;
alter table candidate_attachment add column updated_date timestamptz;
alter table candidate_attachment add column location text not null;
