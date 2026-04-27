
create table candidate_visa
(
    id                      bigserial not null primary key,
    candidate_id            bigint not null references candidate,
    country_id              bigint references country,
    eligibility             text,
    assessment_notes        text
);
