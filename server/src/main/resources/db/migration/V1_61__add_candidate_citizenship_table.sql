
create table candidate_citizenship
(
    id                      bigserial not null primary key,
    candidate_id            bigint not null references candidate,
    nationality_id          bigint references nationality,
    has_passport            text,
    notes                   text
);
