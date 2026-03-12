
alter table candidate add column mini_intake_completed_by bigint references users;
alter table candidate add column mini_intake_completed_date timestamptz;
alter table candidate add column full_intake_completed_by bigint references users;
alter table candidate add column full_intake_completed_date timestamptz;
