
create table candidate_exam
(
    id                      bigserial not null primary key,
    candidate_id            bigint not null references candidate,
    exam                    text,
    other_exam              text,
    score                   text
);
