
create table country_nationality_join
(
    country_id bigint not null references country,
    nationality_id bigint not null references nationality,
    primary key (country_id, nationality_id)
);
