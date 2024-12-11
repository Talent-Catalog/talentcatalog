
create table candidate_destination
(
    id                      bigserial not null primary key,
    candidate_id            bigint not null references candidate,
    country_id              bigint references country,
    interest                text,
    family                  text,
    location                text,
    notes                   text
);
