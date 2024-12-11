
create table candidate_property (
  candidate_id      bigint not null references candidate,
  name              text not null,
  value             text,
  related_task_id   bigint references task,

--   Name must be unique for a candidate. ie a Candidate cannot have two properties with the same name
  primary key (candidate_id, name)
);
