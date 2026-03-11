
create table help_link
(
    id                      bigserial not null primary key,
    country_id              bigint references country,
    case_stage              text,
    job_stage               text,
    label                   text not null,
    link                    text not null
);
