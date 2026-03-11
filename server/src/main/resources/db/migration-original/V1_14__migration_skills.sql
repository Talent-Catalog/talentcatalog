
create table candidate_skill (
  id                      bigserial not null primary key,
  candidate_id            bigint not null references candidate,
  skill                   text,
  time_period             integer
);
