
alter table candidate drop column dependants;
alter table candidate drop column dependants_notes;

create table candidate_dependant
(
    id                      bigserial not null primary key,
    candidate_id            bigint not null references candidate,
    relation                text,
    dob                     date,
    health_concern          text,
    notes                   text
);
