
alter table candidate_visa add column created_by   bigint references users;
alter table candidate_visa add column created_date timestamptz;
alter table candidate_visa add column updated_by   bigint references users;
alter table candidate_visa add column updated_date timestamptz;
alter table candidate_visa add column protection text;
alter table candidate_visa add column protection_grounds text;
alter table candidate_visa add column tbb_eligibility_assessment text;

